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
 * 人脸核身回调：brain 回调只回传 status+idCard，不可信任，服务端主动向百度查询权威结果
 * （通过 idCard -> verify_token 映射反查）后才决定是否写库。
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
    public void testNoTokenMapping_doesNotWriteVerified() throws Exception {
        // 找不到待验证会话（过期/伪造请求的典型特征）——即使伪造 status=success 也不写库
        when(faceAuthCallbackTokenStore.consumeAndGet("idCardPlain")).thenReturn(null);

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(wechatService, never()).updateVerify(anyString(), anyInt());
        verify(baiduFaceAuthService, never()).queryFaceAuthResult(anyString());
    }

    @Test
    public void testTokenMapping_baiduConfirmsPassed_writesVerified() throws Exception {
        when(faceAuthCallbackTokenStore.consumeAndGet("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, true));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(wechatService).updateVerify("idCardPlain", 1);
    }

    @Test
    public void testTokenMapping_baiduConfirmsNotPassed_doesNotWriteVerified() throws Exception {
        when(faceAuthCallbackTokenStore.consumeAndGet("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, false));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(baiduFaceAuthService, times(1)).queryFaceAuthResult("verifyTokenAbc");
        verify(wechatService, never()).updateVerify(anyString(), anyInt());
    }

    @Test
    public void testTokenMapping_baiduQueryThrows_doesNotWriteVerified() throws Exception {
        when(faceAuthCallbackTokenStore.consumeAndGet("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenThrow(new RuntimeException("网络异常"));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(wechatService, never()).updateVerify(anyString(), anyInt());
        // 异常按 INCONCLUSIVE 处理，会重试到用完 3 次
        verify(baiduFaceAuthService, times(3)).queryFaceAuthResult("verifyTokenAbc");
    }

    @Test
    public void testForgedStatusFailed_withRealMapping_stillTrustsBaiduNotStatus() throws Exception {
        // status 里伪造成 failed，但百度权威结果其实是通过的——服务端应以百度为准，仍然写库
        // （体现"不再直接信任回调里的 status"这个核心设计目标）
        when(faceAuthCallbackTokenStore.consumeAndGet("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc")).thenReturn(queryResult(true, true));

        controller.handleCallback("failed", "idCardPlain", null, response);

        verify(wechatService).updateVerify("idCardPlain", 1);
    }

    @Test
    public void testRetry_firstTwoInconclusive_thirdPassed_writesVerified() throws Exception {
        when(faceAuthCallbackTokenStore.consumeAndGet("idCardPlain")).thenReturn("verifyTokenAbc");
        when(baiduFaceAuthService.queryFaceAuthResult("verifyTokenAbc"))
                .thenReturn(queryResult(false, null))
                .thenReturn(queryResult(false, null))
                .thenReturn(queryResult(true, true));

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(wechatService).updateVerify("idCardPlain", 1);
        verify(baiduFaceAuthService, times(3)).queryFaceAuthResult("verifyTokenAbc");
    }

    @Test
    public void testRendersHtml() throws Exception {
        when(faceAuthCallbackTokenStore.consumeAndGet("idCardPlain")).thenReturn(null);

        controller.handleCallback("success", "idCardPlain", null, response);

        verify(response).setContentType("text/html; charset=utf-8");
        verify(templateEngine).process(eq("mini-callback"), any());
    }
}
