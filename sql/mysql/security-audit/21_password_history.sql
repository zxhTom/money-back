-- ============================================================================
-- 21: 密码变更记录 custom_password_history（增量，幂等）。
--   用户自助改密 / 管理员重置 / 找回密码 / 风控自动改密 每次变更各记一条。
--   password_hash 只存 bcrypt 密文，严禁明文。菜单挂安全监控 5083。
-- ============================================================================

CREATE TABLE IF NOT EXISTS `custom_password_history` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       bigint       NOT NULL COMMENT '被改密用户ID',
    `username`      varchar(64)           DEFAULT '' COMMENT '用户名(变更时快照)',
    `password_hash` varchar(100) NOT NULL COMMENT 'bcrypt 密文（禁止明文）',
    `scene`         varchar(32)           DEFAULT '' COMMENT 'SELF_PROFILE/RESET/RESET_WITH_PAY',
    `operator_id`   bigint                DEFAULT NULL COMMENT '操作人(本人=同user_id；无上下文为空)',
    `source_ip`     varchar(64)           DEFAULT '' COMMENT '来源IP',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密码变更记录(只存密文)';

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5181, '密码变更记录', '', 2, 70, 5083, 'pwd-history', 'ep:key', 'custom/security/pwdHistory/index', 'CustomSecurityPwdHistory', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5181 OR `component_name` = 'CustomSecurityPwdHistory');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `creator`, `create_time`)
SELECT 5182, '密码记录查询', 'custom:security:pwdhistory:query', 3, 1, 5181, 0, 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:security:pwdhistory:query');
