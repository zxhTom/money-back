-- ============================================================
-- IP 诊断权限按钮（MySQL）
-- 前置条件：02_menu.sql 已执行，「安全监控」目录已存在
-- ============================================================

SET @security_dir_id = (SELECT id FROM system_menu WHERE path = 'security-monitor' AND deleted = 0 LIMIT 1);

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  'IP诊断', 'custom:security:ip-diagnose', 3, 5, @security_dir_id,
  '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0
);
