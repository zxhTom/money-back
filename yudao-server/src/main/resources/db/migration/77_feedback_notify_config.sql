-- 每个租户独立配置帮助与反馈的站内信接收人。
CREATE TABLE IF NOT EXISTS `system_feedback_notify_config` (
    `tenant_id` BIGINT NOT NULL COMMENT '租户编号',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否通知',
    `recipient_user_ids` VARCHAR(1000) NOT NULL DEFAULT '[]' COMMENT '接收管理员 ID，JSON 数组；无配置行时默认租户管理员',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助与反馈通知配置';
