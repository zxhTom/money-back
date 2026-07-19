-- ============================================================
-- 小程序文案配置（幂等版：可重复执行，配合自动迁移器）
-- 对应 Java: cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO
--          cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO
-- ============================================================

-- 1. 文案套表
CREATE TABLE IF NOT EXISTS `custom_text_profile` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`         varchar(100) NOT NULL COMMENT '文案套名称',
    `code`         varchar(64)  NOT NULL COMMENT '内部唯一标识',
    `seed_from`    varchar(64)  DEFAULT '' COMMENT '种子来源：初始固定为safe，克隆时记录来源profile的code',
    `is_active`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否生效：全表仅一条应为1',
    `sort`         int          NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`       varchar(500) DEFAULT '' COMMENT '备注',
    `creator`      varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序文案配置套';

-- 2. 文案条目表
CREATE TABLE IF NOT EXISTS `custom_text_item` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `profile_id`   bigint       NOT NULL COMMENT '所属文案套ID',
    `page_key`     varchar(128) NOT NULL COMMENT '页面标识，如contract.contractDetail',
    `module_key`   varchar(128) DEFAULT '' COMMENT '模块标识，用于同一页面内分组',
    `item_key`     varchar(255) NOT NULL COMMENT '完整文案key，如contract.contractDetail.title',
    `item_value`   text         COMMENT '文案内容',
    `sort`         int          NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`       varchar(500) DEFAULT '' COMMENT '备注',
    `creator`      varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_profile_item` (`profile_id`, `item_key`),
    KEY `idx_profile_page` (`profile_id`, `page_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序文案条目';
