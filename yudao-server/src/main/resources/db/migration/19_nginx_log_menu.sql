-- ============================================================================
-- 19: Nginx 日志查看/统计 菜单（增量，幂等）。功能纯读文件，无新表；挂安全监控目录 5083。
-- ============================================================================

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5161, 'Nginx日志', '', 2, 60, 5083, 'nginx-log', 'ep:document', 'custom/security/nginxLog/index', 'CustomSecurityNginxLog', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5161 OR `component_name` = 'CustomSecurityNginxLog');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `creator`, `create_time`)
SELECT 5162, 'Nginx日志查询', 'custom:security:nginxlog:query', 3, 1, 5161, 0, 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:security:nginxlog:query');
