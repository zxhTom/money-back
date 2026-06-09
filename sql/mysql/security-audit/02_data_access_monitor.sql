-- ============================================================
-- 数据访问监控框架建表 SQL
-- ============================================================

-- 监控配置表（哪些用户有条数限制）
CREATE TABLE IF NOT EXISTS `custom_data_access_config` (
    `id`                    bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `module`                varchar(64)  NOT NULL COMMENT '模块标识，对应 @DataAccessMonitor.module',
    `entity_type`           varchar(64)  NOT NULL COMMENT '实体类型，对应 @DataAccessMonitor.entityType',
    `user_id`               bigint                DEFAULT NULL COMMENT 'null=全局默认配置，非null=针对特定用户',
    `enabled`               tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否启用监控(0=关闭,1=开启)',
    `max_records_per_window` int                  DEFAULT NULL COMMENT '时间窗口内最大访问条数，null=不限制',
    `window_seconds`        int          NOT NULL DEFAULT 86400 COMMENT '时间窗口大小(秒)，默认1天',
    `remark`                varchar(255)          DEFAULT NULL COMMENT '备注',
    `creator`               varchar(64)           DEFAULT NULL,
    `create_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater`               varchar(64)           DEFAULT NULL,
    `update_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`               tinyint(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_module_entity` (`module`, `entity_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '数据访问监控配置';

-- 数据访问日志表（记录用户每次查询到哪些记录）
CREATE TABLE IF NOT EXISTS `custom_data_access_log` (
    `id`           bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      bigint        NOT NULL COMMENT '操作用户ID',
    `username`     varchar(64)            DEFAULT NULL COMMENT '用户名',
    `module`       varchar(64)   NOT NULL COMMENT '模块名',
    `entity_type`  varchar(64)   NOT NULL COMMENT '实体类型',
    `query_params` text                   DEFAULT NULL COMMENT '查询参数JSON',
    `result_ids`   text                   DEFAULT NULL COMMENT '返回记录ID列表JSON，如[1,2,3]',
    `result_count` int                    DEFAULT NULL COMMENT '返回记录条数',
    `request_url`  varchar(255)           DEFAULT NULL COMMENT '请求路径',
    `client_ip`    varchar(50)            DEFAULT NULL COMMENT '客户端IP',
    `access_time`  datetime      NOT NULL COMMENT '访问时间',
    `deleted`      tinyint(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_module_time` (`user_id`, `module`, `access_time`),
    KEY `idx_access_time` (`access_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '数据访问监控日志';
