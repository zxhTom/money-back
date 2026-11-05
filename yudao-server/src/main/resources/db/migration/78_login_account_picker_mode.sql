-- 登录页已记住账号的展示方式。旧版小程序不读取此参数，继续使用列表。
INSERT INTO `infra_config`
    (`category`, `type`, `name`, `config_key`, `value`, `visible`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'login', 2, '记住密码账号展示方式', 'login.remembered-account-picker-mode', 'list', b'1',
       'list=列表模式；circle=圆形横向滑动模式。账号较多或屏幕较窄时可横向滑动。',
       '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `infra_config` WHERE `config_key` = 'login.remembered-account-picker-mode' AND `deleted` = b'0'
);
