-- ============================================================================
-- 新增 creditor（债权人）角色，持有 custom:contract:credit-query（菜单id 5190，
-- 见 28_credit_query_permission.sql）权限。首次在本仓库新增 system_role 行——
-- 之前的信用查询权限收紧(migration 28)只新增了菜单/权限行，一直只授予role_id=1
-- (超级管理员)，本次是第一次为该权限单独建一个可分配给普通用户的角色。
-- id不手动指定，交由system_role.id的AUTO_INCREMENT自动分配，避免猜测可用id。
-- ============================================================================

INSERT INTO `system_role`
    (`name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT '债权人', 'creditor', 10, 1, '', 0, 2,
       '可查询任意人的信用信息（持有custom:contract:credit-query权限）；不持有此角色的用户仅能查询与自己存在合同往来对象的信用信息',
       '1', NOW(), '1', NOW(), b'0', 1
WHERE NOT EXISTS (
    SELECT 1 FROM `system_role` WHERE `code` = 'creditor' AND `deleted` = b'0'
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`)
SELECT r.id, 5190, '1', NOW()
FROM `system_role` r
WHERE r.code = 'creditor' AND r.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM `system_role_menu` WHERE `role_id` = r.id AND `menu_id` = 5190
  );
