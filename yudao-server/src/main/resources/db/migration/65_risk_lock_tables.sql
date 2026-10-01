-- ============================================================
-- 账号锁定与风控表 (LockUser)
-- 包含：
--   1. system_risk_rate_limit_config：API 访问频率规则配置表
--   2. system_risk_user_lock_log：用户锁定记录表
--   3. system_risk_abnormal_report：风控异常报告表
-- ============================================================

CREATE TABLE IF NOT EXISTS `system_risk_rate_limit_config` (
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`              bigint       DEFAULT NULL COMMENT '角色ID（为空代表全局规则）',
    `api_pattern`          varchar(255) NOT NULL DEFAULT '' COMMENT '接口匹配模式，支持 Ant 风格或具体 URI',
    `time_window`          int          NOT NULL DEFAULT 60 COMMENT '时间窗口（秒）',
    `max_count`            int          NOT NULL DEFAULT 100 COMMENT '窗口内最大请求次数',
    `lock_duration_ladder` varchar(255) NOT NULL DEFAULT '' COMMENT '阶梯锁定时间（秒），如 300,3600,86400,-1（-1表示永久锁定）',
    `status`               tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0-启用，1-禁用',
    `tenant_id`            bigint       NOT NULL DEFAULT 1 COMMENT '租户编号',
    `creator`              varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_role` (`tenant_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API访问频率规则配置表';

CREATE TABLE IF NOT EXISTS `system_risk_user_lock_log` (
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`              bigint       NOT NULL COMMENT '用户ID',
    `lock_type`            varchar(32)  NOT NULL COMMENT '锁定类型：RATE_LIMIT, PASSWORD_ERR, MANUAL, RISK_CONTROL',
    `lock_start_time`      datetime     NOT NULL COMMENT '锁定开始时间',
    `lock_end_time`        datetime     DEFAULT NULL COMMENT '锁定结束时间（为空代表永久禁用）',
    `escalation_level`     int          NOT NULL DEFAULT 1 COMMENT '阶梯层级，记录这是第几次惩罚',
    `unlock_time`          datetime     DEFAULT NULL COMMENT '手动解锁时间（为空代表未手动解锁）',
    `unlocker_id`          bigint       DEFAULT NULL COMMENT '解锁人ID',
    `tenant_id`            bigint       NOT NULL DEFAULT 1 COMMENT '租户编号',
    `creator`              varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户锁定记录表';

CREATE TABLE IF NOT EXISTS `system_risk_abnormal_report` (
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`              bigint       NOT NULL COMMENT '用户ID',
    `abnormal_type`        varchar(64)  NOT NULL COMMENT '异常类型',
    `description`          varchar(500) NOT NULL DEFAULT '' COMMENT '描述',
    `status`               tinyint      NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理，1-已处理',
    `tenant_id`            bigint       NOT NULL DEFAULT 1 COMMENT '租户编号',
    `creator`              varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控异常用户报告表';
