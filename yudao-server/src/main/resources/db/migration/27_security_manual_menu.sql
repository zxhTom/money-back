-- 系统使用手册 / 风控须知 页面（挂在 安全监控 5083 下）
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive)
VALUES (5189, '使用手册', 'custom:security:manual', 2, 5, 5083, 'security-manual', 'ep:reading', 'custom/security/manual/index', 'SecurityManual', 0, b'1', b'1');
