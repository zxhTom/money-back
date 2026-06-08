-- ============================================================
-- 审计日志 + 安全监控 建表 SQL（MySQL）
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_audit_log` (
    `id`             bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trace_id`       varchar(64)            DEFAULT NULL COMMENT '链路追踪ID',
    `user_id`        bigint                 DEFAULT NULL COMMENT '操作用户ID',
    `user_type`      tinyint                DEFAULT NULL COMMENT '用户类型(1=管理员,2=小程序用户)',
    `username`       varchar(64)            DEFAULT NULL COMMENT '用户名或昵称',
    `module`         varchar(64)            DEFAULT NULL COMMENT '业务模块',
    `operation_type` varchar(20)   NOT NULL COMMENT '操作类型(CREATE/READ/UPDATE/DELETE/EXPORT/LOGIN)',
    `entity_type`    varchar(64)            DEFAULT NULL COMMENT '实体类型',
    `entity_id`      bigint                 DEFAULT NULL COMMENT '实体ID',
    `operation`      varchar(500)           DEFAULT NULL COMMENT '操作描述',
    `before_data`    mediumtext             DEFAULT NULL COMMENT '操作前数据快照(JSON)',
    `after_data`     mediumtext             DEFAULT NULL COMMENT '操作后数据快照(JSON)',
    `request_url`    varchar(255)           DEFAULT NULL COMMENT '请求URL',
    `request_method` varchar(10)            DEFAULT NULL COMMENT 'HTTP请求方法',
    `external_ip`    varchar(50)            DEFAULT NULL COMMENT '外网IP(X-Forwarded-For第一个)',
    `direct_ip`      varchar(50)            DEFAULT NULL COMMENT '直连IP(RemoteAddr)',
    `all_ip_headers` varchar(1000)          DEFAULT NULL COMMENT '所有IP来源JSON',
    `user_agent`     varchar(500)           DEFAULT NULL COMMENT '浏览器UA',
    `status`         tinyint       NOT NULL DEFAULT 0 COMMENT '操作结果(0=成功,1=失败)',
    `error_message`  varchar(500)           DEFAULT NULL COMMENT '失败原因',
    `cost_time`      int                    DEFAULT NULL COMMENT '耗时(ms)',
    `create_time`    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_entity` (`entity_type`, `entity_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '用户行为审计日志';

CREATE TABLE IF NOT EXISTS `custom_security_alert` (
    `id`                 bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `alert_type`         varchar(50)    NOT NULL COMMENT '告警类型(SQL_INJECTION/XSS/BRUTE_FORCE/DATA_LEAKAGE/IP_ATTACK)',
    `severity`           tinyint        NOT NULL COMMENT '严重级别(1=INFO,2=WARNING,3=CRITICAL)',
    `source_ip`          varchar(50)             DEFAULT NULL COMMENT '来源IP',
    `user_id`            bigint                  DEFAULT NULL COMMENT '关联用户ID',
    `request_url`        varchar(255)            DEFAULT NULL COMMENT '触发告警的请求URL',
    `request_method`     varchar(10)             DEFAULT NULL COMMENT 'HTTP请求方法',
    `suspicious_content` text                    DEFAULT NULL COMMENT '可疑内容(脱敏处理)',
    `alert_message`      varchar(1000)  NOT NULL COMMENT '告警描述',
    `detail`             text                    DEFAULT NULL COMMENT '告警详情(JSON)',
    `handled`            tinyint        NOT NULL DEFAULT 0 COMMENT '处理状态(0=未处理,1=已处理,2=误报)',
    `handle_by`          bigint                  DEFAULT NULL COMMENT '处理人用户ID',
    `handle_time`        datetime                DEFAULT NULL COMMENT '处理时间',
    `handle_remark`      varchar(500)            DEFAULT NULL COMMENT '处理备注',
    `create_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
    PRIMARY KEY (`id`),
    KEY `idx_alert_type` (`alert_type`),
    KEY `idx_severity` (`severity`),
    KEY `idx_source_ip` (`source_ip`),
    KEY `idx_handled` (`handled`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '安全告警记录';

CREATE TABLE IF NOT EXISTS `custom_ip_blacklist` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ip`          varchar(50)  NOT NULL COMMENT 'IP地址',
    `reason`      varchar(200)          DEFAULT NULL COMMENT '封禁原因',
    `auto_added`  bit          NOT NULL DEFAULT b'0' COMMENT '是否自动封禁',
    `expire_time` datetime              DEFAULT NULL COMMENT '过期时间(NULL=永久)',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ip` (`ip`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'IP黑名单';
