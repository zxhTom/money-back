package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.framework.faceauth.FaceAuthCallbackTokenStore;
import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 人脸核身回调：2026-07-29 应产品明确要求恢复了"status 快速路径"——
 * status=success 时立即 updateVerify(idCard,1)，不等百度查询，接受这意味着
 * 免登录回调地址可被伪造 status=success 抢先置位任意 idCard 的 verified。
 * 服务端查百度权威结果（通过 idCard -> verify_token 映射反查）仍然保留，
 * 用于渲染真实结果页状态、以及在快速路径之外也能最终确认 PASSED（幂等重复写，不撤销）。
 */
@ExtendWith(MockitoExtension.class)
public class MiniCallbackControllerTest {

    @Mock
    private WechatService wechatService;
    @Mock
    private IdCardCipherService idCardCipherService;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private FaceAuthCallbackTokenStore faceAuthCallbackTokenStore;
    @Mock
    private BaiduFaceAuthService baiduFaceAuthService;

    @InjectMocks
    private MiniCallbackController controller;

    private HttpServletResponse response;

    @BeforeEach
    public void setUp() throws Exception {
        response = mock(HttpServletResponse.class);
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        lenient().when(idCardCipherService.storedToCipherForResponse(anyString())).thenReturn("cipher");
        lenient().when(idCardCipherService.idNoDisplayFromStored(anyString())).thenReturn("110***********1234");
        lenient().when(templateEngine.process(eq("mini-callback"), any())).thenReturn("<html></html>");
    }

    private static Map<String, Object> queryResult(boolean success, Boolean passed) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (success) {
            JSONObject data = new JSONObject();
            if (passed != null) {
                data.put("passed", passed);
            }
            result.put("data", data);
        }
        return result;
    }

    @Test
    public void testNoTokenMapping_statusSuccess_fastPathStillWritesVerified() throws Exception {
        // 找不到待验证会话（过期/伪造请求的典型特征）——这正是快速路径接受的代价：
        // 即使从没有发起过真实核身会话，伪造 status=success 也会立即写 verified=1。
        // 百度查询这条权威链路仍然会跑（用于渲染结果页），但结果不影响已经写入的 verified。
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn(null);

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(wechatService, times(1)).updateVerify("idCardPlain", 1);
        verify(baiduFaceAuthService, never()).queryFaceAuthResult(anyString());
    }

    @Test
    public void testTokenMapping_baiduConfirmsPassed_writesVerified() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, true));

        controller.handleCallback("success", "idCardPlain", null, response);

        // 快速路径（status=success）先写一次，百度确认 PASSED 后再幂等写一次，共两次
        verify(wechatService, times(2)).updateVerify("idCardPlain", 1);
    }

    @Test
    public void testTokenMapping_baiduConfirmsNotPassed_fastPathAlreadyWroteVerified() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, false));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(baiduFaceAuthService, times(1)).queryFaceAuthResult("verifyTokenAbc");
        // 百度确认未通过不会再写，但快速路径已经因为 status=success 写过一次——
        // 这正是恢复快速路径要接受的代价：百度事后确认不通过，也无法撤销已经写入的 verified。
        verify(wechatService, times(1)).updateVerify("idCardPlain", 1);
    }

    @Test
    public void testTokenMapping_baiduQueryThrows_fastPathAlreadyWroteVerified() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenThrow(new RuntimeException("网络异常"));

        controller.handleCallback("success", "idCardPlain", null, response);

        // 快速路径已经写过一次；百度查询异常按 INCONCLUSIVE 处理不会再写，但也不会重试少于3次
        verify(wechatService, times(1)).updateVerify("idCardPlain", 1);
        verify(baiduFaceAuthService, times(3)).queryFaceAuthResult("verifyTokenAbc");
    }

    @Test
    public void testFailedStatus_fastPathDoesNotFire_butBaiduPassedStillWrites() throws Exception {
        // 快速路径只在 status=success 时触发；status=failed 时不触发快速路径写入，
        // 但百度权威结果如果确实通过，resolveVerificationOutcome 仍然会写库一次
        // （体现快速路径只是"加速"，不是替代百度查询，且不会因 status=failed 而拒绝写入）
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, true));

        controller.handleCallback("failed", "idCardPlain", null, response);

        verify(wechatService, times(1)).updateVerify("idCardPlain", 1);
    }

    @Test
    public void testRetry_firstTwoInconclusive_thirdPassed_writesVerified() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc"))
                .thenReturn(queryResult(false, null))
                .thenReturn(queryResult(false, null))
                .thenReturn(queryResult(true, true));

        controller.handleCallback("success", "idCardPlain", null, response);

        // 快速路径（status=success）先写一次，重试到第3次百度确认 PASSED 后再幂等写一次，共两次
        verify(wechatService, times(2)).updateVerify("idCardPlain", 1);
        verify(baiduFaceAuthService, times(3)).queryFaceAuthResult("verifyTokenAbc");
    }

    @Test
    public void testRendersHtml() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn(null);

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(response).setContentType("text/html; charset=utf-8");
        verify(templateEngine).process(eq("mini-callback"), any());
    }

    @Test
    public void testInconclusive_doesNotDeleteMapping_letsTTLExpireNaturally() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(false, null));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(faceAuthCallbackTokenStore, never()).delete(anyString());
    }

    @Test
    public void testPassed_deletesMapping() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, true));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(faceAuthCallbackTokenStore).delete("idCardPlain");
    }

    @Test
    public void testNotPassed_deletesMapping() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, false));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(faceAuthCallbackTokenStore).delete("idCardPlain");
    }
}
