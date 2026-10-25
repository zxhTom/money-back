package cn.iocoder.yudao.module.infra.framework.security.core;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

@Component("platformFileConfigGuard")
@Slf4j
public class PlatformFileConfigGuard {

    private static final String ROLE_SUPER_ADMIN = "super_admin";

    @Autowired(required = false)
    private TenantProperties tenantProperties;

    @Value("${yudao.tenant.default-tenant-id:#{null}}")
    private Long defaultTenantIdProperty;

    @Resource
    private SecurityFrameworkService securityFrameworkService;

    public boolean isPlatformSuperAdmin() {
        return isPlatformAdminWithRoles(ROLE_SUPER_ADMIN);
    }

    public boolean isPlatformFileConfigAdmin() {
        return isPlatformAdminWithRoles(ROLE_SUPER_ADMIN, "tenant_admin", "contract-manager");
    }

    private boolean isPlatformAdminWithRoles(String... roles) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null || loginUser.getId() == null) {
            return false;
        }

        // 限定为管理员类型
        if (!Objects.equals(UserTypeEnum.ADMIN.getValue(), loginUser.getUserType())) {
            return false;
        }

        // 优先从 tenantProperties 读取，缺失时从 property 属性读取
        Long defaultTenantId = tenantProperties != null && tenantProperties.getDefaultTenantId() != null
                ? tenantProperties.getDefaultTenantId() : defaultTenantIdProperty;
        if (defaultTenantId == null) {
            log.warn("[PlatformGuard] 未配置 defaultTenantId，默认拒绝");
            return false;
        }

        // token tenant 缺失或不匹配拒绝
        Long tokenTenantId = loginUser.getTenantId();
        if (tokenTenantId == null || !Objects.equals(defaultTenantId, tokenTenantId)) {
            return false;
        }

        // 拒绝跨租户访问态，防止绕过权限检查
        if (loginUser.getVisitTenantId() != null && ObjUtil.notEqual(loginUser.getVisitTenantId(), tokenTenantId)) {
            log.warn("[PlatformGuard] 拒绝跨租户访问态访问平台配置 visitTenantId={}", loginUser.getVisitTenantId());
            return false;
        }

        // 上下文租户如果存在，必须与平台默认租户一致，错配拒绝
        Long contextTenantId = cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId();
        if (contextTenantId != null && !Objects.equals(defaultTenantId, contextTenantId)) {
            log.warn("[PlatformGuard] 租户上下文错配 contextTenantId={} defaultTenantId={}", contextTenantId, defaultTenantId);
            return false;
        }

        // 检查请求头租户并要求严格匹配
        HttpServletRequest request = WebFrameworkUtils.getRequest();
        if (request != null) {
            String headerTenant = request.getHeader(WebFrameworkUtils.HEADER_TENANT_ID);
            if (StrUtil.isNotBlank(headerTenant)) {
                try {
                    Long headerTenantId = Long.parseLong(headerTenant.trim());
                    if (!Objects.equals(defaultTenantId, headerTenantId)) {
                        log.warn("[PlatformGuard] header tenant-id({}) 不匹配", headerTenantId);
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }

        // 在平台租户上下文核验角色后恢复
        return TenantUtils.execute(defaultTenantId, () -> securityFrameworkService.hasAnyRoles(roles));
    }

    public void requirePlatformSuperAdmin() {
        if (!isPlatformSuperAdmin()) {
            throw exception0(403, "需要平台管理员权限");
        }
    }
    public void requirePlatformFileConfigAdmin() {
        if (!isPlatformFileConfigAdmin()) throw exception0(403, "需要平台租户的文件配置管理员权限");
    }

}
