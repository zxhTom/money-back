-- ============================================================
-- 告警规则 + 告警说明 Web 菜单（挂在已有「安全监控」目录下）
-- 支持重复执行
-- ============================================================

-- 取安全监控目录 ID
SET @security_dir_id = (
  SELECT id FROM system_menu WHERE name='安全监控' AND type=1 AND deleted=0 LIMIT 1
);

-- 告警规则配置页
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '告警规则', '', 2, 10, @security_dir_id, 'alert-rule', 'ep:setting',
  'custom/security/alertRule/index', 'CustomSecurityAlertRule',
  0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE component_name='CustomSecurityAlertRule' AND deleted=0);

SET @alert_rule_menu_id = (SELECT id FROM system_menu WHERE component_name='CustomSecurityAlertRule' AND deleted=0 LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '告警规则查询', 'custom:security:alert:query', 3, 1, @alert_rule_menu_id, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE parent_id=@alert_rule_menu_id AND permission='custom:security:alert:query' AND deleted=0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '告警规则配置', 'custom:security:alert:handle', 3, 2, @alert_rule_menu_id, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE parent_id=@alert_rule_menu_id AND permission='custom:security:alert:handle' AND deleted=0);

-- 告警类型说明页
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '告警类型说明', '', 2, 11, @security_dir_id, 'alert-types', 'ep:info-filled',
  'custom/security/alertTypes/index', 'CustomSecurityAlertTypes',
  0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE component_name='CustomSecurityAlertTypes' AND deleted=0);

-- 授权给超级管理员（role_id=1）
SET @alert_types_menu_id = (SELECT id FROM system_menu WHERE component_name='CustomSecurityAlertTypes' AND deleted=0 LIMIT 1);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 1, @alert_rule_menu_id, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id=1 AND menu_id=@alert_rule_menu_id AND deleted=0);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 1, @alert_types_menu_id, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id=1 AND menu_id=@alert_types_menu_id AND deleted=0);
