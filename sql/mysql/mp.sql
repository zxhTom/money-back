-- 公众号标签表
CREATE TABLE `mp_tag` (
  -- 主键
  `id` bigint NOT NULL COMMENT '主键',

  -- 业务字段
  `tag_id` bigint DEFAULT NULL COMMENT '公众号标签 id，对应微信平台的标签ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
  `count` int DEFAULT '0' COMMENT '此标签下粉丝数，需要管理员同步后更新',
  `account_id` bigint NOT NULL COMMENT '公众号账号的编号，关联mp_account表id',
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号 appId，冗余字段',

  -- 审计字段（BaseDO）
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除：0-未删除，1-已删除',

  -- 主键和索引
  PRIMARY KEY (`id`),
  -- 业务索引
  KEY `idx_account_id` (`account_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_tag_id` (`tag_id`),
  -- 唯一约束：同一公众号下标签ID唯一
  UNIQUE KEY `uk_account_tag` (`account_id`, `tag_id`),
  -- 时间索引
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号标签表';


-- 公众号账号表
CREATE TABLE `mp_account` (
  -- 主键
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',

  -- 基础信息
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号名称',
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公众号账号',

  -- 微信配置信息
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号 appid',
  `app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号密钥',
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公众号token',
  `aes_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息加解密密钥',

  -- 其他信息
  `qr_code_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '二维码图片 URL',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',

  -- 状态字段（建议添加）
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-启用 1-停用',

  -- 多租户字段
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',

  -- 审计字段（TenantBaseDO）
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除：0-未删除 1-已删除',

  -- 主键和索引
  PRIMARY KEY (`id`),
  -- 唯一约束：appId 必须唯一
  UNIQUE KEY `uk_app_id` (`app_id`),
  -- 业务索引
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号账号表';

CREATE TABLE `mp_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `account_id` bigint NOT NULL COMMENT '公众号账号的编号',
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号 appId',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单标识',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单编号',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',

  -- 按钮操作字段
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '按钮类型',
  `url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '网页链接',
  `mini_program_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小程序的 appId',
  `mini_program_page_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小程序的页面路径',
  `article_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转图文的媒体编号',

  -- 消息内容字段
  `reply_message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息类型',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '回复的消息内容',
  `reply_media_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的媒体 id',
  `reply_media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的媒体 URL',
  `reply_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的标题',
  `reply_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '回复的描述',
  `reply_thumb_media_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的缩略图的媒体 id',
  `reply_thumb_media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的缩略图的媒体 URL',
  `reply_articles` json DEFAULT NULL COMMENT '回复的图文消息数组',
  `reply_music_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的音乐链接',
  `reply_hq_music_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的高质量音乐链接',

  -- BaseDO 审计字段
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',

  PRIMARY KEY (`id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_key` (`menu_key`),
  KEY `idx_type` (`type`),
  KEY `idx_sort` (`sort`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号菜单表';

CREATE TABLE `mp_auto_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account_id` bigint NOT NULL COMMENT '公众号账号的编号',
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号 appId',

  -- 回复类型
  `type` tinyint NOT NULL COMMENT '回复类型',

  -- ==================== 请求消息 ====================
  `request_keyword` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求的关键字',
  `request_match` tinyint DEFAULT NULL COMMENT '请求的关键字的匹配',
  `request_message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求的消息类型',

  -- ==================== 响应消息 ====================
  `response_message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复的消息类型',
  `response_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '回复的消息内容',
  `response_media_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的媒体 id',
  `response_media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的媒体 URL',
  `response_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的标题',
  `response_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '回复的描述',
  `response_thumb_media_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的缩略图的媒体 id',
  `response_thumb_media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的缩略图的媒体 URL',
  `response_articles` json DEFAULT NULL COMMENT '回复的图文消息',
  `response_music_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的音乐链接',
  `response_hq_music_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回复的高质量音乐链接',

  -- 状态和排序
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-启用 1-停用',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',

  -- BaseDO 审计字段
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',

  PRIMARY KEY (`id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_type` (`type`),
  KEY `idx_request_keyword` (`request_keyword`),
  KEY `idx_request_message_type` (`request_message_type`),
  KEY `idx_response_message_type` (`response_message_type`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号自动回复表';


CREATE TABLE `mp_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `msg_id` bigint DEFAULT NULL COMMENT '微信公众号消息 id',
  `account_id` bigint NOT NULL COMMENT '公众号账号的 ID',
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号 appid',
  `user_id` bigint DEFAULT NULL COMMENT '公众号粉丝的编号',
  `openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号粉丝标志',

  -- 消息基本信息
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息类型',
  `send_from` tinyint NOT NULL COMMENT '消息来源',

  -- ========= 普通消息内容 =========
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '消息内容',
  `media_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '媒体文件的编号',
  `media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '媒体文件的 URL',
  `recognition` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '语音识别后文本',
  `format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '语音格式',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `thumb_media_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '缩略图的媒体 id',
  `thumb_media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '缩略图的媒体 URL',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点击图文消息跳转链接',

  -- 地理位置信息
  `location_x` decimal(10,6) DEFAULT NULL COMMENT '地理位置维度',
  `location_y` decimal(10,6) DEFAULT NULL COMMENT '地理位置经度',
  `scale` decimal(5,2) DEFAULT NULL COMMENT '地图缩放大小',
  `label` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',

  -- 音乐和图文信息
  `articles` json DEFAULT NULL COMMENT '图文消息数组',
  `music_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '音乐链接',
  `hq_music_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '高质量音乐链接',

  -- ========= 事件推送 =========
  `event` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件类型',
  `event_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件 Key',

  -- BaseDO 审计字段
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',

  PRIMARY KEY (`id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_msg_id` (`msg_id`),
  KEY `idx_type` (`type`),
  KEY `idx_send_from` (`send_from`),
  KEY `idx_event` (`event`),
  KEY `idx_event_key` (`event_key`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号消息表';

-- 公众号粉丝表
CREATE TABLE `mp_user` (
  -- 主键
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',

  -- 微信身份信息
  `openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '粉丝标识，每个公众号对每个用户唯一',
  `union_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信生态唯一标识，跨公众号、小程序、APP通用',

  -- 关注状态
  `subscribe_status` tinyint NOT NULL DEFAULT 0 COMMENT '关注状态：0-未关注 1-已关注 2-已取消',
  `subscribe_time` datetime DEFAULT NULL COMMENT '关注时间',
  `unsubscribe_time` datetime DEFAULT NULL COMMENT '取消关注时间',

  -- 用户基本信息
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `head_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像地址',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '语言',
  `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',

  -- 标签信息
  `tag_ids` json DEFAULT NULL COMMENT '标签编号数组，存储用户所属的标签ID列表',

  -- 关联信息
  `account_id` bigint NOT NULL COMMENT '公众号账号的编号，关联mp_account表id',
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公众号 appId，冗余字段',

  -- BaseDO 审计字段
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除：0-未删除 1-已删除',

  -- 主键和索引
  PRIMARY KEY (`id`),
  -- 唯一约束：同一公众号下openid必须唯一
  UNIQUE KEY `uk_account_openid` (`account_id`, `openid`),
  -- 业务索引
  KEY `idx_openid` (`openid`),
  KEY `idx_union_id` (`union_id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_subscribe_status` (`subscribe_status`),
  KEY `idx_subscribe_time` (`subscribe_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公众号粉丝表';