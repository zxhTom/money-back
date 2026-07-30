-- ============================================================
-- 意见反馈表
-- 对应 Java: cn.iocoder.yudao.module.custom.dal.dataobject.feedback.FeedbackDO
-- 补建：原建表语句只写在 sql/mysql/feedback.sql，那个目录不会被自动执行，
-- 导致线上/本地库实际从未建过这张表，小程序"帮助与反馈"提交接口报错
-- （system_feedback 表不存在）。
-- ============================================================

CREATE TABLE IF NOT EXISTS `system_feedback` (
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `app_id`        varchar(64)  NOT NULL COMMENT '小程序AppID',
    `app_version`   varchar(32)  NOT NULL COMMENT '小程序版本号',
    `type`          varchar(32)  NOT NULL COMMENT '反馈类型：功能问题、体验建议、内容问题、其他反馈',
    `content`       varchar(500) NOT NULL COMMENT '问题描述',
    `contact_info`  varchar(128) DEFAULT NULL COMMENT '联系方式（手机号或邮箱）',
    `image_urls`    text         COMMENT '图片URL数组（JSON格式存储）',
    `user_id`       bigint       DEFAULT NULL COMMENT '用户ID（如果已登录）',
    `creator`       varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_app_id` (`app_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈表';
