-- 租户隔离第二阶段（B 类未隔离表补租户列）
-- 存量数据归默认租户（tenant_id = 1）
-- 这些表在请求线程内写入，开启租户后由框架自动过滤

ALTER TABLE `custom_audit_log`           ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_invite_code`         ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_invite_register_log` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `system_feedback`            ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_user_ip_history`     ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_password_history`    ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
