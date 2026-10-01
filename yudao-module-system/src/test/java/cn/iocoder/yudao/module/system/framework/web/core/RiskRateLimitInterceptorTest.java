package cn.iocoder.yudao.module.system.framework.web.core;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskRateLimitConfigDO;
import cn.iocoder.yudao.module.system.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.risk.RiskLockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RiskRateLimitInterceptorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private RiskRateLimitInterceptor interceptor;

    @Mock
    private RiskLockService riskLockService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testPreHandle_OptionsMethod() {
        when(request.getMethod()).thenReturn("OPTIONS");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        verifyNoInteractions(riskLockService);
    }

    @Test
    public void testPreHandle_NotLoggedIn() {
        when(request.getMethod()).thenReturn("GET");
        SecurityContextHolder.clearContext();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        verifyNoInteractions(riskLockService);
    }

    @Test
    public void testPreHandle_UserAlreadyLocked() {
        when(request.getMethod()).thenReturn("GET");
        LoginUser loginUser = new LoginUser().setId(100L);
        SecurityFrameworkUtils.setLoginUser(loginUser, request);

        doThrow(new ServiceException(ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED))
                .when(riskLockService).checkLockStatus(100L);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                interceptor.preHandle(request, response, new Object()));
        assertEquals(ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED.getCode(), ex.getCode());
    }

    @Test
    public void testPreHandle_NoMatchingRules() {
        when(request.getMethod()).thenReturn("GET");
        LoginUser loginUser = new LoginUser().setId(100L);
        SecurityFrameworkUtils.setLoginUser(loginUser, request);
        when(request.getRequestURI()).thenReturn("/admin-api/trade/order");

        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .apiPattern("/admin-api/system/user/**")
                .timeWindow(60)
                .maxCount(10)
                .build();
        when(riskLockService.getAllEnableRateLimitRules()).thenReturn(Collections.singletonList(rule));

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        verify(riskLockService, never()).escalateLock(anyLong(), anyString(), any());
    }

    @Test
    public void testPreHandle_RoleMismatch() {
        when(request.getMethod()).thenReturn("GET");
        LoginUser loginUser = new LoginUser().setId(100L);
        SecurityFrameworkUtils.setLoginUser(loginUser, request);
        when(request.getRequestURI()).thenReturn("/admin-api/trade/order");

        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .apiPattern("/admin-api/trade/order")
                .roleId(2L)
                .timeWindow(60)
                .maxCount(10)
                .build();
        when(riskLockService.getAllEnableRateLimitRules()).thenReturn(Collections.singletonList(rule));
        when(permissionService.getUserRoleIdListByUserIdFromCache(100L)).thenReturn(new HashSet<>(Collections.singletonList(1L)));

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    public void testPreHandle_WithinLimit() {
        when(request.getMethod()).thenReturn("GET");
        LoginUser loginUser = new LoginUser().setId(100L);
        SecurityFrameworkUtils.setLoginUser(loginUser, request);
        when(request.getRequestURI()).thenReturn("/admin-api/trade/order");

        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .apiPattern("/admin-api/trade/order")
                .timeWindow(60)
                .maxCount(10)
                .build();
        when(riskLockService.getAllEnableRateLimitRules()).thenReturn(Collections.singletonList(rule));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("risk:api_limit:1:100")).thenReturn(1L);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);

        verify(stringRedisTemplate).expire("risk:api_limit:1:100", 60L, TimeUnit.SECONDS);
        verify(riskLockService, never()).escalateLock(anyLong(), anyString(), any());
    }

    @Test
    public void testPreHandle_LimitExceeded() {
        when(request.getMethod()).thenReturn("GET");
        LoginUser loginUser = new LoginUser().setId(100L);
        SecurityFrameworkUtils.setLoginUser(loginUser, request);
        when(request.getRequestURI()).thenReturn("/admin-api/trade/order");

        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .apiPattern("/admin-api/trade/order")
                .timeWindow(60)
                .maxCount(10)
                .lockDurationLadder("300,3600,-1")
                .build();
        when(riskLockService.getAllEnableRateLimitRules()).thenReturn(Collections.singletonList(rule));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("risk:api_limit:1:100")).thenReturn(11L);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                interceptor.preHandle(request, response, new Object()));
        assertEquals(ErrorCodeConstants.USER_RATE_LIMIT_LOCKED.getCode(), ex.getCode());

        verify(riskLockService).escalateLock(100L, "RATE_LIMIT", rule);
    }
}
