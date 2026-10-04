package cn.iocoder.yudao.module.system.controller.admin.monitor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.annotation.security.PermitAll;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin-api/custom/security/fix")
public class FixPermissionController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/run")
    @PermitAll
    public String run() {
        try {
            // Find the menu IDs
            List<Long> menuIds = jdbcTemplate.queryForList("SELECT id FROM system_menu WHERE permission IN ('custom:security:alert:query', 'custom:security:alert:handle', 'mini:admin:security:alert')", Long.class);
            if (menuIds.isEmpty()) {
                // If the mini permission isn't there, just find the alert ones
                menuIds = jdbcTemplate.queryForList("SELECT id FROM system_menu WHERE permission LIKE 'custom:security:alert%'", Long.class);
            }
            if (menuIds.isEmpty()) return "Menus not found";
            
            // Add to tenant_admin role (role_id = 165) and tenant 1
            for (Long menuId : menuIds) {
                int count = jdbcTemplate.queryForObject("SELECT count(*) FROM system_role_menu WHERE role_id = 165 AND menu_id = ?", Integer.class, menuId);
                if (count == 0) {
                    jdbcTemplate.update("INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES(165, ?, 1, '1', NOW(), '1', NOW(), 0)", menuId);
                }
            }
            
            return "Fixed permissions for role 165. Added menu IDs: " + menuIds;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
