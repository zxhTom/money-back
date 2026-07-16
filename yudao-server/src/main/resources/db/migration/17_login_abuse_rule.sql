-- ============================================================================
-- 17: 登录失败风控告警规则 LOGIN_ABUSE（增量，幂等，无 DELETE）
--   触发：同一来源IP在窗口内登录失败次数超阈值 → 封该 IP + 告警。
--   刻意【不锁账号】：避免攻击者用真实账号反复失败把该账号锁死（挤兑正常用户）。
--   默认：10 次 / 600 秒 → 封 IP 3600 秒；阈值/窗口/时长后台可改。
-- ============================================================================

INSERT INTO `custom_alert_rule`
    (`alert_type`,`name`,`description`,`enabled`,`severity`,`threshold`,`window_seconds`,
     `auto_ban`,`ban_duration_seconds`,`auto_delete_user`,`auto_reset_password`,`match_url`,`notify_channels`,`creator`,`create_time`)
SELECT 'LOGIN_ABUSE','登录失败风控',
       '同一来源IP在窗口内登录失败次数超阈值→封该IP并告警（不锁账号，覆盖后台与小程序登录）',
       1, 3, 10, 600, 1, 3600, 0, 0, NULL, 'IN_APP,WECHAT_MP', 'system', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `custom_alert_rule` r WHERE r.`alert_type`='LOGIN_ABUSE' AND r.`deleted`=0
);

-- 通知 admin(userId=1)
INSERT INTO `custom_alert_rule_notify` (`rule_id`,`target_type`,`target_id`,`creator`,`create_time`)
SELECT r.`id`,'USER',1,'system',NOW()
FROM `custom_alert_rule` r
WHERE r.`alert_type`='LOGIN_ABUSE' AND r.`deleted`=0
  AND NOT EXISTS (SELECT 1 FROM `custom_alert_rule_notify` n
                  WHERE n.`rule_id`=r.`id` AND n.`target_type`='USER' AND n.`target_id`=1 AND n.`deleted`=0);
