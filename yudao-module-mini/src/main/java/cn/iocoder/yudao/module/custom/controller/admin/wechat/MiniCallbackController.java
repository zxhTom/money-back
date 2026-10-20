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
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Locale;

/**
 * 微信小程序 WebView 回调控制器
 * 用于接收外部回调（brain.toms.chat 人脸核身完成后重定向到此）并把结果回传小程序。
 *
 * 回调参数仅用于兼容原有跳转。认证状态只信任服务端保存的核身会话和百度查询结果，
 * 并核对百度确认的证件号与该会话对应的证件号一致。
 *
 * @author zxhtom
 */
@RestController
@RequestMapping("/api/mini/callback")
@PermitAll
@Slf4j
@TenantIgnore
public class MiniCallbackController {

    private static final int MAX_QUERY_ATTEMPTS = 3;
    private static final long QUERY_RETRY_INTERVAL_MILLIS = 500L;

    private enum QueryOutcome { PASSED, NOT_PASSED, INCONCLUSIVE }

    @Autowired
    private ITemplateEngine templateEngine;
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
        // 不把客户端自行传入的 token 当作服务端已验证的会话标识。
        context.setVariable("verifyToken", null);

        // 使用 Thymeleaf 渲染模板
        String html = templateEngine.process("mini-callback", context);

        // 输出 HTML
        PrintWriter out = response.getWriter();
        out.println(html);
        out.flush();
    }

    /**
     * 服务端权威校验：查出 idCard -> verify_token 映射，向百度查询这次核身的真实结果，
     * 只有确认通过才写 verified=1。映射只在拿到确定结果（通过/不通过）时才删除，
     * 仍不确定时保留映射靠 TTL 自然过期，避免抢先伪造的回调把真实会话状态提前清空。
     * 返回值用于渲染结果页（"success"/"failed"/"pending"）。
     */
    private String resolveVerificationOutcome(String idCard, String idNoDisplay) {
        String pendingVerifyToken = faceAuthCallbackTokenStore.get(idCard);
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
                outcome = interpretQueryResult(queryResult, idCard);
            } catch (Exception e) {
                log.warn("[FaceAuth] 第{}次查询百度权威结果异常，idCard(脱敏)={}", attempt + 1, idNoDisplay, e);
            }
        }

        if (outcome == QueryOutcome.PASSED) {
            wechatService.updateVerify(idCard, 1);
            faceAuthCallbackTokenStore.delete(idCard);
            return "success";
        } else if (outcome == QueryOutcome.NOT_PASSED) {
            faceAuthCallbackTokenStore.delete(idCard);
            log.warn("[FaceAuth] 百度权威结果未通过，不写库。idCard(脱敏)={}", idNoDisplay);
            return "failed";
        } else {
            // 重试耗尽仍不确定：不删映射，留给 30 分钟 TTL 自然过期——避免网络抖动或抢先伪造的回调
            // 提前烧掉这次真实核身会话的 token，导致后续真实回调被误判为失败。
            log.warn("[FaceAuth] 重试后仍无法确认结果，不写库、不判定失败、不删除映射。idCard(脱敏)={}", idNoDisplay);
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

    /** 查询失败、响应缺字段时不认证，保留会话供用户稍后重试。 */
    private QueryOutcome interpretQueryResult(Map<String, Object> queryResult, String idCard) {
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
        if (!passed) {
            return QueryOutcome.NOT_PASSED;
        }
        JSONObject identity = ((JSONObject) data).getJSONObject("idcard_confirm");
        String confirmedId = identity == null ? null : identity.getString("idcard_number");
        String expectedId = idCardCipherService.resolveToPlain(idCard);
        if (confirmedId == null || confirmedId.trim().isEmpty()
                || expectedId == null || expectedId.trim().isEmpty()) {
            return QueryOutcome.INCONCLUSIVE;
        }
        return confirmedId.trim().toUpperCase(Locale.ROOT).equals(expectedId.trim().toUpperCase(Locale.ROOT))
                ? QueryOutcome.PASSED : QueryOutcome.NOT_PASSED;
    }
}
