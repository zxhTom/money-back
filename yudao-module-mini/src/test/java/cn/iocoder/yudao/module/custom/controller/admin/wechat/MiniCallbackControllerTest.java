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
import org.thymeleaf.ITemplateEngine;

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

/** 回调不信任客户端状态或 token，只接受百度权威通过且身份匹配的结果。 */
@ExtendWith(MockitoExtension.class)
public class MiniCallbackControllerTest {

    @Mock
    private WechatService wechatService;
    @Mock
    private IdCardCipherService idCardCipherService;
    @Mock
    private ITemplateEngine templateEngine;
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
        lenient().when(idCardCipherService.resolveToPlain(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
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
                JSONObject identity = new JSONObject();
                identity.put("idcard_number", "idCardPlain");
                data.put("idcard_confirm", identity);
            }
            result.put("data", data);
        }
        return result;
    }

    @Test
    public void testNoTokenMapping_forgedSuccessNeverWritesVerified() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn(null);
        controller.handleCallback("success", "idCardPlain", "attacker-supplied-token", response);
        verify(wechatService, never()).updateVerify(anyString(), anyInt());
        verify(baiduFaceAuthService, never()).queryFaceAuthResult(anyString());
    }

    @Test
    public void testTokenMapping_baiduConfirmsPassed_writesVerified() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, true));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(wechatService, times(1)).updateVerify("idCardPlain", 1);
    }

    @Test
    public void testTokenMapping_baiduRejects_forgedSuccessNeverWrites() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, false));
        controller.handleCallback("success", "idCardPlain", null, response);
        verify(wechatService, never()).updateVerify(anyString(), anyInt());
    }

    @Test
    public void testTokenMapping_baiduQueryThrows_neverWritesAndRetainsSession() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenThrow(new RuntimeException("network unavailable"));
        controller.handleCallback("success", "idCardPlain", null, response);
        verify(wechatService, never()).updateVerify(anyString(), anyInt());
        verify(baiduFaceAuthService, times(3)).queryFaceAuthResult("verifyTokenAbc");
        verify(faceAuthCallbackTokenStore, never()).delete(anyString());
    }

    @Test
    public void testFailedStatus_fastPathDoesNotFire_butBaiduPassedStillWrites() throws Exception {
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

        verify(wechatService, times(1)).updateVerify("idCardPlain", 1);
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
    @Test
    public void testBaiduPassedForDifferentIdentity_neverWrites() throws Exception {
        when(faceAuthCallbackTokenStore.get("victimId")).thenReturn("server-token");
        when(baiduFaceAuthService.queryFaceAuthResult("server-token")).thenReturn(queryResult(true, true));
        controller.handleCallback("success", "victimId", "untrusted-token", response);
        verify(wechatService, never()).updateVerify(anyString(), anyInt());
        verify(baiduFaceAuthService).queryFaceAuthResult("server-token");
    }

    @Test
    public void testBaiduPassedWithoutConfirmedIdentity_neverWrites() throws Exception {
        when(faceAuthCallbackTokenStore.get("idCardPlain")).thenReturn("server-token");
        Map<String, Object> result = queryResult(true, true);
        ((JSONObject) result.get("data")).remove("idcard_confirm");
        when(baiduFaceAuthService.queryFaceAuthResult("server-token")).thenReturn(result);
        controller.handleCallback("success", "idCardPlain", null, response);
        verify(wechatService, never()).updateVerify(anyString(), anyInt());
        verify(faceAuthCallbackTokenStore, never()).delete(anyString());
    }

}
