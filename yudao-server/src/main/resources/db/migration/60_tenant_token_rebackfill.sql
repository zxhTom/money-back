-- 60 号迁移：回滚租户关闭期间再产生的 tenant_id = 0 数据二次兜底回填
UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `custom_contract`             SET `tenant_id` = 1, `update_time` = `update_time` WHERE `tenant_id` = 0;
UPDATE `custom_contract_daily_stats` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_users`                SET `tenant_id` = 1 WHERE `tenant_id` = 0;
