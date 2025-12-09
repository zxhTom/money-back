package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.service.wechat.OffcialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/offcial")
public class OffcialController {

    @Autowired
    OffcialService offcialService;

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
    public String handleMessage(
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestBody String requestBody,
            HttpServletRequest request) {

        // 验证签名
        if (!offcialService.checkSignature(signature, timestamp, nonce)) {
            return "invalid signature";
        }

        // 处理微信消息
        return offcialService.processMessage(requestBody, request);
    }
}
