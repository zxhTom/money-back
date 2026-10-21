-- 已有“修改用户”权限的管理角色可使用人工修改认证状态；保留独立权限以便后续单独收回。
INSERT INTO `system_role_menu`
    (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT DISTINCT existing.`role_id`, verified.`id`, 'system', NOW(), 'system', NOW(), b'0', existing.`tenant_id`
FROM `system_role_menu` existing
JOIN `system_menu` edit_user ON edit_user.`id` = existing.`menu_id`
    AND edit_user.`permission` = 'system:user:update'
JOIN `system_menu` verified ON verified.`permission` = 'system:user:update-verified'
JOIN `system_role` r ON r.`id` = existing.`role_id`
WHERE existing.`deleted` = b'0'
    AND r.`deleted` = b'0'
    AND r.`status` = 0
    AND r.`code` IN ('super_admin', 'tenant_admin', 'contract-manager')
    AND r.`tenant_id` = existing.`tenant_id`
    AND edit_user.`deleted` = b'0'
    AND verified.`deleted` = b'0'
    AND NOT EXISTS (
        SELECT 1 FROM `system_role_menu` granted
        WHERE granted.`role_id` = existing.`role_id`
            AND granted.`menu_id` = verified.`id`
            AND granted.`tenant_id` = existing.`tenant_id`
            AND granted.`deleted` = b'0'
    );
