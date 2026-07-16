-- ============================================================================
-- URL 访问监控告警类型 + 风控改密动作（增量，幂等）
--   1. custom_alert_rule 加 auto_reset_password（触发后改密+踢token）、match_url（URL监控地址）
--   2. custom_security_pwd_reset_log：风控自动改密记录（明文新密码，供管理员查看）
--   3. system_menu：改密记录查看权限（复用 custom:security:alert:query，无需新增）
-- 用法：在告警规则页新增一条"URL访问监控"规则，填监控URL + 频率(阈值/窗口) + 勾选自动封禁IP即可。
-- ============================================================================

-- 1. 告警规则新增两列（列已存在时迁移器自动跳过 1060）
ALTER TABLE `custom_alert_rule`
    ADD COLUMN `auto_reset_password` tinyint NOT NULL DEFAULT 0
    COMMENT '触发后是否重置密码并剔除token(0-否 1-是)' AFTER `auto_delete_user`;

ALTER TABLE `custom_alert_rule`
    ADD COLUMN `match_url` varchar(255) DEFAULT NULL
    COMMENT 'URL监控规则专用：被监控的请求地址(可带或不带/admin-api前缀)' AFTER `auto_reset_password`;

-- 2. 风控改密记录表
CREATE TABLE IF NOT EXISTS `custom_security_pwd_reset_log` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      bigint       NOT NULL COMMENT '被改密用户ID',
    `username`     varchar(300) DEFAULT NULL COMMENT '用户名',
    `new_password` varchar(128) NOT NULL COMMENT '新密码明文（风控生成，供管理员查看）',
    `reason`       varchar(500) DEFAULT NULL COMMENT '改密原因（触发的风控描述）',
    `alert_type`   varchar(64)  DEFAULT NULL COMMENT '触发的告警类型',
    `source_ip`    varchar(64)  DEFAULT NULL COMMENT '来源IP',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控自动改密记录';

-- 3. 菜单：风控改密记录查看页（挂在安全监控目录 5083 下，复用 custom:security:alert:query 权限）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5140, '风控改密记录', '', 2, 30, 5083, 'pwd-reset-log', 'ep:key', 'custom/security/pwdResetLog/index', 'CustomSecurityPwdResetLog', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5140 OR `component_name` = 'CustomSecurityPwdResetLog');
