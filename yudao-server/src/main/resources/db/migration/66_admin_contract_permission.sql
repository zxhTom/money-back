-- 插入小程序权限字典值（如果通过字典控制）或插入系统菜单
-- 这里假设存在 system_menu 来管理小程序菜单权限。
INSERT INTO system_menu (
    name, permission, type, sort, parent_id, path, icon, component, status, creator, updater, create_time, update_time, deleted
) VALUES (
    '小程序全部合同查询', 'mini:admin:contract:list', 3, 10, 0, '', '', '', 0, '1', '1', NOW(), NOW(), 0
);
