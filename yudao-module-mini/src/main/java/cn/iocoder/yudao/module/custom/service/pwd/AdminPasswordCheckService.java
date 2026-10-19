package cn.iocoder.yudao.module.custom.service.pwd;

import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.sql.DataSource;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/** Read-only administrator tool. Never invokes authentication, payment or password update services. */
@Service
public class AdminPasswordCheckService {
    private final JdbcTemplate jdbc;
    private final AdminUserService users;
    private final SecurityFrameworkService security;
    public AdminPasswordCheckService(DataSource dataSource, AdminUserService users, SecurityFrameworkService security) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.users = users;
        this.security = security;
    }
    private void requireAdmin() {
        if (!security.hasAnyRoles("super_admin", "tenant_admin")) throw new AccessDeniedException("仅管理员可核验密码");
    }
    public List<Map<String, String>> search(String keyword) {
        requireAdmin();
        String term = keyword == null ? "" : keyword.trim();
        if (term.length() > 64) throw exception(new ErrorCode(400, "用户名过长"));
        // Only return display fields. Explicit tenant predicate also applies to super administrators.
        return jdbc.query("SELECT username,nickname FROM system_users WHERE tenant_id=? AND deleted=0 "
                + "AND (LOCATE(?,username)>0 OR LOCATE(?,nickname)>0) ORDER BY username LIMIT 20",
                (rs, n) -> {
                    Map<String,String> item = new LinkedHashMap<>();
                    item.put("username",rs.getString("username")); item.put("nickname",rs.getString("nickname"));
                    return item;
                }, TenantContextHolder.getRequiredTenantId(), term, term);
    }
    public boolean verify(String username, String password, String kind) {
        requireAdmin();
        if (!StringUtils.hasText(username) || username.trim().length() > 64
                || password == null || password.isEmpty() || password.length() > 72
                || !("login".equals(kind) || "pay".equals(kind))) {
            throw exception(new ErrorCode(400, "请选择用户、密码类型并输入有效密码"));
        }
        // Fixed column names only. JDBC avoids MyBatis StdOutImpl logging credential hashes in result rows.
        String column = "login".equals(kind) ? "password" : "pay_password";
        List<String> hashes = jdbc.query("SELECT " + column + " FROM system_users "
                        + "WHERE tenant_id=? AND deleted=0 AND username=? LIMIT 2",
                (rs,n) -> rs.getString(1), TenantContextHolder.getRequiredTenantId(), username.trim());
        return hashes.size() == 1 && StringUtils.hasText(hashes.get(0)) && users.isPasswordMatch(password, hashes.get(0));
    }
}
