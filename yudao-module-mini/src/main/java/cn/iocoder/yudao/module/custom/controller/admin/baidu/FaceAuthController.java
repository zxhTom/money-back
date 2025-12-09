package cn.iocoder.yudao.module.custom.controller.admin.baidu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.baidu.vo.BaiduUserInfo;
import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@RestController
@RequestMapping("/api/faceAuth")
public class FaceAuthController {

    @Autowired
    private BaiduFaceAuthService baiduFaceAuthService;

    @Autowired
    WechatService wechatService;
    // 你的小程序服务器域名，用于构造回调地址
    @Value("${server.domain}")
    private String serverDomain;

    /**
     * 第一步：小程序调用此接口，开启一个核身流程
     * 返回 verify_token 和 构造好的核身URL
     */
    @GetMapping("/start")
    public CommonResult<Map<String, Object>> startFaceAuth(@RequestParam String idCard) throws Exception {
        // 你的小程序回调页面路径，用于接收核身完成后的跳转
        String successUrl = serverDomain + "/api/faceAuth/callback?status=success&idCard="+idCard;
        String failUrl = serverDomain + "/pages/authResult?status=fail&idCard="+idCard;
//        successUrl = wechatService.generateUrlLink("/pages/authResult/authResult", "status=success");
//        failUrl = wechatService.generateUrlLink("/pages/authResult/authResult", "status=fail");
        // 1. 获取 verify_token
        String verifyToken = baiduFaceAuthService.getVerifyToken(successUrl, failUrl);

        // 2. 根据 token 构造H5核身页面URL[citation:6]
        String authUrl = String.format("https://brain.zxhtom.store/face/print/?token=%s&successUrl=%s&failedUrl=%s",
                verifyToken, successUrl, failUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("verify_token", verifyToken);
        result.put("auth_url", authUrl); // 小程序端用 web-view 加载此链接
        return success(result);
    }

    /**
     * 第二步：小程序在获取用户输入的姓名、身份证后，调用此接口上报信息
     */
    @PostMapping("/reportInfo")
    public CommonResult<Map<String, Object>> reportUserInfo(@RequestBody BaiduUserInfo baiduUserInfo) throws Exception {
        String verifyToken = baiduUserInfo.getVerifyToken();
        if (StringUtils.isEmpty(verifyToken)) {
            CommonResult<Map<String, Object>> mapCommonResult = startFaceAuth(baiduUserInfo.getIdCard());
            Map<String, Object> checkedData = mapCommonResult.getCheckedData();
            verifyToken = checkedData.getOrDefault("verify_token","").toString();
        }
        String name = baiduUserInfo.getName();
        String idCard = baiduUserInfo.getIdCard();
        boolean success = baiduFaceAuthService.reportUserInfo(verifyToken, name, idCard);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (!success) {
            result.put("message", "信息上报失败，请重试");
        }
        return success(result);
    }

    // 注意：还需要一个查询最终核验结果的接口（调用百度云结果查询API[citation:2]）
    // 核身完成后，你的服务端必须调用此接口验证，不能仅依赖前端回调
    @PostMapping("/queryResult")
    public CommonResult<Map<String, Object>> queryFaceAuthResult(@RequestParam String verifyToken) throws Exception {
        return success(baiduFaceAuthService.queryFaceAuthResult(verifyToken));
    }
    /**
     * 接收百度云人脸核身回调，并返回引导页面
     * 访问示例：/api/faceAuth/callback?status=success&verify_token=xxx&user_id=xxx
     */
    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    @PermitAll
    public void handleCallback(
            @RequestParam String status,
            @RequestParam String idCard,
            @RequestParam(required = false) String user_id,
            HttpServletResponse response) throws IOException {

        // 1. 【关键】在此处保存核验凭证并关联用户 (例如存入数据库或缓存)
        // 您的业务逻辑：saveAuthResult(verify_token, status, user_id);
        wechatService.updateVerify(idCard, "success".equals(status) ? 1 : 0);
        // 2. 构建并输出一个友好的HTML引导页
        buildHtmlResponse(response, status, idCard);
    }

    private void buildHtmlResponse(HttpServletResponse response, String status, String idCard) throws IOException {
        // 设置响应内容类型为HTML
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();

        // 根据状态设置页面内容
        String title;
        String message;
        String icon;

        if ("success".equalsIgnoreCase(status)) {
            title = "验证成功！";
            message = String.format("人脸核身流程已完成 (凭证：%s)。请返回小程序查看结果。", idCard);
            icon = "✅";
        } else {
            title = "验证未完成";
            message = String.format("人脸核身流程中断或失败 (凭证：%s)。请返回小程序重新尝试。", idCard);
            icon = "⏸️";
        }

        // 输出一个简洁美观的HTML页面
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"zh-CN\">");
        out.println("<head>");
        out.println("    <meta charset=\"UTF-8\">");
        out.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">");
        out.println("    <title>" + title + "</title>");
        out.println("    <style>");
        out.println("        * { margin: 0; padding: 0; box-sizing: border-box; }");
        out.println("        body { font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', sans-serif; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); min-height: 100vh; display: flex; justify-content: center; align-items: center; padding: 20px; }");
        out.println("        .card { background: white; border-radius: 24px; padding: 40px 30px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1); text-align: center; max-width: 500px; width: 100%; }");
        out.println("        .icon { font-size: 80px; margin-bottom: 25px; line-height: 1; }");
        out.println("        .title { font-size: 28px; font-weight: 700; color: #333; margin-bottom: 15px; }");
        out.println("        .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; word-break: break-all; }");
        out.println("        .tip { font-size: 14px; color: #999; padding: 15px; background-color: #f8f9fa; border-radius: 12px; margin-top: 25px; }");
        out.println("        .token { font-size: 12px; color: #aaa; margin-top: 20px; word-break: break-all; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class=\"card\">");
        out.println("        <div class=\"icon\">" + icon + "</div>");
        out.println("        <h1 class=\"title\">" + title + "</h1>");
        out.println("        <p class=\"message\">" + message + "</p>");
        out.println("        <div class=\"tip\">💡 请手动关闭此页面，或从微信聊天列表/最近使用中重新进入您的小程序。</div>");
        out.println("        <div class=\"token\">核验凭证：" + idCard + "</div>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");

        out.flush();
    }
}
