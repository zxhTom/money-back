-- contract_model 表补齐基类字段（creator/updater/update_time/deleted）
-- 原因：该表缺基类字段，DO 继承 TenantBaseDO 后用 MP 标准方法会报 Unknown column

ALTER TABLE `contract_model` ADD COLUMN `creator` varchar(64) DEFAULT '' COMMENT '创建者';
ALTER TABLE `contract_model` ADD COLUMN `updater` varchar(64) DEFAULT '' COMMENT '更新者';
ALTER TABLE `contract_model` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `contract_model` ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除';
