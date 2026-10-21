-- 用户管理：单独授权人工修改认证状态。
INSERT INTO `system_menu`
    (`name`, `permission`, `type`, `sort`, `parent_id`, `status`, `creator`, `create_time`)
SELECT '修改认证状态', 'system:user:update-verified', 3, 98, (SELECT parent_id FROM system_menu WHERE permission = 'system:user:update' AND deleted = b'0' LIMIT 1), 0, 'system', NOW()
FROM DUAL WHERE EXISTS (SELECT 1 FROM system_menu WHERE permission = 'system:user:update' AND deleted = b'0') AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:update-verified'
);
