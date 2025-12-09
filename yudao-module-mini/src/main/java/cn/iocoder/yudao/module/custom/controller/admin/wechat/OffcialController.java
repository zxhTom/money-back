package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.service.wechat.OffcialService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatMenuService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/offcial/{appid}")
@PermitAll
@Slf4j
public class OffcialController {
    @Autowired
    WxMpService wxMpService;
    @Autowired
    OffcialService offcialService;
//    @Autowired
//    WxMpMessageRouter wxMpMessageRouter;

    @Autowired
    WechatMenuService wechatMenuService;
    @GetMapping("/menu/create")
    public String validate(){
        String menu = wechatMenuService.createMenu();
        return menu;
    }
    /**
     * 微信服务器验证（GET请求）
     */
    @GetMapping("/callback")
    public String validate(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        // 验证签名
        if (offcialService.checkSignature(signature, timestamp, nonce)) {
            return echostr; // 返回echostr表示验证成功
        }
        return "invalid";
    }

    /**
     * 接收微信消息（POST请求）
     */
    @PostMapping("/callback")
    public String post(
            @PathVariable String appid,
            @RequestBody String requestBody,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("openid") String openid,
            @RequestParam(name = "encrypt_type", required = false) String encType,
            @RequestParam(name = "msg_signature", required = false) String msgSignature) {

        log.info("\n接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}], timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        // 切换公众号配置（多公众号支持）
        if (!wxMpService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }

        // 1. 验证消息签名
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        // 2. 处理加密消息（如果配置了加密）
        if ("aes".equalsIgnoreCase(encType)) {
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(
                    requestBody, wxMpService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());

            // 3. >>> **核心步骤：将消息交给路由器，路由器会自动找到对应的Handler！**
//            WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
//            if (outMessage != null) {
//                out = outMessage.toEncryptedXml(wxMpService.getWxMpConfigStorage());
//            }
        } else {
            // 4. 处理明文消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody); // **此处将XML字符串解析成对象**
            log.debug("\n明文消息内容为：\n{} ", inMessage.toString());

            // 5. >>> **核心步骤：将消息交给路由器**
/*
            WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
            if (outMessage != null) {
                out = outMessage.toXml();
            }
*/
        }

        log.debug("\n组装回复信息：{}", out);
        return out; // 将最终XML回复给微信服务器
    }
}
