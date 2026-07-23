-- ============================================================
-- 图标集配置（幂等版：可重复执行，配合自动迁移器）
-- 对应 Java: cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_icon_set_profile` (
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`              varchar(100) NOT NULL COMMENT '图标集名称',
    `code`              varchar(64)  DEFAULT NULL COMMENT '内部唯一标识（新建时可为空，克隆/预设时生成）',
    `type`              tinyint      NOT NULL DEFAULT 1 COMMENT '类型：0=预设(不可删除) 1=自定义',
    `source_preset_id`  bigint       DEFAULT NULL COMMENT '若基于某预设克隆创建，记录来源图标集ID',
    `icons`             text         COMMENT '图标key->SVG源码JSON字符串，如{"plus":"<svg>...</svg>"}，由JacksonTypeHandler序列化/反序列化；缺失的key由调用方回退到预设图案',
    `thumbnail_url`     varchar(500) DEFAULT '' COMMENT '缩略图地址',
    `is_active`         tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否生效：全表仅一条应为1',
    `sort`              int          NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`            varchar(500) DEFAULT '' COMMENT '备注',
    `creator`           varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图标集配置';
