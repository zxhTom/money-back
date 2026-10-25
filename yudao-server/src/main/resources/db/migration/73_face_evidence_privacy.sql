-- 人脸存证私有化表及历史材料隔离表
-- 编号：73_face_evidence_privacy.sql

CREATE TABLE IF NOT EXISTS `custom_face_evidence` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id`    BIGINT       NOT NULL DEFAULT 1 COMMENT '租户编号',
    `user_id`      BIGINT       NOT NULL COMMENT '用户编号',
    `session_hash` VARCHAR(64)  NOT NULL COMMENT '会话摘要（SHA256(verifyToken)）',
    `media_type`   VARCHAR(32)  NOT NULL COMMENT '媒体类型：video / best_image',
    `path`         VARCHAR(255) NOT NULL COMMENT '逻辑存储路径 private/face-evidence/<uuid>.<ext>',
    `content`      MEDIUMBLOB   NOT NULL COMMENT '存证二进制数据',
    `size`         INT          NOT NULL COMMENT '数据大小（字节）',
    `sha256`       VARCHAR(64)  NOT NULL COMMENT '二进制内容SHA256摘要',
    `creator`      VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_session_media` (`tenant_id`, `session_hash`, `media_type`),
    KEY `idx_tenant_user` (`tenant_id`, `user_id`),
    KEY `idx_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人脸核身私有存证表';

CREATE TABLE IF NOT EXISTS `infra_file_quarantine` (
    `id`             BIGINT                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_id`      BIGINT                  NOT NULL COMMENT '存储配置ID',
    `path_hash`      CHAR(64)                NOT NULL COMMENT '规范化路径SHA256哈希（唯一索引）',
    `canonical_path` VARBINARY(2048)         NOT NULL COMMENT '规范化路径原始字节（覆盖中文与特殊字符）',
    `content_hash`   VARCHAR(64)             DEFAULT NULL COMMENT '内容SHA256摘要',
    `reason`         VARCHAR(255)            NOT NULL DEFAULT '历史人脸存证隔离' COMMENT '隔离原因',
    `creator`        VARCHAR(64)             DEFAULT '' COMMENT '创建者',
    `create_time`    DATETIME                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        VARCHAR(64)             DEFAULT '' COMMENT '更新者',
    `update_time`    DATETIME                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        BIT(1)                  NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_path_hash` (`config_id`, `path_hash`),
    KEY `idx_content_hash` (`content_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='文件存储历史隔离清单表';
