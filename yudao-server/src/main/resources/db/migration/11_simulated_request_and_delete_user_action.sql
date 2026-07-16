-- ============================================================================
-- 模拟/伪造请求风控 + 告警规则新增"逻辑删除用户并踢下线"动作（增量）
--   1. custom_alert_rule 新增 auto_delete_user 列：触发后是否逻辑删除命中账号并剔除其 token
--   2. 新增 SIMULATED_REQUEST 告警规则：同一账号 1 小时内伪造来源IP 请求达 2 次即处置
--   3. 该规则通知 admin（userId=1）
-- 说明：伪造请求 = 客户端在 X-Forwarded-For 注入内网IP 伪装来源；后端用可信真实IP 比对识别。
--       动作可配：auto_ban(封禁IP) 与 auto_delete_user(删号踢下线) 可单独/组合开启。
--       删号动作默认关闭(0)，需要时在告警规则页打开；超管账号永不自动删除。
-- ============================================================================

-- 1. 新增动作列（所有告警规则通用）
ALTER TABLE `custom_alert_rule`
    ADD COLUMN `auto_delete_user` tinyint NOT NULL DEFAULT 0
    COMMENT '触发后是否逻辑删除用户并剔除其token(0-否 1-是)' AFTER `ban_duration_seconds`;

-- 2. 新增 SIMULATED_REQUEST 规则（阈值2/窗口1小时，默认封IP、不删号）
INSERT INTO `custom_alert_rule`
    (`alert_type`, `name`, `description`, `enabled`, `severity`, `threshold`, `window_seconds`,
     `auto_ban`, `ban_duration_seconds`, `auto_delete_user`, `notify_channels`, `creator`, `create_time`)
SELECT 'SIMULATED_REQUEST', '模拟/伪造请求', '客户端伪造来源IP(X-Forwarded-For注入内网IP)；同一账号窗口内达阈值即处置',
       1, 3, 2, 3600, 1, 86400, 0, 'IN_APP,WECHAT_MP', 'system', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `custom_alert_rule` WHERE `alert_type` = 'SIMULATED_REQUEST');

-- 3. 通知 admin（userId=1）
INSERT INTO `custom_alert_rule_notify` (`rule_id`, `target_type`, `target_id`, `creator`, `create_time`)
SELECT r.id, 'USER', 1, 'system', NOW()
FROM `custom_alert_rule` r
WHERE r.alert_type = 'SIMULATED_REQUEST' AND r.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM `custom_alert_rule_notify` n
      WHERE n.rule_id = r.id AND n.target_type = 'USER' AND n.target_id = 1 AND n.deleted = 0
  );

-- 开启"删号踢下线"动作（确认后再执行）：
-- UPDATE `custom_alert_rule` SET `auto_delete_user` = 1 WHERE `alert_type` = 'SIMULATED_REQUEST';
