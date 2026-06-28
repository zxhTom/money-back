-- ============================================================
-- 小程序功能权限资源（system_menu type=4）
-- 支持重复执行：跳过已存在的记录
-- ============================================================

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '测试功能', 'mini:test:mode', 4, 1, 0, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'mini:test:mode' AND deleted = 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '数据大盘', 'mini:admin:dashboard', 4, 2, 0, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'mini:admin:dashboard' AND deleted = 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '安全告警', 'mini:admin:security:alert', 4, 3, 0, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'mini:admin:security:alert' AND deleted = 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 'IP黑名单', 'mini:admin:security:blacklist', 4, 4, 0, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'mini:admin:security:blacklist' AND deleted = 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 'IP诊断', 'mini:admin:security:diagnose', 4, 5, 0, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'mini:admin:security:diagnose' AND deleted = 0);
