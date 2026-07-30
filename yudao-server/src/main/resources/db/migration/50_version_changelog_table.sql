-- ============================================================
-- 版本升级说明
-- 对应 Java: cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_version_changelog` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `version`       varchar(32)  NOT NULL COMMENT '对应小程序 accountInfo.miniProgram.version',
    `title`         varchar(100) NOT NULL COMMENT '公告标题',
    `content`       text         NOT NULL COMMENT '公告正文（纯文本，支持换行）',
    `enabled`       tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否发布：0-下线 1-发布',
    `creator`       varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版本升级说明';

ALTER TABLE `system_users` ADD COLUMN `last_seen_changelog_version` VARCHAR(32) DEFAULT NULL
    COMMENT '该用户最后一次确认已读的版本升级说明版本号' AFTER `invite_enabled`;
