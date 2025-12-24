package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 微信小程序 WebView 回调控制器
 * 用于接收外部回调并将结果通知回小程序
 *
 * @author zxhtom
 */
@RestController
@RequestMapping("/api/mini/callback")
@PermitAll
public class MiniCallbackController {

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    WechatService wechatService;

    /**
     * 接收回调并返回结果页面
     * 访问示例：/api/mini/callback?status=success&idNo=xxx&verifyToken=xxx
     *
     * @param status 状态：success 或 failed
     * @param idCard 身份证号或其他标识
     * @param verifyToken 验证令牌
     * @param response HTTP 响应对象
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PermitAll
    public void handleCallback(
            @RequestParam("status") String status,
            @RequestParam("idCard") String idCard,
            @RequestParam(value = "verifyToken",required = false) String verifyToken,
            HttpServletResponse response) throws IOException {
        wechatService.updateVerify(idCard, "success".equals(status) ? 1 : 0);
        // 设置响应内容类型为HTML
        response.setContentType("text/html; charset=utf-8");

        // 创建 Thymeleaf 上下文，设置变量
        Context context = new Context();
        context.setVariable("status", status);
        context.setVariable("idNo", idCard);
        context.setVariable("verifyToken", verifyToken);

        // 使用 Thymeleaf 渲染模板
        String html = templateEngine.process("mini-callback", context);

        // 输出 HTML
        PrintWriter out = response.getWriter();
        out.println(html);
        out.flush();
    }
}

