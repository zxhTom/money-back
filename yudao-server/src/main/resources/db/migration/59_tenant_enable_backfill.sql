-- ============================================================
-- 开启多租户上线数据回填与租户 2 重建准备
--
-- 背景：54 号迁移加列 (tenant_id) 时因线上列已存在被数据库跳过，
-- 导致线上合同和令牌数据留在默认值 tenant_id = 0；
-- 57 号迁移已于 2026-08-26 执行过，迁移执行器不会重复执行，
-- 故在此 59 号脚本集中将存量数据回填至默认租户 1，
-- 并软删除异常创建的租户 2（若无关联用户和合同），以便后台标准重建。
-- ============================================================

-- 1. 合同表默认租户改为 1（只改元数据，不动数据；重复执行无副作用）
ALTER TABLE `custom_contract` ALTER COLUMN `tenant_id` SET DEFAULT 1;

-- 2. 业务数据回填到租户 1
-- 显式赋值 update_time，避免 ON UPDATE 把修改时间刷成当前时间、打乱列表排序
UPDATE `custom_contract`             SET `tenant_id` = 1, `update_time` = `update_time` WHERE `tenant_id` = 0;
UPDATE `custom_contract_daily_stats` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 3. 令牌原地修正，老用户免重登（发布后需清 Redis oauth2_access_token:*）
UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 4. 系统表兜底（线上当前为 0 行，57 执行后到本次发布之间若有新增会被带上）
UPDATE `system_users`         SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_dept`          SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_role`          SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_role_menu`     SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_social_client` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 5. 软删除 54 号手写创建的租户 2：套餐 0 会被当成系统租户，后台既不能编辑也不能删除，
--    所以改为更新标志位后在后台用标准流程重建。只有名下没有用户和合同时才生效。
UPDATE `system_tenant` SET `deleted` = 1, `update_time` = NOW(), `updater` = 'system'
WHERE `id` = 2 AND `package_id` = 0 AND `deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `system_users`    WHERE `tenant_id` = 2)
  AND NOT EXISTS (SELECT 1 FROM `custom_contract` WHERE `tenant_id` = 2);
