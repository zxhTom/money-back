-- ============================================================
-- 新注册用户强制修改支付密码：system_users 增加"是否改过支付密码"标记
--
-- 背景：注册时支付密码默认等于登录密码（CustomDefineServiceImpl.register 里
-- user.setPayPassword(user.getPassword())），此前没有任何字段能区分"用户自己设过"
-- 和"沿用登录密码"，所以没法做强制修改。
--
-- 范围：只对新注册用户生效。存量用户在这里一次性回填为 1（视为已修改），
-- 不会被拦截——存量用户的支付密码大概率就是登录密码，强制拦截影响面太大。
--
-- 幂等：列已存在时 MySQL 报 1060，SqlMigrationRunner 识别为"对象已存在"自动跳过。
-- 不要用 SET @var / PREPARE，见 54_contract_tenant_id.sql 顶部说明。
-- ============================================================

ALTER TABLE `system_users` ADD COLUMN `pay_password_changed` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已单独修改过支付密码(0-否,支付密码仍等于登录密码 1-是)';

-- 存量用户全部视为已修改，避免上线后老用户被拦在门外。
-- 只在列刚新增（全为默认值 0）时有意义；重复执行会把新注册未改的用户也刷成 1，
-- 所以用 create_time 限定：只回填本迁移之前就存在的用户。
UPDATE `system_users` SET `pay_password_changed` = b'1'
WHERE `pay_password_changed` = b'0' AND `create_time` < '2026-08-14 00:00:00';
