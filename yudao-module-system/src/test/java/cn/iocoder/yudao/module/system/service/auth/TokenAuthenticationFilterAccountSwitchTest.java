package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.filter.TokenAuthenticationFilter;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TokenAuthenticationFilterAccountSwitchTest {

    private SecurityProperties securityProperties;
    private GlobalExceptionHandler globalExceptionHandler;
    private ApiErrorLogCommonApi apiErrorLogApi;
    private OAuth2TokenCommonApi oauth2TokenApi;
    private TokenAuthenticationFilter filter;
    private AccountImpersonationService impersonationService;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private OAuth2TokenService tokenService;
    private AdminUserService userService;
    private PermissionService permissionService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        WebProperties webProperties = new WebProperties();
        new WebFrameworkUtils(webProperties);

        securityProperties = new SecurityProperties();
        securityProperties.setTokenHeader("Authorization");
        securityProperties.setTokenParameter("token");

        apiErrorLogApi = mock(ApiErrorLogCommonApi.class);
        globalExceptionHandler = new GlobalExceptionHandler("test", apiErrorLogApi);

        oauth2TokenApi = mock(OAuth2TokenCommonApi.class);
        filter = new TokenAuthenticationFilter(securityProperties, globalExceptionHandler, oauth2TokenApi);

        impersonationService = new AccountImpersonationService();
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenService = mock(OAuth2TokenService.class);
        userService = mock(AdminUserService.class);
        permissionService = mock(PermissionService.class);

        ReflectionTestUtils.setField(impersonationService, "redis", redisTemplate);
        ReflectionTestUtils.setField(impersonationService, "tokenService", tokenService);
        ReflectionTestUtils.setField(impersonationService, "userService", userService);
        ReflectionTestUtils.setField(impersonationService, "permissionService", permissionService);
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    void normalInvalidTokenReturnsNullAndContinuesFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/admin-api/system/user/get");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = mock(MockFilterChain.class);

        when(oauth2TokenApi.checkAccessToken("invalid-token"))
                .thenThrow(new ServiceException(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "访问令牌不存在"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityFrameworkUtils.getLoginUser());
        assertEquals(200, response.getStatus());
        // 普通无效令牌不中断过滤器，链条继续执行（让 Spring Security 决定是否匿名访问）
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void accountSwitchSessionInvalidReturns419AndStopsFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/admin-api/system/user/get");
        request.addHeader("Authorization", "Bearer target-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = mock(MockFilterChain.class);

        // Redis 中无切换会话记录（null）
        when(valueOperations.get("money:account-impersonation:v1:target-token")).thenReturn(null);

        // tokenApi 边界真实调用 AccountImpersonationService.validate 产生真实 419 ServiceException
        when(oauth2TokenApi.checkAccessToken("target-token")).thenAnswer(invocation -> {
            OAuth2AccessTokenDO targetDO = new OAuth2AccessTokenDO();
            targetDO.setAccessToken("target-token");
            targetDO.setUserId(99L);
            targetDO.setUserType(UserTypeEnum.ADMIN.getValue());
            targetDO.setTenantId(1L);
            targetDO.setScopes(Collections.singletonList(OAuth2TokenCommonApi.ACCOUNT_IMPERSONATION_SCOPE));
            targetDO.setExpiresTime(LocalDateTime.now().plusHours(1));

            impersonationService.validate(targetDO, "target-token");
            return new OAuth2AccessTokenCheckRespDTO();
        });

        filter.doFilter(request, response, chain);

        assertNull(SecurityFrameworkUtils.getLoginUser());
        // 过滤器链未继续
        verify(chain, never()).doFilter(request, response);
        // 真实 GlobalExceptionHandler 序列化结果为 code:419
        assertTrue(response.getContentAsString().contains("\"code\":419"), "Expected code 419 in response");
        assertTrue(response.getContentAsString().contains("账号切换会话无效或已过期"), "Expected safety message in response");
    }

    @Test
    void normalAccessDeniedExceptionReturns403AndStopsFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin-api/system/user/profile/update-password");
        request.setServletPath("/admin-api/system/user/profile/update-password");
        request.addHeader("Authorization", "Bearer impersonation-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = mock(MockFilterChain.class);

        OAuth2AccessTokenCheckRespDTO respDTO = new OAuth2AccessTokenCheckRespDTO();
        respDTO.setUserId(99L);
        respDTO.setUserType(UserTypeEnum.ADMIN.getValue());
        respDTO.setTenantId(1L);
        respDTO.setScopes(Collections.singletonList(OAuth2TokenCommonApi.ACCOUNT_IMPERSONATION_SCOPE));
        when(oauth2TokenApi.checkAccessToken("impersonation-token")).thenReturn(respDTO);

        filter.doFilter(request, response, chain);

        assertNull(SecurityFrameworkUtils.getLoginUser());
        verify(chain, never()).doFilter(request, response);
        assertTrue(response.getContentAsString().contains("\"code\":403"), "Expected code 403 for restricted path");
    }
}
