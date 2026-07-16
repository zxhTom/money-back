-- ============================================================================
-- 多登录风控 + IP白名单 + 令牌登录IP（增量，幂等）
--   1. system_oauth2_access_token 加 ip 列（令牌登录来源IP）+ 索引
--   2. custom_ip_whitelist：IP白名单（命中的IP躲过所有封禁与多登录风控）
--   3. custom_alert_rule 新增两类规则：
--        IP_MULTI_USER 多夫多妻(一IP多用户)  → 封IP + 剔除相关用户(不改密)
--        USER_MULTI_IP 一用户多IP            → 封该用户所有IP
--   4. system_menu：IP白名单管理页 + 权限（归到安全监控目录 5083）
-- ============================================================================

-- 1. 令牌登录来源IP（列已存在时迁移器自动跳过 1060）
ALTER TABLE `system_oauth2_access_token`
    ADD COLUMN `ip` varchar(64) DEFAULT NULL COMMENT '登录来源IP' AFTER `expires_time`;
ALTER TABLE `system_oauth2_access_token`
    ADD INDEX `idx_ip` (`ip`);

-- 2. IP白名单表
CREATE TABLE IF NOT EXISTS `custom_ip_whitelist` (
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ip`          varchar(64) NOT NULL COMMENT 'IP地址（精确匹配）',
    `remark`      varchar(255) DEFAULT '' COMMENT '备注',
    `enabled`     tinyint     NOT NULL DEFAULT 1 COMMENT '是否启用(0-否 1-是)',
    `creator`     varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP白名单';

-- 3. 两类多登录告警规则（阈值3=最多允许3，超过即触发；幂等 NOT EXISTS）
INSERT INTO `custom_alert_rule`
    (`alert_type`, `name`, `description`, `enabled`, `severity`, `threshold`, `window_seconds`, `auto_ban`, `ban_duration_seconds`, `auto_delete_user`, `auto_reset_password`, `notify_channels`, `creator`, `create_time`)
SELECT 'IP_MULTI_USER', '多夫多妻(一IP多用户)', '同一IP同时登录用户数超过阈值→封IP并剔除相关用户(不改密)', 1, 3, 3, NULL, 1, 86400, 0, 0, 'IN_APP,WECHAT_MP', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE `alert_type` = 'IP_MULTI_USER');

INSERT INTO `custom_alert_rule`
    (`alert_type`, `name`, `description`, `enabled`, `severity`, `threshold`, `window_seconds`, `auto_ban`, `ban_duration_seconds`, `auto_delete_user`, `auto_reset_password`, `notify_channels`, `creator`, `create_time`)
SELECT 'USER_MULTI_IP', '一用户多IP', '同一用户同时在线IP数超过阈值→立即封该用户所有IP', 1, 3, 3, NULL, 1, 86400, 0, 0, 'IN_APP,WECHAT_MP', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE `alert_type` = 'USER_MULTI_IP');

-- 通知 admin（userId=1）
INSERT INTO `custom_alert_rule_notify` (`rule_id`, `target_type`, `target_id`, `creator`, `create_time`)
SELECT r.id, 'USER', 1, 'system', NOW()
FROM `custom_alert_rule` r
WHERE r.alert_type IN ('IP_MULTI_USER', 'USER_MULTI_IP') AND r.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM `custom_alert_rule_notify` n
                  WHERE n.rule_id = r.id AND n.target_type = 'USER' AND n.target_id = 1 AND n.deleted = 0);

-- 4. IP白名单管理页 + 权限（挂在安全监控目录 5083 下）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5141, 'IP白名单', '', 2, 40, 5083, 'ip-whitelist', 'ep:circle-check', 'custom/security/ipWhitelist/index', 'CustomSecurityIpWhitelist', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5141 OR `component_name` = 'CustomSecurityIpWhitelist');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `creator`, `create_time`)
SELECT 5142, 'IP白名单查询', 'custom:security:whitelist:query', 3, 1, 5141, 0, 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:security:whitelist:query');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `creator`, `create_time`)
SELECT 5143, 'IP白名单配置', 'custom:security:whitelist:update', 3, 2, 5141, 0, 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:security:whitelist:update');
