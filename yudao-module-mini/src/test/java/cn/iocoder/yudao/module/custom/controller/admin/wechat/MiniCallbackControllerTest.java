package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 人脸核身回调：brain 回调只回传 status+idCard（无 verify_token/nonce），只能以 status 为准。
 * 策略——只认 success 置 verified=1；failed 绝不清零（防恶意降级）。
 */
@ExtendWith(MockitoExtension.class)
public class MiniCallbackControllerTest {

    @Mock
    private WechatService wechatService;
    @Mock
    private IdCardCipherService idCardCipherService;
    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private MiniCallbackController controller;

    private HttpServletResponse response;

    @BeforeEach
    public void setUp() throws Exception {
        response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        lenient().when(idCardCipherService.storedToCipherForResponse(anyString())).thenReturn("cipher");
        lenient().when(idCardCipherService.idNoDisplayFromStored(anyString())).thenReturn("110***********1234");
        lenient().when(templateEngine.process(eq("mini-callback"), any())).thenReturn("<html></html>");
    }

    @Test
    public void testSuccess_marksVerified() throws Exception {
        controller.handleCallback("success", "idCardPlain", "tok", response);
        verify(wechatService).updateVerify("idCardPlain", 1);
    }

    @Test
    public void testFailed_doesNotTouchVerified() throws Exception {
        controller.handleCallback("failed", "idCardPlain", null, response);
        verify(wechatService, never()).updateVerify(anyString(), anyInt());
    }

    @Test
    public void testRendersHtml() throws Exception {
        controller.handleCallback("success", "idCardPlain", null, response);
        verify(response).setContentType("text/html; charset=utf-8");
        verify(templateEngine).process(eq("mini-callback"), any());
    }
}
