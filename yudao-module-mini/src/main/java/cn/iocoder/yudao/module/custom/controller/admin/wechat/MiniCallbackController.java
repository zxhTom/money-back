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
 * 说明（重要，涉及已知的安全取舍，改动前务必确认这是产品明确要的）：
 *   brain.toms.chat 核身完成后只会重定向到 successUrl / failUrl（?status=success|failed&idCard=xxx），
 *   这个重定向里没有 verify_token，本身又是免登录公开地址，任何人都能伪造这个请求。
 *   本来（2026-07-29 之前）这里已经改成完全不信任 status，只认 idCard 反查
 *   FaceAuthCallbackTokenStore 里的 verify_token、拿它向百度查权威结果（server -> 百度，
 *   无法被第三方伪造）、只有百度确认"通过"才写库。
 *   2026-07-29 应产品明确要求恢复了"status 快速路径"：status=success 时立即
 *   updateVerify(idCard,1)，不等百度查询——这意味着这个免登录回调地址理论上可以被
 *   伪造 status=success 抢先把任意 idCard 标记为已认证，这是明确知情并接受的代价，
 *   不是遗漏。服务端查百度那套（resolveVerificationOutcome）仍然保留，用于渲染
 *   真实结果页状态（pending/success/failed），对 PASSED 结果也会重复写一次
 *   verified=1（幂等），但不会撤销快速路径已经写入的结果。
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

        // 快速路径（明确要求恢复，接受其安全代价）：直接信任回调自带的 status，
        // status=success 时立即置 verified=1，不等下面的服务端查百度权威结果。
        // 代价：这个回调地址本身免登录、任何人可伪造 status=success，所以这行本身
        // 不能证明用户真的通过了核身——它只是为了比等百度查询快。真正的权威判定
        // 仍然是下面 resolveVerificationOutcome 里的服务端查百度，失败/不确定都不会
        // 被这里的快速路径覆盖或抢先（resolveVerificationOutcome 对 PASSED 结果会
        // 再次 updateVerify(idCard,1)，是幂等重复写，不是回退）。
        if ("success".equals(status)) {
            wechatService.updateVerify(idCard, 1);
        }

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
                outcome = interpretQueryResult(queryResult);
            } catch (Exception e) {
                log.warn("[FaceAuth] 第{}次查询百度权威结果异常，idCard(脱敏)={}", attempt + 1, idNoDisplay, e);
            }
        }

        if (outcome == QueryOutcome.PASSED) {
            faceAuthCallbackTokenStore.delete(idCard);
            wechatService.updateVerify(idCard, 1);
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
