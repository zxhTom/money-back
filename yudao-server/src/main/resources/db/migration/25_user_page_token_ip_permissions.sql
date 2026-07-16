-- 用户列表页「令牌」「IP记录」按钮独立授权资源，挂到 用户管理(100) 下，可在角色菜单权限勾选
-- 令牌查看：原复用 system:oauth2-token:page(在"令牌管理"菜单下不易找)；IP记录：原复用 system:user:query
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive)
VALUES
 (5186, '令牌查看',   'system:user:token-view', 3, 11, 100, '', '', NULL, 0, b'1', b'1'),
 (5187, 'IP记录查看', 'system:user:ip-history', 3, 12, 100, '', '', NULL, 0, b'1', b'1');
