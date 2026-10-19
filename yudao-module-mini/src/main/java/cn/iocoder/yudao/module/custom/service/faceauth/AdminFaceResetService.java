package cn.iocoder.yudao.module.custom.service.faceauth;

import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.util.List;

/** Administrator action: only resets verification, never grants it. */
@Service
public class AdminFaceResetService {
    private final JdbcTemplate jdbc;
    private final SecurityFrameworkService security;
    public AdminFaceResetService(DataSource dataSource, SecurityFrameworkService security) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.security = security;
    }
    @Transactional(rollbackFor = Exception.class)
    public void reset(String username) {
        if (!security.hasAnyRoles("super_admin", "tenant_admin"))
            throw new AccessDeniedException("仅管理员可要求重新认证");
        if (username == null || username.trim().isEmpty() || username.trim().length() > 64)
            throw exception(new ErrorCode(400, "请输入有效用户名"));
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        List<Long> ids = jdbc.query("SELECT id FROM system_users WHERE tenant_id=? AND deleted=0 AND username=? LIMIT 2 FOR UPDATE",
                (rs, n) -> rs.getLong("id"), tenantId, username.trim());
        if (ids.size() != 1) throw exception(new ErrorCode(400, "用户不存在或不唯一，请重新选择"));
        jdbc.update("UPDATE system_users SET verified=0,update_time=CURRENT_TIMESTAMP WHERE id=? AND tenant_id=? AND deleted=0",
                ids.get(0), tenantId);
    }
}
