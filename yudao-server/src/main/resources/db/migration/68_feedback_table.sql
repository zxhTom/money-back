CREATE TABLE system_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    app_id VARCHAR(64) NOT NULL COMMENT '小程序AppID',
    app_version VARCHAR(32) NOT NULL COMMENT '小程序版本号',
    type VARCHAR(32) NOT NULL COMMENT '反馈类型：功能问题、体验建议、内容问题、其他反馈',
    content VARCHAR(500) NOT NULL COMMENT '问题描述',
    contact_info VARCHAR(128) DEFAULT NULL COMMENT '联系方式（手机号或邮箱）',
    image_urls TEXT DEFAULT NULL COMMENT '图片URL数组（JSON格式存储）',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID（如果已登录）',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_app_id (app_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT='反馈表';

