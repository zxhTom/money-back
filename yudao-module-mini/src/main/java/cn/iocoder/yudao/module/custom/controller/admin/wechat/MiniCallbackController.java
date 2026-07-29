package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.framework.faceauth.FaceAuthCallbackTokenStore;
import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Map;

/**
 * 微信小程序 WebView 回调控制器
 * 用于接收外部回调（brain.toms.chat 人脸核身完成后重定向到此）并把结果回传小程序。
 *
 * 说明（重要，勿再改回"直接信任 status"）：
 *   brain.toms.chat 核身完成后只会重定向到 successUrl / failUrl（?status=success|failed&idCard=xxx），
 *   这个重定向里没有 verify_token，本身又是免登录公开地址，任何人都能伪造这个请求。
 *   因此这里不再信任 status，而是用 idCard 反查 FaceAuthCallbackTokenStore 里 startFaceAuth 时
 *   记录的 verify_token，拿这个 token 向百度查权威结果（server -> 百度，无法被第三方伪造），
 *   只有百度确认"通过"才写库。status 只用来渲染结果页文案，不驱动任何写库判断。
 *
 * @author zxhtom
 */
@RestController
@RequestMapping("/api/mini/callback")
@PermitAll
@Slf4j
public class MiniCallbackController {

    private static final int MAX_QUERY_ATTEMPTS = 3;
    private static final long QUERY_RETRY_INTERVAL_MILLIS = 500L;

    private enum QueryOutcome { PASSED, NOT_PASSED, INCONCLUSIVE }

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    WechatService wechatService;

    @Autowired
    private IdCardCipherService idCardCipherService;
    @Resource
    private FaceAuthCallbackTokenStore faceAuthCallbackTokenStore;
    @Resource
    private BaiduFaceAuthService baiduFaceAuthService;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PermitAll
    public void handleCallback(
            @RequestParam("status") String status,
            @RequestParam("idCard") String idCard,
            @RequestParam(value = "verifyToken", required = false) String verifyToken,
            HttpServletResponse response) throws IOException {
        String idNoDisplay = idCardCipherService.idNoDisplayFromStored(idCard);
        String renderStatus = resolveVerificationOutcome(idCard, idNoDisplay);

        // 设置响应内容类型为HTML
        response.setContentType("text/html; charset=utf-8");

        String idNoCipher = idCardCipherService.storedToCipherForResponse(idCard);

        // 创建 Thymeleaf 上下文，设置变量
        Context context = new Context();
        context.setVariable("status", renderStatus);
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

    /**
     * 服务端权威校验：消费 idCard -> verify_token 映射，向百度查询这次核身的真实结果，
     * 只有确认通过才写 verified=1。返回值用于渲染结果页（"success"/"failed"/"pending"）。
     */
    private String resolveVerificationOutcome(String idCard, String idNoDisplay) {
        String pendingVerifyToken = faceAuthCallbackTokenStore.consumeAndGet(idCard);
        if (pendingVerifyToken == null) {
            log.warn("[FaceAuth] 回调时找不到对应的待验证会话（可能是伪造请求或已过期），不写库。idCard(脱敏)={}", idNoDisplay);
            return "failed";
        }

        QueryOutcome outcome = QueryOutcome.INCONCLUSIVE;
        for (int attempt = 0; attempt < MAX_QUERY_ATTEMPTS && outcome == QueryOutcome.INCONCLUSIVE; attempt++) {
            if (attempt > 0) {
                if (!sleepBeforeRetry()) {
                    break;
                }
            }
            try {
                Map<String, Object> queryResult = baiduFaceAuthService.queryFaceAuthResult(pendingVerifyToken);
                outcome = interpretQueryResult(queryResult);
            } catch (Exception e) {
                log.warn("[FaceAuth] 第{}次查询百度权威结果异常，idCard(脱敏)={}", attempt + 1, idNoDisplay, e);
            }
        }

        if (outcome == QueryOutcome.PASSED) {
            wechatService.updateVerify(idCard, 1);
            return "success";
        } else if (outcome == QueryOutcome.NOT_PASSED) {
            log.warn("[FaceAuth] 百度权威结果未通过，不写库。idCard(脱敏)={}", idNoDisplay);
            return "failed";
        } else {
            log.warn("[FaceAuth] 重试后仍无法确认结果，不写库、不判定失败。idCard(脱敏)={}", idNoDisplay);
            return "pending";
        }
    }

    private boolean sleepBeforeRetry() {
        try {
            Thread.sleep(QUERY_RETRY_INTERVAL_MILLIS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * ⚠️ data.passed 这个字段名未在真实百度响应上验证过（queryFaceAuthResult 此前是死代码，从未被调用），
     * 上线前必须用真实核身流程核对。success=false（含异常）一律视为 INCONCLUSIVE 触发重试，
     * 而不是直接判定 NOT_PASSED，避免把"百度那边暂时查不到/接口抖动"误判成"核验不通过"。
     */
    private QueryOutcome interpretQueryResult(Map<String, Object> queryResult) {
        if (queryResult == null || !Boolean.TRUE.equals(queryResult.get("success"))) {
            return QueryOutcome.INCONCLUSIVE;
        }
        Object data = queryResult.get("data");
        if (!(data instanceof JSONObject)) {
            return QueryOutcome.INCONCLUSIVE;
        }
        Boolean passed = ((JSONObject) data).getBoolean("passed");
        if (passed == null) {
            return QueryOutcome.INCONCLUSIVE;
        }
        return passed ? QueryOutcome.PASSED : QueryOutcome.NOT_PASSED;
    }
}
