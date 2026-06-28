-- ============================================================
-- 重置密码是否同步支付密码 系统配置
-- 支持重复执行
-- ============================================================
INSERT INTO infra_config (category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted)
SELECT 'biz', 2, '重置密码同步支付密码',
       'system.user.reset-password-sync-pay',
       'true',
       b'0',
       'true=管理员重置登录密码时同时重置支付密码；false=仅重置登录密码，支付密码不变',
       '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM infra_config
  WHERE config_key = 'system.user.reset-password-sync-pay' AND deleted = b'0'
);
