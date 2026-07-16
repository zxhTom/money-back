-- 邀请码注册功能（幂等版：可重复执行，配合自动迁移器）
-- admin 账号 userId 固定为 1；如你的 admin 不是 1，请改下面的 1。

-- 1. 用户属性：邀请功能开关（列已存在时由迁移器自动跳过 1060 错误）
ALTER TABLE `system_users`
    ADD COLUMN `invite_enabled` tinyint(1) NOT NULL DEFAULT 0
    COMMENT '是否允许该用户生成邀请码(0-否 1-是)' AFTER `tease_enabled`;

-- 2. 邀请码表
CREATE TABLE IF NOT EXISTS `custom_invite_code` (
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `inviter_user_id` bigint       NOT NULL COMMENT '生成邀请码的用户ID',
    `code`            varchar(32)  NOT NULL COMMENT '邀请码',
    `status`          tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0-有效 1-已被新码替换/失效',
    `expire_time`     datetime     NOT NULL COMMENT '过期时间（生成时间+8小时）',
    `register_count`  int          NOT NULL DEFAULT 0 COMMENT '通过该码成功注册的人数',
    `creator`         varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_inviter` (`inviter_user_id`),
    KEY `idx_status_expire` (`status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请码';

-- 3. 邀请注册记录表
CREATE TABLE IF NOT EXISTS `custom_invite_register_log` (
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `invite_code_id`   bigint       NOT NULL COMMENT '邀请码ID',
    `code`             varchar(32)  NOT NULL COMMENT '邀请码',
    `inviter_user_id`  bigint       NOT NULL COMMENT '邀请人用户ID',
    `invitee_user_id`  bigint       NOT NULL COMMENT '被邀请人用户ID',
    `invitee_username` varchar(300) DEFAULT '' COMMENT '被邀请人用户名',
    `invitee_realname` varchar(255) DEFAULT '' COMMENT '被邀请人真实姓名',
    `register_ip`      varchar(64)  DEFAULT '' COMMENT '注册来源IP',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    KEY `idx_code_id` (`invite_code_id`),
    KEY `idx_inviter` (`inviter_user_id`),
    KEY `idx_invitee` (`invitee_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请注册记录';

-- 4. 告警规则：注册频率风控 + 人脸未认证（幂等：NOT EXISTS 防重复）
INSERT INTO `custom_alert_rule`
    (`alert_type`, `name`, `description`, `enabled`, `severity`, `threshold`, `window_seconds`, `auto_ban`, `ban_duration_seconds`, `notify_channels`, `creator`, `create_time`)
SELECT 'REGISTER_ABUSE', '注册频率风控', '同一IP在窗口内注册次数超过阈值则触发告警并拉黑IP', 1, 3, 20, 3600, 1, 86400, 'IN_APP,WECHAT_MP', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE `alert_type` = 'REGISTER_ABUSE');

INSERT INTO `custom_alert_rule`
    (`alert_type`, `name`, `description`, `enabled`, `severity`, `threshold`, `window_seconds`, `auto_ban`, `ban_duration_seconds`, `notify_channels`, `creator`, `create_time`)
SELECT 'FACE_AUTH_UNCOMPLETED', '人脸未认证扫描', '定时扫描近期注册但未完成人脸认证的用户并推送管理员', 1, 2, NULL, NULL, 0, NULL, 'IN_APP,WECHAT_MP', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE `alert_type` = 'FACE_AUTH_UNCOMPLETED');

-- 两条规则通知 admin（userId=1）
INSERT INTO `custom_alert_rule_notify` (`rule_id`, `target_type`, `target_id`, `creator`, `create_time`)
SELECT r.id, 'USER', 1, 'system', NOW()
FROM `custom_alert_rule` r
WHERE r.alert_type IN ('REGISTER_ABUSE', 'FACE_AUTH_UNCOMPLETED') AND r.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM `custom_alert_rule_notify` n
                  WHERE n.rule_id = r.id AND n.target_type = 'USER' AND n.target_id = 1 AND n.deleted = 0);

-- 5. 权限点：邀请管理 system:user:invite（幂等：按 permission 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5130, '邀请管理', 'system:user:invite', 3, 20, 100, '', '', '', NULL, 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'system:user:invite');

-- 6. 定时任务：人脸未认证扫描（幂等：按 handler_name 判重；status=2 停用，确认后手动开启）
INSERT INTO `infra_job`
    (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`)
SELECT '人脸未认证扫描 Job', 2, 'faceAuthUncompletedCheckJob', '7', '0 0 9 * * ?', 0, 0, 0, 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `infra_job` WHERE `handler_name` = 'faceAuthUncompletedCheckJob');

-- 7. 开启指定名单的邀请属性（幂等）
UPDATE `system_users`
SET `invite_enabled` = 1, `update_time` = NOW()
WHERE `deleted` = 0
  AND `realname` IN ('冯书涛','史荣梅','陈积平','张强','刘橙枫','赵志祥','张厚涛','罗意洲','胡花枝','张显');
