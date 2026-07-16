-- 信用查询改用独立权限 custom:contract:credit-query（默认不授予任何角色 = 默认拒绝）
-- 原 custom:contract:credit 被默认角色 contract 持有，导致所有小程序用户可按身份证查任意人信用，属越权/信息泄漏
-- 新增资源后：仅超级管理员默认可用；管理员在角色菜单权限里把它勾给"指定角色"，才可访问
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive)
VALUES (5190, '信用查询(敏感)', 'custom:contract:credit-query', 3, 70, 5083, '', '', NULL, 0, b'1', b'1');
