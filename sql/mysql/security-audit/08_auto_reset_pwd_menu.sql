-- ============================================================
-- 1. 菜单：定时自动重置密码
-- ============================================================
-- 先找到安全管理父目录 ID（按 name 查）
SET @parentId = (
    SELECT id FROM system_menu WHERE name = '安全管理' AND type = 1 LIMIT 1
);

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, component_name,
    status, visible, keep_alive, always_show,
    creator, updater, create_time, update_time, deleted, tenant_id
)
SELECT
    '定时重置密码', '', 2, 60, @parentId,
    'auto-reset-pwd', 'ep:refresh', 'custom/security/autoResetPwd/index', 'CustomAutoResetPwd',
    0, 1, 1, 0,
    'admin', 'admin', NOW(), NOW(), 0, 1
WHERE @parentId IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM system_menu WHERE name = '定时重置密码' AND deleted = 0
);

-- ============================================================
-- 2. 定时任务（infra_job）
-- cron: 0 0 22 * * ? → 每晚 22:00
-- handlerName: autoResetPwdJob（Spring Bean 名称）
-- handlerParam: 替换为实际通知邮箱
-- ============================================================
INSERT INTO infra_job (
    name, status, handler_name, handler_param, cron_expression,
    retry_count, retry_interval, monitor_timeout,
    creator, updater, create_time, update_time, deleted
)
SELECT
    '用户密码定时自动重置', 2, 'autoResetPwdJob',
    'notifyEmail=mengsapples@gmail.com',
    '0 0 22 * * ?',
    3, 0, 0,
    'admin', 'admin', NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM infra_job WHERE handler_name = 'autoResetPwdJob' AND deleted = 0
);
