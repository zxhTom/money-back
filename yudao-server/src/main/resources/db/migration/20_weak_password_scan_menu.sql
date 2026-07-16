-- ============================================================================
-- 20: 弱密码检测 按钮权限（增量，幂等）。
--   权限 system:user:weak-scan，挂用户管理菜单(111) 下的按钮(type=3)。
--   仅超级管理员拥有全部权限故可见/可用；控制器另有"硬校验必须超管"的代码闸，双重隔离。
-- ============================================================================

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `creator`, `create_time`)
SELECT 5171, '弱密码检测', 'system:user:weak-scan', 3, 99, 111, 0, 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:weak-scan' OR `id` = 5171);
