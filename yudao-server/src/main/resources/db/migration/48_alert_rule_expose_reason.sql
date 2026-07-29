ALTER TABLE `custom_alert_rule` ADD COLUMN `expose_reason` TINYINT NOT NULL DEFAULT 0
    COMMENT '触发后是否在响应里暴露具体原因（次数/阈值等）：0-否（返回通用提示）1-是' AFTER `auto_ban`;

INSERT INTO `custom_alert_rule` (alert_type, name, description, enabled, severity, auto_ban, expose_reason, creator, updater)
SELECT 'IP_BLACKLIST_BLOCK', 'IP黑名单拦截原因',
       'IP 因命中黑名单被拒绝访问时，是否告知具体封禁原因和解封时间。此规则的"阈值/窗口/自动封禁"等字段本身不生效，仅 expose_reason 生效——是否封禁由触发它的原始规则（暴力破解/SQL注入/注册风控等）决定。',
       1, 2, 0, 0, 'system', 'system'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE `alert_type` = 'IP_BLACKLIST_BLOCK');

INSERT INTO `custom_alert_rule` (alert_type, name, description, enabled, severity, auto_ban, expose_reason, creator, updater)
SELECT 'CONTRACT_QUERY_RATE_LIMIT', '信用查询限速原因',
       '信用查询触发多时间窗口限速时，是否告知具体窗口/次数/上限。真正的限速阈值在 yudao.contract-page-rate-limit.* 配置里，此规则的"阈值/窗口"字段不生效，仅 expose_reason 生效。',
       1, 1, 0, 0, 'system', 'system'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE `alert_type` = 'CONTRACT_QUERY_RATE_LIMIT');
