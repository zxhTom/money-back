-- ============================================================
-- 全局诊断页面菜单及权限（MySQL）
-- 前置条件：02_menu.sql 已执行，「安全监控」目录已存在
-- ============================================================

SET @security_dir_id = (SELECT id FROM system_menu WHERE path = 'security-monitor' AND deleted = 0 LIMIT 1);

-- 全局诊断页面（菜单项）
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '全局诊断', '', 2, 6, @security_dir_id,
  'diagnose', 'ep:data-analysis', 'custom/security/diagnose/index', 'CustomSecurityGlobalDiagnose',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @diagnose_menu_id = LAST_INSERT_ID();

-- 操作权限按钮
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '全局诊断-查询', 'custom:security:global-diagnose', 3, 1, @diagnose_menu_id,
  '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0
);
