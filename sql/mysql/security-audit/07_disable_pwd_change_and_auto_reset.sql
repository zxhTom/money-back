-- 1. 用户属性：禁止自主修改密码
ALTER TABLE `system_users`
    ADD COLUMN `disable_pwd_change` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁止用户自主修改密码(0-否 1-是)' AFTER `password_strength`;

-- 2. 自动重置密码用户配置表
CREATE TABLE `custom_auto_reset_pwd_user`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id`     bigint       NOT NULL COMMENT '用户ID',
    `creator`     varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`, `tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '定时自动重置密码 - 用户配置';
