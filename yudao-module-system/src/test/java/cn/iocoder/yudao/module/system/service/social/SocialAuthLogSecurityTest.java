package cn.iocoder.yudao.module.system.service.social;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SocialAuthLogSecurityTest {

    private SocialClientServiceImpl socialClientService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    public void setup() {
        socialClientService = spy(new SocialClientServiceImpl());
        logger = (Logger) LoggerFactory.getLogger(SocialClientServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    public void cleanup() {
        logger.detachAppender(listAppender);
    }

    @Test
    public void testGetAuthUserDoesNotLogSensitiveCodeStateOrUserData() {
        String sensitiveCode = "SYNTHETIC_AUTH_CODE_XYZ_987654";
        String sensitiveState = "SYNTHETIC_STATE_UUID_ABC_123456";
        String sensitiveToken = "SYNTHETIC_ACCESS_TOKEN_SECRET_KEY";
        String sensitiveNickname = "SYNTHETIC_PRIVATE_NICKNAME";

        AuthRequest mockAuthRequest = mock(AuthRequest.class);
        doReturn(mockAuthRequest).when(socialClientService).buildAuthRequest(anyInt(), anyInt());

        AuthUser authUser = new AuthUser();
        authUser.setUuid("test-uuid");
        authUser.setUsername(sensitiveNickname);
        authUser.setNickname(sensitiveNickname);

        AuthResponse<?> successResponse = AuthResponse.builder()
                .code(2000)
                .data(authUser)
                .build();
        when(mockAuthRequest.login(any(AuthCallback.class))).thenReturn((AuthResponse) successResponse);

        AuthUser returnedUser = socialClientService.getAuthUser(31, 2, sensitiveCode, sensitiveState);
        assertNotNull(returnedUser);
        assertEquals(sensitiveNickname, returnedUser.getNickname());

        for (ILoggingEvent event : listAppender.list) {
            String logMsg = event.getFormattedMessage();
            assertFalse(logMsg.contains(sensitiveCode), "日志不得包含认证 code");
            assertFalse(logMsg.contains(sensitiveState), "日志不得包含认证 state");
            assertFalse(logMsg.contains(sensitiveToken), "日志不得包含 token");
            assertFalse(logMsg.contains(sensitiveNickname), "日志不得包含用户昵称等个人资料");
            assertFalse(logMsg.contains("authResponse"), "不得打印 authResponse 序列化全量内容");
            assertFalse(logMsg.contains("authCallback"), "不得打印 authCallback 序列化全量内容");
        }
    }

    @Test
    public void testGetAuthUserFailureDoesNotLogSensitiveData() {
        String sensitiveCode = "SYNTHETIC_FAIL_AUTH_CODE_0001";
        String sensitiveState = "SYNTHETIC_FAIL_STATE_0002";

        AuthRequest mockAuthRequest = mock(AuthRequest.class);
        doReturn(mockAuthRequest).when(socialClientService).buildAuthRequest(anyInt(), anyInt());

        AuthResponse<?> failResponse = AuthResponse.builder()
                .code(5000)
                .msg("第三方授权失败错误描述")
                .build();
        when(mockAuthRequest.login(any(AuthCallback.class))).thenReturn((AuthResponse) failResponse);

        assertThrows(ServiceException.class, () -> socialClientService.getAuthUser(31, 2, sensitiveCode, sensitiveState));

        for (ILoggingEvent event : listAppender.list) {
            String logMsg = event.getFormattedMessage();
            assertFalse(logMsg.contains(sensitiveCode), "失败日志亦不得包含认证 code");
            assertFalse(logMsg.contains(sensitiveState), "失败日志亦不得包含认证 state");
            assertFalse(logMsg.contains("authCallback"), "失败日志不得包含 callback 全量对象");
        }
    }
}
