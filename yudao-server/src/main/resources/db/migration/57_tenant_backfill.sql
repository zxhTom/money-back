-- ============================================================
-- 多租户上线事故修复：回填租户关闭期间产生的 tenant_id = 0 存量数据
--
-- 背景：yudao 的租户过滤是框架自动识别的——凡是 DO 继承 TenantBaseDO 的表，
-- 查询都会被自动追加 tenant_id = ? 条件（见 TenantDatabaseInterceptor.computeIgnoreTable）。
-- yudao 种子数据里 tenant_id 写死为 1，但 tenant.enable=false 期间新建的行
-- （小程序注册用户、后来加的角色/部门/角色菜单关联）拿的是列默认值 0。
-- 一旦 enable=true，这些 tenant_id=0 的行对租户 1 完全不可见：
-- 用户查不到 → 登录失败；role_menu 查不到 → 菜单权限为空。
--
-- 只更新 tenant_id = 0 的行，所以 54 建的 admin2（tenant_id=2）不受影响。
-- 幂等：跑第二次时已经没有 tenant_id=0 的行，UPDATE 影响 0 行。
--
-- 不含 member_user / pay_channel / pay_notify_task / mp_account：
-- 这几张表不在种子 SQL 里，本库可能不存在，写进来会让迁移执行器
-- 撞上 1146 表不存在而中断后续所有迁移。若这些模块在用，见文件末尾说明手动执行。
-- ============================================================

UPDATE `system_users`      SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_dept`       SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_role`       SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_role_menu`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_social_client` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 令牌改在 DB 里原地修正 tenant_id = 1，避免老用户被踢下线。
-- 注意：Redis 里的 oauth2_access_token:* 缓存需要在发布时手动清除，
-- 清除后后端会回库读到 tenant_id=1。
UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 若本库存在以下表且对应模块在用，请手动执行（故意不放进迁移，避免表不存在导致中断）：
--   UPDATE member_user     SET tenant_id = 1 WHERE tenant_id = 0;
--   UPDATE pay_channel     SET tenant_id = 1 WHERE tenant_id = 0;
--   UPDATE pay_notify_task SET tenant_id = 1 WHERE tenant_id = 0;
--   UPDATE mp_account      SET tenant_id = 1 WHERE tenant_id = 0;
