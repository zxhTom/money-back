-- ============================================================
-- 数据反查页面菜单（挂在「数据访问」目录下）
-- 支持重复执行
-- ============================================================

-- 取安全监控目录 ID
SET @security_dir_id = (
  SELECT id FROM system_menu WHERE path='security-monitor' AND deleted=0 LIMIT 1
);

-- 插入「数据反查」页面菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '数据反查', '', 2, 20, @security_dir_id, 'data-access-reverse', 'ep:search',
  'custom/security/dataAccess/reverse/index', 'CustomDataAccessReverse',
  0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE component_name='CustomDataAccessReverse' AND deleted=0);

SET @reverse_menu_id = (SELECT id FROM system_menu WHERE component_name='CustomDataAccessReverse' AND deleted=0 LIMIT 1);

-- 查询权限按钮
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '数据反查查询', 'system:data-access:log:query', 3, 1, @reverse_menu_id, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE parent_id=@reverse_menu_id AND permission='system:data-access:log:query' AND deleted=0);

-- 授权给超级管理员（role_id=1）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 1, @reverse_menu_id, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id=1 AND menu_id=@reverse_menu_id AND deleted=0);
