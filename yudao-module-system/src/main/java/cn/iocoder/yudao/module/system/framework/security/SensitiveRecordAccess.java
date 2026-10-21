package cn.iocoder.yudao.module.system.framework.security;

import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/** History permissions grant self-service access; management roles may inspect other users. */
@Component
public class SensitiveRecordAccess {
    @Resource private SecurityFrameworkService security;
    private Long caller() {
        Long id = SecurityFrameworkUtils.getLoginUserId();
        if (id == null) throw new AccessDeniedException("请先登录");
        return id;
    }
    public boolean administrator() {
        caller();
        return security.hasAnyRoles("super_admin", "tenant_admin", "contract-manager");
    }
    public Long userFilter(Long requested) {
        Long own = caller();
        if (administrator()) return requested;
        if (requested != null && !own.equals(requested)) throw new AccessDeniedException("只能查看本人的改密记录");
        return own;
    }
    public String tenantUserIdsSql() {
        caller();
        if (security.hasAnyRoles("super_admin")) return null; // Existing platform-wide administrator audit.
        Long tenant = TenantContextHolder.getRequiredTenantId();
        // tenant is a server-side Long, never a request SQL fragment. Include soft-deleted users' history.
        return "SELECT id FROM system_users WHERE tenant_id = " + tenant;
    }
}
