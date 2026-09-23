-- 61 号迁移：回填租户临时关闭期间产生的 tenant_id = 0 令牌
UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
