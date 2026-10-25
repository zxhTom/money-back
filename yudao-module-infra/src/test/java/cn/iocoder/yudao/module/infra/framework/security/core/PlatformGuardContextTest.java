package cn.iocoder.yudao.module.infra.framework.security.core;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PlatformGuardContextTest {

    private static class FakeSecurityFrameworkService implements SecurityFrameworkService {
        private boolean superAdminRole = false;

        public void setSuperAdminRole(boolean superAdminRole) {
            this.superAdminRole = superAdminRole;
        }

        @Override
        public boolean hasPermission(String permission) { return false; }
        @Override
        public boolean hasAnyPermissions(String... permissions) { return false; }
        @Override
        public boolean hasRole(String role) { return "super_admin".equals(role) && superAdminRole; }
        @Override
        public boolean hasAnyRoles(String... roles) {
            for (String role : roles) {
                if ("super_admin".equals(role) && superAdminRole) return true;
            }
            return false;
        }
        @Override
        public boolean hasScope(String scope) { return false; }
        @Override
        public boolean hasAnyScopes(String... scope) { return false; }
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(FakeSecurityFrameworkService.class, FakeSecurityFrameworkService::new)
            .withUserConfiguration(PlatformFileConfigGuard.class);

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    private void login(Long userId, Integer userType, Long tenantId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(userId);
        loginUser.setUserType(userType);
        loginUser.setTenantId(tenantId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
        TenantContextHolder.setTenantId(tenantId);
    }

    @Test
    public void testGuardLoadsSuccessfullyWithoutTenantPropertiesBean() {
        contextRunner
                .withPropertyValues("yudao.tenant.enable=false", "yudao.tenant.default-tenant-id=1")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(PlatformFileConfigGuard.class);
                });
    }

    @Test
    public void testGuardLoadsSuccessfullyWithDefaultDisabledAndNoProperty() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(PlatformFileConfigGuard.class);
                });
    }

    @Test
    public void testGuardRejectsWhenDefaultTenantIdMissingInDisabledTenant() {
        // 验证默认租户缺失时 fail-closed 拒绝
        contextRunner
                .withPropertyValues("yudao.tenant.enable=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    PlatformFileConfigGuard guard = context.getBean(PlatformFileConfigGuard.class);
                    FakeSecurityFrameworkService securityService = context.getBean(FakeSecurityFrameworkService.class);
                    securityService.setSuperAdminRole(true);

                    // 即使登录了并且角色为 super_admin，但由于系统缺失 defaultTenantId 定义，必须 fail-closed 拒绝
                    login(1L, UserTypeEnum.ADMIN.getValue(), 1L);
                    assertThat(guard.isPlatformSuperAdmin()).isFalse();
                });
    }

    @Test
    public void testGuardAcceptsPlatformAdminWhenDefaultTenantConfigured() {
        // 验证默认租户存在时平台管理员访问成功与非平台身份拒绝
        contextRunner
                .withPropertyValues("yudao.tenant.enable=false", "yudao.tenant.default-tenant-id=1")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    PlatformFileConfigGuard guard = context.getBean(PlatformFileConfigGuard.class);
                    FakeSecurityFrameworkService securityService = context.getBean(FakeSecurityFrameworkService.class);

                    // 1. 未登录：拒绝
                    assertThat(guard.isPlatformSuperAdmin()).isFalse();

                    // 2. 登录非 ADMIN 用户类型：拒绝
                    login(2L, UserTypeEnum.MEMBER.getValue(), 1L);
                    assertThat(guard.isPlatformSuperAdmin()).isFalse();

                    // 3. 登录租户2管理员（非平台默认租户1）：拒绝
                    login(3L, UserTypeEnum.ADMIN.getValue(), 2L);
                    securityService.setSuperAdminRole(true);
                    assertThat(guard.isPlatformSuperAdmin()).isFalse();

                    // 4. 登录平台租户1管理员但无 super_admin 角色：拒绝
                    login(4L, UserTypeEnum.ADMIN.getValue(), 1L);
                    securityService.setSuperAdminRole(false);
                    assertThat(guard.isPlatformSuperAdmin()).isFalse();

                    // 5. 登录平台租户1管理员且拥有 super_admin 角色：成功允许
                    securityService.setSuperAdminRole(true);
                    assertThat(guard.isPlatformSuperAdmin()).isTrue();
                });
    }
}
