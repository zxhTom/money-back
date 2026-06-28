-- ============================================================
-- Web 管理端「小程序功能权限」菜单（挂在系统管理 id=1 下）
-- 支持重复执行：跳过已存在的记录
-- ============================================================

-- 1. 插入页面菜单（若不存在）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '小程序功能权限', '', 2, 20, 1, 'mini-permission', 'ep:mobile-phone', 'system/miniPermission/index', 'SystemMiniPermission', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE component_name = 'SystemMiniPermission' AND deleted = 0);

-- 2. 取菜单 ID（无论是刚插入还是已存在）
SET @mini_perm_menu_id = (SELECT id FROM system_menu WHERE component_name = 'SystemMiniPermission' AND deleted = 0 LIMIT 1);

-- 3. 子权限按钮（若不存在）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '查询小程序资源', 'system:role:query', 3, 1, @mini_perm_menu_id, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE parent_id = @mini_perm_menu_id AND permission = 'system:role:query' AND deleted = 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '分配小程序权限', 'system:mini-permission:assign', 3, 2, @mini_perm_menu_id, '', '', '', '', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE parent_id = @mini_perm_menu_id AND permission = 'system:mini-permission:assign' AND deleted = 0);

-- 4. 授权给超级管理员角色（若未授权）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 1, @mini_perm_menu_id, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = @mini_perm_menu_id AND deleted = 0);
