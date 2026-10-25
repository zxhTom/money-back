package cn.iocoder.yudao.module.infra.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.infra.framework.security.core.PlatformAdminAuthorizationManager;
import cn.iocoder.yudao.module.infra.framework.security.core.PlatformFileConfigGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminMonitorSecurityTest {

    private PlatformFileConfigGuard platformGuard;
    private SecurityFrameworkService securityService;
    private TenantProperties tenantProperties;
    private MockedStatic<SpringUtil> springUtilMock;

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private void login(Long userId, Integer userType, Long tenantId) {
        LoginUser user = new LoginUser();
        user.setId(userId);
        user.setUserType(userType);
        user.setTenantId(tenantId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
        TenantContextHolder.setTenantId(tenantId);
    }

    @BeforeEach
    public void setup() throws Exception {
        platformGuard = new PlatformFileConfigGuard();
        securityService = mock(SecurityFrameworkService.class);
        tenantProperties = new TenantProperties();
        tenantProperties.setDefaultTenantId(1L);

        inject(platformGuard, "securityFrameworkService", securityService);
        inject(platformGuard, "tenantProperties", tenantProperties);

        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(PlatformFileConfigGuard.class)).thenReturn(platformGuard);
    }

    @AfterEach
    public void cleanup() {
        if (springUtilMock != null) {
            springUtilMock.close();
        }
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    public void testAnonymousAccessDeniedToMonitorPath() {
        PlatformAdminAuthorizationManager manager = new PlatformAdminAuthorizationManager();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        RequestAuthorizationContext context = new RequestAuthorizationContext(request);

        AuthorizationDecision decision = manager.check(() -> null, context);
        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    public void testNormalUserAccessDeniedToMonitorPath() {
        login(100L, UserTypeEnum.MEMBER.getValue(), 1L);
        PlatformAdminAuthorizationManager manager = new PlatformAdminAuthorizationManager();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/instances");
        RequestAuthorizationContext context = new RequestAuthorizationContext(request);

        AuthorizationDecision decision = manager.check(SecurityFrameworkUtils::getAuthentication, context);
        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    public void testTenantAdminAccessDeniedToMonitorPath() {
        login(10L, UserTypeEnum.ADMIN.getValue(), 2L);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(false);

        PlatformAdminAuthorizationManager manager = new PlatformAdminAuthorizationManager();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/instances");
        RequestAuthorizationContext context = new RequestAuthorizationContext(request);

        AuthorizationDecision decision = manager.check(SecurityFrameworkUtils::getAuthentication, context);
        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    public void testPlatformSuperAdminAllowedToMonitorPath() {
        login(1L, UserTypeEnum.ADMIN.getValue(), 1L);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(true);

        PlatformAdminAuthorizationManager manager = new PlatformAdminAuthorizationManager();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/instances");
        RequestAuthorizationContext context = new RequestAuthorizationContext(request);

        AuthorizationDecision decision = manager.check(SecurityFrameworkUtils::getAuthentication, context);
        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    public void testInvalidContextPathDoesNotRegisterWildcardOrAnyMatcher() throws Exception {
        String[] invalidPaths = new String[]{
                "", " ", "\t", "/", "/**", "/admin/**", "admin", "admin/test",
                "/admin/../evil", "/admin//instances", "/admin/*", "/admin/?"
        };

        for (String invalidPath : invalidPaths) {
            SecurityConfiguration configuration = new SecurityConfiguration();
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
                    mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl =
                    mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
            when(registry.requestMatchers(anyString())).thenReturn(authorizedUrl);
            when(authorizedUrl.permitAll()).thenReturn(registry);
            when(authorizedUrl.access(any())).thenReturn(registry);

            inject(configuration, "adminSeverContextPath", invalidPath);
            AuthorizeRequestsCustomizer customizer = configuration.authorizeRequestsCustomizer();
            WebProperties webProps = new WebProperties();
            webProps.getAdminApi().setPrefix("/admin-api");
            inject(customizer, "webProperties", webProps);
            customizer.customize(registry);

            verify(registry, never()).requestMatchers(eq(invalidPath));
            verify(registry, never()).requestMatchers(eq(invalidPath + "/**"));
        }
    }

    @Test
    public void testValidFixedAbsolutePathRegistersMatcher() throws Exception {
        SecurityConfiguration configuration = new SecurityConfiguration();
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
                mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl =
                mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);

        when(registry.requestMatchers(anyString())).thenReturn(authorizedUrl);
        when(authorizedUrl.permitAll()).thenReturn(registry);
        when(authorizedUrl.access(any())).thenReturn(registry);

        inject(configuration, "adminSeverContextPath", "/admin");
        AuthorizeRequestsCustomizer customizer = configuration.authorizeRequestsCustomizer();
        WebProperties webProps = new WebProperties();
        webProps.getAdminApi().setPrefix("/admin-api");
        inject(customizer, "webProperties", webProps);
        customizer.customize(registry);

        verify(registry).requestMatchers(eq("/admin"));
        verify(registry).requestMatchers(eq("/admin/**"));
    }
}
