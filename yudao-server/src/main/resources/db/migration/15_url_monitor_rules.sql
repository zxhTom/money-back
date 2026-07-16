-- ============================================================================
-- 将 9 个重点接口配置为 URL 访问监控告警规则（幂等）
--   触发：某IP在窗口内访问该URL超过阈值 → 立即封IP（auto_ban=1），通知 admin
--   match_url 不带 /admin-api 前缀（服务端归一化匹配，带不带都命中）
--   敏感接口（改密/找回/注册/删除/导出）阈值更低，读接口更高
-- ============================================================================

-- 0. 放宽唯一键：原 (alert_type,deleted) 只允许每类型一条，URL监控需要"每URL一条"，
--    改为 (alert_type,match_url,deleted)。单条原子 ALTER；非URL类型 match_url 为 NULL 不受影响。
ALTER TABLE `custom_alert_rule`
    DROP INDEX `uk_alert_type`,
    ADD UNIQUE KEY `uk_alert_type` (`alert_type`, `match_url`, `deleted`);

INSERT INTO `custom_alert_rule`
    (`alert_type`,`name`,`description`,`enabled`,`severity`,`threshold`,`window_seconds`,
     `auto_ban`,`ban_duration_seconds`,`auto_delete_user`,`auto_reset_password`,`match_url`,`notify_channels`,`creator`,`create_time`)
SELECT t.* FROM (
    SELECT 'URL_MONITOR' a,'URL监控-合同创建'      n,'监控 /custom/contract/create'                          d,1 e,3 s,60  th,3600 w,1 b,86400 bd,0 du,0 rp,'/custom/contract/create'                          mu,'IN_APP,WECHAT_MP' nc,'system' cr, NOW() ct
    UNION ALL SELECT 'URL_MONITOR','URL监控-合同更新','监控 /custom/contract/update',1,3,60,3600,1,86400,0,0,'/custom/contract/update','IN_APP,WECHAT_MP','system',NOW()
    UNION ALL SELECT 'URL_MONITOR','URL监控-合同删除','监控 /custom/contract/delete',1,3,40,3600,1,86400,0,0,'/custom/contract/delete','IN_APP,WECHAT_MP','system',NOW()
    UNION ALL SELECT 'URL_MONITOR','URL监控-合同查看','监控 /custom/contract/get',1,2,300,3600,1,86400,0,0,'/custom/contract/get','IN_APP,WECHAT_MP','system',NOW()
    UNION ALL SELECT 'URL_MONITOR','URL监控-合同导出','监控 /custom/contract/export-excel',1,3,20,3600,1,86400,0,0,'/custom/contract/export-excel','IN_APP,WECHAT_MP','system',NOW()
    UNION ALL SELECT 'URL_MONITOR','URL监控-用户注册','监控 /custom/contract/dashboard/register',1,3,20,3600,1,86400,0,0,'/custom/contract/dashboard/register','IN_APP,WECHAT_MP','system',NOW()
    UNION ALL SELECT 'URL_MONITOR','URL监控-身份证找回密码','监控 reset-password-by-idno',1,3,10,3600,1,86400,0,0,'/custom/contract/dashboard/reset-password-by-idno','IN_APP,WECHAT_MP','system',NOW()
    UNION ALL SELECT 'URL_MONITOR','URL监控-邮箱重置密码','监控 update-password-by-email',1,3,10,3600,1,86400,0,0,'/custom/contract/dashboard/update-password-by-email','IN_APP,WECHAT_MP','system',NOW()
    UNION ALL SELECT 'URL_MONITOR','URL监控-后台重置密码','监控 /system/user/update-password',1,3,30,3600,1,86400,0,0,'/system/user/update-password','IN_APP,WECHAT_MP','system',NOW()
) t
WHERE NOT EXISTS (
    SELECT 1 FROM `custom_alert_rule` r WHERE r.alert_type='URL_MONITOR' AND r.match_url = t.mu
);

-- 为所有还没有 admin 通知的 URL_MONITOR 规则补上通知（admin userId=1）
INSERT INTO `custom_alert_rule_notify` (`rule_id`,`target_type`,`target_id`,`creator`,`create_time`)
SELECT r.id,'USER',1,'system',NOW()
FROM `custom_alert_rule` r
WHERE r.alert_type='URL_MONITOR' AND r.deleted=0
  AND NOT EXISTS (SELECT 1 FROM `custom_alert_rule_notify` n
                  WHERE n.rule_id=r.id AND n.target_type='USER' AND n.target_id=1 AND n.deleted=0);
