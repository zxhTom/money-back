-- 配置表按租户隔离（批次 A：schema + 隔离）
-- 唯一索引必须带上 tenant_id，否则两个租户不能有同名 code

-- 1. 加列（time_window 已有列，靠 1060 跳过；再把它的存量 0 修正为 1）
ALTER TABLE `custom_skin_profile`         ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_text_profile`         ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_text_item`            ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_icon_set_profile`     ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_miniprogram_config`   ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_version_changelog`    ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_speed_control_config` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `fee_strategy`                ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `contract_model`              ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `time_window`                 ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
UPDATE `time_window` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 2. 唯一索引改为带租户（先删后建，各自独立成句）
ALTER TABLE `custom_skin_profile`      DROP INDEX `uk_code`;
ALTER TABLE `custom_skin_profile`      ADD UNIQUE KEY `uk_tenant_code` (`tenant_id`,`code`);
ALTER TABLE `custom_text_profile`      DROP INDEX `uk_code`;
ALTER TABLE `custom_text_profile`      ADD UNIQUE KEY `uk_tenant_code` (`tenant_id`,`code`);
ALTER TABLE `custom_icon_set_profile`  DROP INDEX `uk_code`;
ALTER TABLE `custom_icon_set_profile`  ADD UNIQUE KEY `uk_tenant_code` (`tenant_id`,`code`);
ALTER TABLE `custom_version_changelog` DROP INDEX `uk_version`;
ALTER TABLE `custom_version_changelog` ADD UNIQUE KEY `uk_tenant_version` (`tenant_id`,`version`);
ALTER TABLE `fee_strategy`             DROP INDEX `uk_min_max`;
ALTER TABLE `fee_strategy`             ADD UNIQUE KEY `uk_tenant_min_max` (`tenant_id`,`min_amount`,`max_amount`);
ALTER TABLE `contract_model`           DROP INDEX `contract_model_unique`;
ALTER TABLE `contract_model`           ADD UNIQUE KEY `uk_tenant_app_version` (`tenant_id`,`app_version`);
