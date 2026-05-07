-- ============================================================
-- 指定模块对指定设备的开放控制
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_device_module_access` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `module_name` VARCHAR(128) NOT NULL COMMENT '模块名',
  `device_id` VARCHAR(128) NOT NULL COMMENT '设备ID',
  `enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否开放：0 否，1 是',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_module_device` (`module_name`, `device_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_module_name` (`module_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备模块开放控制';

