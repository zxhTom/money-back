package cn.iocoder.yudao.module.infra.framework.security.core;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkServiceImpl;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.infra.controller.admin.file.FileConfigController;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileConfigMapper;
import cn.iocoder.yudao.module.infra.service.file.FileConfigServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlatformFileConfigSecurityTest {

    private PlatformFileConfigGuard platformGuard;
    private SecurityFrameworkService securityService;
    private TenantProperties tenantProperties;
    private FileConfigServiceImpl fileConfigService;
    private FileConfigMapper fileConfigMapper;

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void login(Long userId, Integer userType, Long tenantId, Long visitTenantId) {
        LoginUser user = new LoginUser();
        user.setId(userId);
        user.setUserType(userType);
        user.setTenantId(tenantId);
        user.setVisitTenantId(visitTenantId);
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
        inject(platformGuard, "defaultTenantIdProperty", 1L);

        fileConfigService = new FileConfigServiceImpl();
        fileConfigMapper = mock(FileConfigMapper.class);
        inject(fileConfigService, "fileConfigMapper", fileConfigMapper);
        inject(fileConfigService, "platformFileConfigGuard", platformGuard);
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void testPlatformSuperAdminAllowedOnDefaultTenant() {
        login(1L, UserTypeEnum.ADMIN.getValue(), 1L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(true);

        assertTrue(platformGuard.isPlatformSuperAdmin());
        assertDoesNotThrow(() -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testTenantDisabledAllowsAdminViaProperty() throws Exception {
        // tenantProperties bean 为 null (多租户未开启)，但配置了 property = 1L
        inject(platformGuard, "tenantProperties", null);
        inject(platformGuard, "defaultTenantIdProperty", 1L);

        login(1L, UserTypeEnum.ADMIN.getValue(), 1L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(true);

        assertTrue(platformGuard.isPlatformSuperAdmin());
        assertDoesNotThrow(() -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testMissingBeanAndPropertyFailsClosed() throws Exception {
        // bean 与 property 均缺失时直接 fail-closed
        inject(platformGuard, "tenantProperties", null);
        inject(platformGuard, "defaultTenantIdProperty", null);

        login(1L, UserTypeEnum.ADMIN.getValue(), 1L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(true);

        assertFalse(platformGuard.isPlatformSuperAdmin());
        assertThrows(ServiceException.class, () -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testVisitTenantIdCrossTenantBypassRejected() {
        // 关键安全项：平台普通管理员通过 tenant:visit 进入跨租户访问态 (visitTenantId=2 != tokenTenant=1)
        login(1L, UserTypeEnum.ADMIN.getValue(), 1L, 2L);

        // 如果调用原生 SecurityFrameworkServiceImpl，skipPermissionCheck 会因为 visit!=token 返回 true
        cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi permissionApi =
                mock(cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi.class);
        when(permissionApi.hasAnyRoles(anyLong(), any())).thenReturn(false); // 实际数据库并无 super_admin 角色
        SecurityFrameworkServiceImpl realSs = new SecurityFrameworkServiceImpl(permissionApi);

        try {
            inject(platformGuard, "securityFrameworkService", realSs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 验证守卫拒绝跨租户访问态，即使真实的 skipPermissionCheck 返回 true 也不被利用绕过
        assertFalse(platformGuard.isPlatformSuperAdmin());
        assertThrows(ServiceException.class, () -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testTenantAdminWithSubPermissionsRejected() {
        login(10L, UserTypeEnum.ADMIN.getValue(), 2L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(false);

        assertFalse(platformGuard.isPlatformSuperAdmin());
        assertThrows(ServiceException.class, () -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testMemberUserWithSameIdAsAdminRejected() {
        login(1L, UserTypeEnum.MEMBER.getValue(), 1L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(true);

        assertFalse(platformGuard.isPlatformSuperAdmin());
        assertThrows(ServiceException.class, () -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testContextTenantMismatchRejected() {
        login(1L, UserTypeEnum.ADMIN.getValue(), 1L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(true);
        // TenantContextHolder 设为错配的 2L
        TenantContextHolder.setTenantId(2L);

        assertFalse(platformGuard.isPlatformSuperAdmin());
        assertThrows(ServiceException.class, () -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testForgedHeaderTenantRejected() {
        login(1L, UserTypeEnum.ADMIN.getValue(), 1L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("tenant-id", "2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertFalse(platformGuard.isPlatformSuperAdmin());
        assertThrows(ServiceException.class, () -> platformGuard.requirePlatformSuperAdmin());
    }

    @Test
    public void testServiceWriteAndTestGuarded() {
        login(10L, UserTypeEnum.ADMIN.getValue(), 2L, null);
        when(securityService.hasAnyRoles("super_admin")).thenReturn(false);

        FileConfigSaveReqVO req = new FileConfigSaveReqVO();
        req.setId(100L);
        assertThrows(ServiceException.class, () -> fileConfigService.createFileConfig(req));
        assertThrows(ServiceException.class, () -> fileConfigService.updateFileConfig(req));
        assertThrows(ServiceException.class, () -> fileConfigService.updateFileConfigMaster(100L));
        assertThrows(ServiceException.class, () -> fileConfigService.deleteFileConfig(100L));
        assertThrows(ServiceException.class, () -> fileConfigService.deleteFileConfigList(Collections.singletonList(100L)));
        assertThrows(ServiceException.class, () -> fileConfigService.testFileConfig(100L));
    }

    @Test
    public void testInternalReadAndMasterIdNotBlockedForNormalUpload() {
        login(999L, UserTypeEnum.MEMBER.getValue(), 2L, null);

        FileConfigDO mockConfig = new FileConfigDO();
        mockConfig.setId(28L);
        when(fileConfigMapper.selectById(28L)).thenReturn(mockConfig);
        when(fileConfigMapper.selectByMaster()).thenReturn(mockConfig);

        assertEquals(mockConfig, fileConfigService.getFileConfig(28L));
        assertEquals(28L, fileConfigService.getMasterFileConfigId());
    }
}
