-- 归档数据库初始化脚本
-- 所有表结构与主库保持一致，额外增加 archive_time 字段标记归档时间
-- 执行前请确认已创建 contract_archive 数据库

CREATE DATABASE IF NOT EXISTS `contract_archive` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `contract_archive`;

-- ============================================================
-- 合同归档表 (custom_contract)
-- ============================================================
CREATE TABLE IF NOT EXISTS `custom_contract` LIKE `contract`.`custom_contract`;
ALTER TABLE `custom_contract`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 支付订单归档表 (pay_order)
-- ============================================================
CREATE TABLE IF NOT EXISTS `pay_order` LIKE `contract`.`pay_order`;
ALTER TABLE `pay_order`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 支付退款归档表 (pay_refund)
-- ============================================================
CREATE TABLE IF NOT EXISTS `pay_refund` LIKE `contract`.`pay_refund`;
ALTER TABLE `pay_refund`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 钱包交易流水归档表 (pay_wallet_transaction)
-- ============================================================
CREATE TABLE IF NOT EXISTS `pay_wallet_transaction` LIKE `contract`.`pay_wallet_transaction`;
ALTER TABLE `pay_wallet_transaction`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 交易订单归档表 (trade_order)
-- ============================================================
CREATE TABLE IF NOT EXISTS `trade_order` LIKE `contract`.`trade_order`;
ALTER TABLE `trade_order`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 积分记录归档表 (member_point_record)
-- ============================================================
CREATE TABLE IF NOT EXISTS `member_point_record` LIKE `contract`.`member_point_record`;
ALTER TABLE `member_point_record`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 会员经验记录归档表 (member_experience_record)
-- ============================================================
CREATE TABLE IF NOT EXISTS `member_experience_record` LIKE `contract`.`member_experience_record`;
ALTER TABLE `member_experience_record`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 分销佣金记录归档表 (trade_brokerage_record)
-- ============================================================
CREATE TABLE IF NOT EXISTS `trade_brokerage_record` LIKE `contract`.`trade_brokerage_record`;
ALTER TABLE `trade_brokerage_record`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- 库存记录归档表 (erp_stock_record)
-- ============================================================
CREATE TABLE IF NOT EXISTS `erp_stock_record` LIKE `contract`.`erp_stock_record`;
ALTER TABLE `erp_stock_record`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);

-- ============================================================
-- AI 聊天消息归档表 (ai_chat_message)
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_chat_message` LIKE `contract`.`ai_chat_message`;
ALTER TABLE `ai_chat_message`
    ADD COLUMN IF NOT EXISTS `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    ADD INDEX IF NOT EXISTS `idx_archive_time` (`archive_time`);
