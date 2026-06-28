-- ============================================================
-- 告警规则表 + 通知人员表
-- 支持重复执行
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_alert_rule` (
  `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
  `alert_type`           VARCHAR(50)  NOT NULL COMMENT '告警类型：BRUTE_FORCE/SQL_INJECTION/XSS/DATA_LEAKAGE/IP_RISK',
  `name`                 VARCHAR(100) NOT NULL COMMENT '规则名称',
  `description`          VARCHAR(500)          COMMENT '规则说明',
  `enabled`              TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用：0否 1是',
  `severity`             TINYINT      NOT NULL DEFAULT 2 COMMENT '告警级别：1低 2中 3高 4严重',
  `threshold`            INT                   COMMENT '触发阈值（次数/条数），NULL=每次都触发',
  `window_seconds`       INT                   COMMENT '时间窗口（秒），配合 threshold 使用',
  `auto_ban`             TINYINT      NOT NULL DEFAULT 0 COMMENT '是否自动封禁 IP：0否 1是',
  `ban_duration_seconds` BIGINT                COMMENT '自动封禁时长（秒），NULL=永久',
  `notify_channels`      VARCHAR(100)          COMMENT '通知渠道，逗号分隔：IN_APP,EMAIL',
  `creator`              VARCHAR(64)  NOT NULL DEFAULT '',
  `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater`              VARCHAR(64)  NOT NULL DEFAULT '',
  `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`              TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_type` (`alert_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则配置';

CREATE TABLE IF NOT EXISTS `custom_alert_rule_notify` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `rule_id`     BIGINT      NOT NULL COMMENT '规则 ID',
  `target_type` VARCHAR(10) NOT NULL COMMENT '通知对象类型：USER/ROLE/DEPT',
  `target_id`   BIGINT      NOT NULL COMMENT '对象 ID（用户ID/角色ID/部门ID）',
  `creator`     VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater`     VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则通知人员';

-- ── 预置5条规则（重复执行跳过）──────────────────────────────
INSERT INTO `custom_alert_rule` (alert_type, name, description, enabled, severity, threshold, window_seconds, auto_ban, ban_duration_seconds, notify_channels, creator, updater)
SELECT 'BRUTE_FORCE', '暴力破解检测',
  '检测同一 IP 在短时间内对登录/密码接口的高频请求，超过阈值后触发告警并可自动封禁 IP。',
  1, 3, 10, 300, 1, 180000, 'WECHAT_MP', '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE alert_type='BRUTE_FORCE' AND deleted=0);

INSERT INTO `custom_alert_rule` (alert_type, name, description, enabled, severity, threshold, window_seconds, auto_ban, ban_duration_seconds, notify_channels, creator, updater)
SELECT 'SQL_INJECTION', 'SQL 注入检测',
  '检测请求参数中包含 SQL 注入特征字符串，累计超过阈值次数后自动封禁 IP。',
  1, 3, 5, 600, 1, 180000, 'WECHAT_MP', '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE alert_type='SQL_INJECTION' AND deleted=0);

INSERT INTO `custom_alert_rule` (alert_type, name, description, enabled, severity, threshold, window_seconds, auto_ban, ban_duration_seconds, notify_channels, creator, updater)
SELECT 'XSS', 'XSS 攻击检测',
  '检测请求参数中包含跨站脚本攻击特征，每次检测到均触发告警（后端 XssFilter 会过滤内容）。',
  1, 2, NULL, NULL, 0, NULL, 'WECHAT_MP', '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE alert_type='XSS' AND deleted=0);

INSERT INTO `custom_alert_rule` (alert_type, name, description, enabled, severity, threshold, window_seconds, auto_ban, ban_duration_seconds, notify_channels, creator, updater)
SELECT 'DATA_LEAKAGE', '数据泄露检测',
  '检测用户在短时间内大量访问/导出敏感数据（如合同、用户信息），超过阈值触发告警。',
  1, 2, NULL, NULL, 0, NULL, 'WECHAT_MP', '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE alert_type='DATA_LEAKAGE' AND deleted=0);

INSERT INTO `custom_alert_rule` (alert_type, name, description, enabled, severity, threshold, window_seconds, auto_ban, ban_duration_seconds, notify_channels, creator, updater)
SELECT 'IP_RISK', 'IP 风险检测',
  '通过 IP 库查询判断请求 IP 是否为代理、机房、Tor 出口节点等高风险类型，每次扫描到均触发。',
  1, 2, NULL, NULL, 0, NULL, 'WECHAT_MP', '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE alert_type='IP_RISK' AND deleted=0);
