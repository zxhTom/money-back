-- ============================================================================
-- 22: 小程序注册严格等级 系统配置（infra_config，幂等）。默认 low。
--   low    = 登录密码仅6位数字，支付密码=登录密码（原逻辑，兼容旧版小程序）
--   middle = 登录密码不能纯数字，支付密码=身份证后6位（不需用户输入）
--   high   = 登录密码不能纯数字，支付密码用户自设6位数字且不能连续/过简（如123456、223344）
--   小程序进注册页调 /custom/contract/dashboard/register-policy 取此等级动态控制；后端注册时也按此校验。
-- ============================================================================

INSERT INTO `infra_config`
    (`category`, `type`, `name`, `config_key`, `value`, `visible`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'biz', 2, '小程序注册严格等级', 'system.user.register-strictness', 'low', b'0',
       'low/middle/high：控制注册登录密码与支付密码规则；默认 low（兼容旧版小程序）',
       '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `infra_config` WHERE `config_key` = 'system.user.register-strictness' AND `deleted` = b'0'
);
