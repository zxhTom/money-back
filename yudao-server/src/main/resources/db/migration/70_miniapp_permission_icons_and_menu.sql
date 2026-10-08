-- ----------------------------
-- 70_miniapp_permission_icons_and_menu.sql
-- 修复小程序功能资源的图标，并自动化安装 Web 端「小程序功能权限」管理菜单
-- ----------------------------

-- 1. 为小程序功能资源 (type=4) 补齐 Element Plus 默认图标（若 icon 为空）
UPDATE system_menu SET icon = 'ep:experiment' WHERE permission = 'mini:test:mode' AND (icon IS NULL OR icon = '' OR icon = '#');
UPDATE system_menu SET icon = 'ep:data-board' WHERE permission = 'mini:admin:dashboard' AND (icon IS NULL OR icon = '' OR icon = '#');
UPDATE system_menu SET icon = 'ep:warning-filled' WHERE permission = 'mini:admin:security:alert' AND (icon IS NULL OR icon = '' OR icon = '#');
UPDATE system_menu SET icon = 'ep:user-filled' WHERE permission = 'mini:admin:security:blacklist' AND (icon IS NULL OR icon = '' OR icon = '#');
UPDATE system_menu SET icon = 'ep:aim' WHERE permission = 'mini:admin:security:diagnose' AND (icon IS NULL OR icon = '' OR icon = '#');

-- 2. 插入 Web 端管理菜单 [小程序功能权限] (挂载在 "系统管理" parent_id = 1 下)
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '小程序功能权限', '', 2, 20, 1, 'mini-permission', 'ep:mobile-phone', 'system/miniPermission/index', 'SystemMiniPermission', 0, 1, 0, 0, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE component_name = 'SystemMiniPermission' AND deleted = 0);

-- 3. 授权给超级管理员角色 (role_id = 1)
SET @mini_perm_menu_id = (SELECT id FROM system_menu WHERE component_name = 'SystemMiniPermission' AND deleted = 0 LIMIT 1);
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 1, @mini_perm_menu_id, '1', NOW(), '1', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = @mini_perm_menu_id AND deleted = 0);
