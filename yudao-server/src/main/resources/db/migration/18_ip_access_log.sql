-- ============================================================================
-- 18: IP 访问尝试记录 custom_ip_access_log（增量，幂等）
--   记录"被拦截/可疑"的访问（黑名单拦截 / 暴力破解 / SQL注入 / XSS），
--   按 (ip, reason, 分钟) 聚合累加 hit_count，行数有界（≤1440/IP/天/原因）。
--   用于观察被封 IP 是否仍在试探、以及每日访问频率。菜单挂安全监控目录 5083。
-- ============================================================================

CREATE TABLE IF NOT EXISTS `custom_ip_access_log` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ip`            varchar(64)  NOT NULL COMMENT '来源IP',
    `reason`        varchar(32)  NOT NULL COMMENT 'BLACKLIST_BLOCK/BRUTE_FORCE/SQL_INJECT/XSS',
    `uri`           varchar(255)          DEFAULT '' COMMENT '请求URI(最近一次样本)',
    `method`        varchar(16)           DEFAULT '' COMMENT '请求方法',
    `user_agent`    varchar(512)          DEFAULT '' COMMENT 'UA(样本)',
    `user_id`       bigint                DEFAULT NULL COMMENT '当时登录用户(可空)',
    `hit_count`     int          NOT NULL DEFAULT 1 COMMENT '该(ip,reason,分钟)内命中次数',
    `stat_day`      date         NOT NULL COMMENT '统计日(应用侧计算,避免DB时区偏差)',
    `minute_bucket` datetime     NOT NULL COMMENT '分钟桶',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ip_reason_minute` (`ip`, `reason`, `minute_bucket`),
    KEY `idx_stat_day` (`stat_day`),
    KEY `idx_ip_time` (`ip`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP访问尝试记录(按IP+原因+分钟聚合)';

-- 菜单：IP访问统计（挂安全监控目录 5083 下）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5151, 'IP访问统计', '', 2, 50, 5083, 'ip-access', 'ep:data-analysis', 'custom/security/ipAccess/index', 'CustomSecurityIpAccess', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5151 OR `component_name` = 'CustomSecurityIpAccess');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `creator`, `create_time`)
SELECT 5152, 'IP访问统计查询', 'custom:security:ipaccess:query', 3, 1, 5151, 0, 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:security:ipaccess:query');
