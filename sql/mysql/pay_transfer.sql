-- ------------------------------------------------------------
-- 转账单 pay_transfer
-- 字段来源：yudao-module-pay/.../transfer/PayTransferDO.java + BaseDO
-- 状态字典：pay_transfer_status（见 ruoyi-vue-pro.sql system_dict_data）
-- ------------------------------------------------------------

DROP TABLE IF EXISTS `pay_transfer`;
CREATE TABLE `pay_transfer` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
    `no` varchar(64) NOT NULL COMMENT '转账单号',
    `app_id` bigint(20) NOT NULL COMMENT '应用编号，关联 pay_app.id',
    `channel_id` bigint(20) NOT NULL COMMENT '转账渠道编号，关联 pay_channel.id',
    `channel_code` varchar(32) NOT NULL COMMENT '转账渠道编码',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户编号',
    `user_type` tinyint(4) DEFAULT NULL COMMENT '用户类型',
    `merchant_transfer_id` varchar(64) NOT NULL COMMENT '商户转账单编号（同一应用下需业务保证与状态配合）',
    `subject` varchar(256) NOT NULL COMMENT '转账标题',
    `price` int(11) NOT NULL COMMENT '转账金额，单位：分',
    `user_account` varchar(255) NOT NULL COMMENT '收款人账号（如微信 openid、支付宝账号）',
    `user_name` varchar(64) DEFAULT NULL COMMENT '收款人姓名',
    `status` tinyint(4) NOT NULL COMMENT '转账状态，字典 pay_transfer_status',
    `success_time` datetime DEFAULT NULL COMMENT '转账成功时间',
    `notify_url` varchar(1024) DEFAULT NULL COMMENT '异步通知商户地址',
    `user_ip` varchar(64) NOT NULL COMMENT '用户 IP',
    `channel_extras` text COMMENT '渠道额外参数，JSON（JacksonTypeHandler）',
    `channel_transfer_no` varchar(64) DEFAULT NULL COMMENT '渠道侧转账单号',
    `channel_error_code` varchar(128) DEFAULT NULL COMMENT '调用渠道错误码',
    `channel_error_msg` varchar(256) DEFAULT NULL COMMENT '调用渠道错误提示',
    `channel_notify_data` text COMMENT '渠道同步/异步通知原文',
    `channel_package_info` text COMMENT '渠道 package 信息（如微信 JSAPI 调起用户确认收款）',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_no` (`no`) USING BTREE,
    KEY `idx_app_id` (`app_id`) USING BTREE,
    KEY `idx_app_merchant` (`app_id`, `merchant_transfer_id`) USING BTREE,
    KEY `idx_channel_id` (`channel_id`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转账单';
