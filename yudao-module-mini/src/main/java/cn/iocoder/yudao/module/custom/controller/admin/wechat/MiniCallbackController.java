package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 微信小程序 WebView 回调控制器
 * 用于接收外部回调（brain.toms.chat 人脸核身完成后重定向到此）并把结果回传小程序。
 *
 * 说明（重要，勿再改成"回调里反查百度"）：
 *   brain.toms.chat 核身完成后只会重定向到 successUrl / failUrl（?status=success|failed&idCard=xxx），
 *   这个重定向里没有 verify_token，服务端在回调处无 token 可反查百度，
 *   所以此处只能以 status 作为结果来源。若要做服务端权威校验，正确位置是小程序侧持有的 verify_token
 *   走 /api/faceAuth/queryResult（server→百度），不能放在本回调里。
 *
 * @author zxhtom
 */
@RestController
@RequestMapping("/api/mini/callback")
@PermitAll
@Slf4j
public class MiniCallbackController {

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    WechatService wechatService;

    @Autowired
    private IdCardCipherService idCardCipherService;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PermitAll
    public void handleCallback(
            @RequestParam("status") String status,
            @RequestParam("idCard") String idCard,
            @RequestParam(value = "verifyToken", required = false) String verifyToken,
            HttpServletResponse response) throws IOException {
        // 安全：本回调是公开地址，且 brain.toms.chat 重定向只回传 status+idCard（无 verify_token、无自定义 nonce），
        // 服务端在此无 token 可反查百度，只能以 status 为准。策略：只允许"认证成功"把 verified 置 1；
        // 伪造 failed 也绝不清零，防止恶意降级。（若要服务端权威校验，须由小程序持 verify_token 走 /api/faceAuth/queryResult。）
        if ("success".equals(status)) {
            wechatService.updateVerify(idCard, 1);
        }
        // 设置响应内容类型为HTML
        response.setContentType("text/html; charset=utf-8");

        String idNoCipher = idCardCipherService.storedToCipherForResponse(idCard);
        String idNoDisplay = idCardCipherService.idNoDisplayFromStored(idCard);

        // 创建 Thymeleaf 上下文，设置变量
        Context context = new Context();
        context.setVariable("status", status);
        context.setVariable("idNoCipher", idNoCipher);
        context.setVariable("idNoDisplay", idNoDisplay);
        context.setVariable("verifyToken", verifyToken);

        // 使用 Thymeleaf 渲染模板
        String html = templateEngine.process("mini-callback", context);

        // 输出 HTML
        PrintWriter out = response.getWriter();
        out.println(html);
        out.flush();
    }
}
