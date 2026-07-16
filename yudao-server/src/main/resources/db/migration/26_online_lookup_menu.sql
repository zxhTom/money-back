-- 在线 IP/用户 互查页面（挂在 安全监控 5083 下）
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive)
VALUES (5188, '在线互查', 'custom:security:online-lookup', 2, 60, 5083, 'online-lookup', 'ep:search', 'custom/security/onlineLookup/index', 'OnlineLookup', 0, b'1', b'1');
