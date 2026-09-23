package cn.iocoder.yudao.framework.tenant.core.security;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TenantSecurityWebFilterTest {

    @Mock
    private TenantFrameworkService tenantFrameworkService;
    @Mock
    private GlobalExceptionHandler globalExceptionHandler;
    @Mock
    private FilterChain filterChain;

    private TenantProperties tenantProperties;
    private WebProperties webProperties;
    private TenantSecurityWebFilter filter;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();

        tenantProperties = new TenantProperties();
        webProperties = new WebProperties();

        filter = new TenantSecurityWebFilter(
                webProperties,
                tenantProperties,
                tenantProperties.getIgnoreUrls(),
                globalExceptionHandler,
                tenantFrameworkService
        );
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCase1_DefaultTenant_NoHeader_NoUser_NormalUrl() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertEquals(1L, TenantContextHolder.getTenantId());
            assertFalse(TenantContextHolder.isIgnore());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        verify(tenantFrameworkService).validTenant(1L);
        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCase2_DefaultTenant_NoHeader_UserTenant2() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);

        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(1);
        loginUser.setTenantId(2L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertEquals(2L, TenantContextHolder.getTenantId());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        verify(tenantFrameworkService).validTenant(2L);
        verify(filterChain).doFilter(request, response);
        assertNotEquals(403, response.getStatus());
    }

    @Test
    public void testCase3_DefaultTenant_Header1_UserTenant2() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);
        TenantContextHolder.setTenantId(1L);

        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(1);
        loginUser.setTenantId(2L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(response.getContentAsString().contains("您无权访问该租户的数据"));
    }

    @Test
    public void testCase4_DefaultTenantNull_NoHeader_NoUser_NormalUrl() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(response.getContentAsString().contains("请求的租户标识未传递"));
    }

    @Test
    public void testCase5_DefaultTenant_NoHeader_NoUser_IgnoreUrl() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);
        tenantProperties.getIgnoreUrls().add("/admin-api/system/auth/login");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin-api/system/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertTrue(TenantContextHolder.isIgnore());
            assertNull(TenantContextHolder.getTenantId());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        verify(tenantFrameworkService, never()).validTenant(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testCase6_DefaultTenant_Header2_NoUser() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);
        TenantContextHolder.setTenantId(2L);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertEquals(2L, TenantContextHolder.getTenantId());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        verify(tenantFrameworkService).validTenant(2L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testCase7_DefaultTenant_NoHeader_UserTenantNull() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);

        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(1);
        loginUser.setTenantId(null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertEquals(1L, TenantContextHolder.getTenantId());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        verify(tenantFrameworkService).validTenant(1L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testCase8_DefaultTenant_Header1_UserTenantNull() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);
        TenantContextHolder.setTenantId(1L);

        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(1);
        loginUser.setTenantId(null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertEquals(1L, TenantContextHolder.getTenantId());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        verify(tenantFrameworkService).validTenant(1L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testCase9_DefaultTenant_Header2_UserTenantNull() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);
        TenantContextHolder.setTenantId(2L);

        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(1);
        loginUser.setTenantId(null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(response.getContentAsString().contains("您无权访问该租户的数据"));
    }

    @Test
    public void testCase10_DefaultTenant_NoHeader_UserTenant0() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(1L);

        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(1);
        loginUser.setTenantId(0L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertEquals(1L, TenantContextHolder.getTenantId());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        verify(tenantFrameworkService).validTenant(1L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testCase11_DefaultTenantNull_NoHeader_UserTenantNull_NormalUrl() throws ServletException, IOException {
        tenantProperties.setDefaultTenantId(null);

        LoginUser loginUser = new LoginUser();
        loginUser.setId(10L);
        loginUser.setUserType(1);
        loginUser.setTenantId(null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertTrue(response.getContentAsString().contains("请求的租户标识未传递"));
    }

}
