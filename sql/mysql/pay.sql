DROP TABLE IF EXISTS `pay_app`;
CREATE TABLE `pay_app` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '应用编号，数据库自增',
                           `app_key` VARCHAR(64) NOT NULL COMMENT '应用标识',
                           `name` VARCHAR(64) NOT NULL COMMENT '应用名',
                           `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（参见 CommonStatusEnum）',
                           `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                           `order_notify_url` VARCHAR(255) DEFAULT NULL COMMENT '支付结果回调地址',
                           `refund_notify_url` VARCHAR(255) DEFAULT NULL COMMENT '退款结果回调地址',
                           `transfer_notify_url` VARCHAR(255) DEFAULT NULL COMMENT '转账结果回调地址',
                           `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
                           `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
                           `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_app_key` (`app_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付应用表';

-- -----------------------------------------------------
-- Table structure for `pay_channel`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pay_channel`;
CREATE TABLE `pay_channel` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '渠道编号',
                               `code` varchar(32) NOT NULL COMMENT '渠道编码',
                               `status` tinyint(4) NOT NULL COMMENT '状态',
                               `fee_rate` double NOT NULL DEFAULT '0' COMMENT '渠道费率，单位：百分比',
                               `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                               `app_id` bigint(20) NOT NULL COMMENT '应用编号',
                               `config` text NOT NULL COMMENT '支付渠道配置，JSON 格式',
                               `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                               `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付渠道';

-- -----------------------------------------------------
-- Table structure for `pay_demo_order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pay_demo_order`;
CREATE TABLE `pay_demo_order` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单编号',
                                  `user_id` bigint(20) NOT NULL COMMENT '用户编号',
                                  `spu_id` bigint(20) NOT NULL COMMENT '商品编号',
                                  `spu_name` varchar(255) NOT NULL COMMENT '商品名称',
                                  `price` int(11) NOT NULL COMMENT '价格，单位：分',
                                  `pay_status` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否支付',
                                  `pay_order_id` bigint(20) DEFAULT NULL COMMENT '支付订单编号',
                                  `pay_time` datetime DEFAULT NULL COMMENT '付款时间',
                                  `pay_channel_code` varchar(32) DEFAULT NULL COMMENT '支付渠道',
                                  `pay_refund_id` bigint(20) DEFAULT NULL COMMENT '支付退款单号',
                                  `refund_price` int(11) DEFAULT NULL COMMENT '退款金额，单位：分',
                                  `refund_time` datetime DEFAULT NULL COMMENT '退款完成时间',
                                  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='示例订单';

-- -----------------------------------------------------
-- Table structure for `pay_notify_log`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pay_notify_log`;
CREATE TABLE `pay_notify_log` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志编号',
                                  `task_id` bigint(20) NOT NULL COMMENT '通知任务编号',
                                  `notify_times` int(11) NOT NULL COMMENT '第几次被通知',
                                  `response` text NOT NULL COMMENT 'HTTP 响应结果',
                                  `status` tinyint(4) NOT NULL COMMENT '支付通知状态',
                                  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付通知 App 的日志';

-- -----------------------------------------------------
-- Table structure for `pay_notify_task`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pay_notify_task`;
CREATE TABLE `pay_notify_task` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
                                   `app_id` bigint(20) NOT NULL COMMENT '应用编号',
                                   `type` tinyint(4) NOT NULL COMMENT '通知类型',
                                   `data_id` bigint(20) NOT NULL COMMENT '数据编号',
                                   `merchant_order_id` varchar(64) DEFAULT NULL COMMENT '商户订单编号',
                                   `merchant_refund_id` varchar(64) DEFAULT NULL COMMENT '商户退款编号',
                                   `merchant_transfer_id` varchar(64) DEFAULT NULL COMMENT '商户转账编号',
                                   `status` tinyint(4) NOT NULL COMMENT '通知状态',
                                   `next_notify_time` datetime NOT NULL COMMENT '下一次通知时间',
                                   `last_execute_time` datetime DEFAULT NULL COMMENT '最后一次执行时间',
                                   `notify_times` int(11) NOT NULL COMMENT '当前通知次数',
                                   `max_notify_times` int(11) NOT NULL COMMENT '最大可通知次数',
                                   `notify_url` varchar(1024) NOT NULL COMMENT '通知地址',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                   `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户支付、退款等的通知';

-- -----------------------------------------------------
-- Table structure for `pay_order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单编号',
                             `app_id` bigint(20) NOT NULL COMMENT '应用编号',
                             `channel_id` bigint(20) DEFAULT NULL COMMENT '渠道编号',
                             `channel_code` varchar(32) DEFAULT NULL COMMENT '渠道编码',
                             `user_id` bigint(20) DEFAULT NULL COMMENT '用户编号',
                             `user_type` tinyint(4) DEFAULT NULL COMMENT '用户类型',
                             `merchant_order_id` varchar(64) NOT NULL COMMENT '商户订单编号',
                             `subject` varchar(255) NOT NULL COMMENT '商品标题',
                             `body` varchar(255) DEFAULT NULL COMMENT '商品描述信息',
                             `notify_url` varchar(1024) NOT NULL COMMENT '异步通知地址',
                             `price` int(11) NOT NULL COMMENT '支付金额，单位：分',
                             `channel_fee_rate` double DEFAULT '0' COMMENT '渠道手续费，单位：百分比',
                             `channel_fee_price` int(11) DEFAULT '0' COMMENT '渠道手续金额，单位：分',
                             `status` tinyint(4) NOT NULL COMMENT '支付状态',
                             `user_ip` varchar(64) NOT NULL COMMENT '用户 IP',
                             `expire_time` datetime NOT NULL COMMENT '订单失效时间',
                             `success_time` datetime DEFAULT NULL COMMENT '订单支付成功时间',
                             `extension_id` bigint(20) DEFAULT NULL COMMENT '支付成功的订单拓展单编号',
                             `no` varchar(64) DEFAULT NULL COMMENT '支付成功的外部订单号',
                             `refund_price` int(11) NOT NULL DEFAULT '0' COMMENT '退款总金额，单位：分',
                             `channel_user_id` varchar(255) DEFAULT NULL COMMENT '渠道用户编号',
                             `channel_order_no` varchar(64) DEFAULT NULL COMMENT '渠道订单号',
                             `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单';

-- -----------------------------------------------------
-- Table structure for `pay_order_extension`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pay_order_extension`;
CREATE TABLE `pay_order_extension` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单拓展编号',
                                       `no` varchar(64) NOT NULL COMMENT '外部订单号',
                                       `order_id` bigint(20) NOT NULL COMMENT '订单号',
                                       `channel_id` bigint(20) NOT NULL COMMENT '渠道编号',
                                       `channel_code` varchar(32) NOT NULL COMMENT '渠道编码',
                                       `user_ip` varchar(64) NOT NULL COMMENT '用户 IP',
                                       `status` tinyint(4) NOT NULL COMMENT '支付状态',
                                       `channel_extras` text COMMENT '支付渠道的额外参数，JSON 格式',
                                       `channel_error_code` varchar(128) DEFAULT NULL COMMENT '调用渠道的错误码',
                                       `channel_error_msg` varchar(256) DEFAULT NULL COMMENT '调用渠道报错时，错误信息',
                                       `channel_notify_data` text COMMENT '支付渠道的同步/异步通知的内容',
                                       `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单拓展';

-- -----------------------------------------------------
-- Table structure for `pay_refund`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pay_refund`;
CREATE TABLE `pay_refund` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '退款单编号',
                              `no` varchar(64) NOT NULL COMMENT '外部退款号',
                              `app_id` bigint(20) NOT NULL COMMENT '应用编号',
                              `channel_id` bigint(20) NOT NULL COMMENT '渠道编号',
                              `channel_code` varchar(32) NOT NULL COMMENT '渠道编码',
                              `order_id` bigint(20) NOT NULL COMMENT '支付订单编号',
                              `order_no` varchar(64) NOT NULL COMMENT '支付订单的外部订单号',
                              `user_id` bigint(20) NOT NULL COMMENT '用户编号',
                              `user_type` tinyint(4) NOT NULL COMMENT '用户类型',
                              `merchant_order_id` varchar(64) NOT NULL COMMENT '商户订单编号',
                              `merchant_refund_id` varchar(64) NOT NULL COMMENT '商户退款订单号',
                              `notify_url` varchar(1024) NOT NULL COMMENT '异步通知商户地址',
                              `status` tinyint(4) NOT NULL COMMENT '退款状态',
                              `pay_price` int(11) NOT NULL COMMENT '支付金额，单位：分',
                              `refund_price` int(11) NOT NULL COMMENT '退款金额，单位：分',
                              `reason` varchar(256) NOT NULL COMMENT '退款原因',
                              `user_ip` varchar(64) DEFAULT NULL COMMENT '用户 IP',
                              `channel_order_no` varchar(64) NOT NULL COMMENT '渠道订单号',
                              `channel_refund_no` varchar(64) DEFAULT NULL COMMENT '渠道退款单号',
                              `success_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '退款成功时间',
                              `channel_error_code` varchar(128) DEFAULT NULL COMMENT '渠道调用报错的错误码',
                              `channel_error_msg` varchar(256) DEFAULT NULL COMMENT '渠道调用报错的错误信息',
                              `channel_notify_data` text COMMENT '支付渠道的同步/异步通知的内容',
                              `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                              `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款订单';

DROP TABLE IF EXISTS `pay_demo_withdraw`;
CREATE TABLE `pay_demo_withdraw` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提现单编号，自增',
                                     `subject` varchar(255) NOT NULL COMMENT '提现标题',
                                     `price` int(11) NOT NULL COMMENT '提现金额，单位：分',
                                     `user_account` varchar(255) NOT NULL COMMENT '收款人账号',
                                     `user_name` varchar(255) NOT NULL COMMENT '收款人姓名',
                                     `type` tinyint(4) NOT NULL COMMENT '提现方式',
                                     `status` tinyint(4) NOT NULL COMMENT '提现状态',
                                     `pay_transfer_id` bigint(20) DEFAULT NULL COMMENT '转账单编号',
                                     `transfer_channel_code` varchar(50) DEFAULT NULL COMMENT '转账渠道',
                                     `transfer_time` datetime DEFAULT NULL COMMENT '转账成功时间',
                                     `transfer_error_msg` text COMMENT '转账错误提示',
                                     `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                     `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_pay_transfer_id` (`pay_transfer_id`),
                                     KEY `idx_transfer_channel_code` (`transfer_channel_code`),
                                     KEY `idx_status` (`status`),
                                     KEY `idx_create_time` (`create_time`),
                                     KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例提现表';

