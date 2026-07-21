-- ============================================================
-- 文案系统初始种子数据（幂等版：可重复执行，配合自动迁移器）
-- 来源：contract-miniprogram 仓库 11 个 lang/safe/*.js 文件的一次性提取
-- 对应表：custom_text_profile / custom_text_item（Task 1.2 已建，见 31_text_profile_table.sql）
-- profile_id 通过子查询 (SELECT id FROM custom_text_profile WHERE code=...) 获取，不使用硬编码字面量
-- ============================================================

-- 1. 默认文案套
INSERT INTO `custom_text_profile`
    (`name`, `code`, `seed_from`, `is_active`, `sort`, `remark`, `creator`, `create_time`)
SELECT '默认文案', 'default-safe', 'safe', 1, 1, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_profile` WHERE `code` = 'default-safe');

-- 2.1 首页 index.js -> page_key='index' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.pageTitle', '美好约定', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.wantToSign', '创建约定', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.wantToSign' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.programName', '闪电风速', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.programName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.signGreeting', '让约定更安心', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.signGreeting' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.wantToSupplement', '补充约定', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.wantToSupplement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.supplementSubtitle1', '朋友请帮忙', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.supplementSubtitle1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.supplementSubtitle2', '我们来实现', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.supplementSubtitle2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.contractManagement', '我的约定', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.contractManagement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.manageSubtitle', '管理更省心', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.manageSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.healthQuery', '约定体检', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.healthQuery' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.healthQuerySubtitle', '一键生成美好度报告', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.healthQuerySubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.moreTitle', '更多服务', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.moreTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.bannerTitle', '分享建议 共创美好', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.bannerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.joinButton', '参与共创', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.joinButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.disclaimerTitle', '温馨提醒', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.disclaimerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.disclaimerContent', '本平台是记录美好约定的温馨空间，因个人填写信息产生的相关事宜由当事人自行沟通处理，让我们共同维护这份美好。', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.disclaimerContent' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.2 合同主包 contract.js -> page_key='contract' (43 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.defaultNickname', '小伙伴', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.defaultNickname' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.welcomeText', '欢迎回来', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.welcomeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.borrowText', '我借出', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.borrowText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.lendText', '我借入', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.lendText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabAll', '全部', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabAll' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabPendingConfirm', '待确认', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabPendingPayment', '待完成', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabRepaid', '已完成', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabOverdue', '已延期', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabExpired', '已结束', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.searchPlaceholder', '搜索伙伴姓名...', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.searchPlaceholderDebtor', '搜索对方姓名...', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchPlaceholderDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.searchPlaceholderCreditor', '搜索对方姓名...', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchPlaceholderCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.searchClear', '×', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchClear' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.sortDefault', '默认排序', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortDefault' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.sortRepaymentDate', '完成日期', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.sortAmount', '金额', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.sortStartDate', '开始日期', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statsTotalContracts', '约定总数', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsTotalContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statsTotalAmount', '约定金额', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsTotalAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statsUnitContracts', '个', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsUnitContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statsUnitAmount', '元', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsUnitAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusPendingConfirm', '待确认', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusPendingPayment', '待完成', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusOverdue', '已延期', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusExpired', '已结束', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.contractTypeBorrow', '对方欠我', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractTypeBorrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.contractTypeLend', '我欠对方', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractTypeLend' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.labelDebtor', '对方姓名', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.labelCreditor', '对方姓名', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.labelInterestRate', '约定内容', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelInterestRate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.labelStartDate', '开始日', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.labelRepaymentDate', '完成日', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.labelExtendedRepayment', '📅 延期完成日', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelExtendedRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.btnShare', '分享', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnShare' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.btnDownload', '下载', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnDownload' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.btnSupplement', '补充', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnSupplement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.btnEdit', '编辑', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnEdit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.emptyText', '暂无约定记录', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.emptyDesc', '快去创建您的第一个约定吧', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.emptyDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.emptyAction', '创建约定', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.emptyAction' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.loadingText', '加载中...', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.fabNewContract', '新建约定', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.fabNewContract' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.3 底部导航 tab.js -> page_key='tab' (4 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'tab', '', 'tab.home', '首页', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.home' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'tab', '', 'tab.contract', '合同', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.contract' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'tab', '', 'tab.credit', '健康度查询', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.credit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'tab', '', 'tab.profile', '我的', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.profile' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.4 征信主包 credit.js -> page_key='credit' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.pageTitle', '伙伴信息查询', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.warningText', '请确保填写的信息准确无误，以便建立可靠的伙伴关系。', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.warningText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.realNameLabel', '伙伴姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.realNamePlaceholder', '请输入伙伴姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.idNumberLabel', '联系号码', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.idNumberLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.idNumberPlaceholder', '请输入联系号码', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.idNumberPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.idErrorText', '请填写正确的联系号码', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.idErrorText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.securityTip', '为建立更可靠的协作关系，建议填写完整信息。', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.securityTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.submitBtn', '确认查询', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.recentListTitle', '协作伙伴', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.recentListTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.headerName', '姓名', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.headerName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.headerId', '联系方式', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.headerId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.prevArrow', '〈', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.prevArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.nextArrow', '〉', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.nextArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.5 征信查询 credit-query.js -> page_key='credit.creditQuery' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.pageTitle', '伙伴信息查询', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.warningText', '请确保填写的信息准确无误，以便建立可靠的伙伴关系。', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.warningText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.realNameLabel', '伙伴姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.realNamePlaceholder', '请输入伙伴姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.idNumberLabel', '联系号码', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.idNumberLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.idNumberPlaceholder', '请输入联系号码', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.idNumberPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.idErrorText', '请填写正确的联系号码', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.idErrorText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.securityTip', '为建立更可靠的协作关系，建议填写完整信息。', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.securityTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.submitBtn', '确认查询', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.recentListTitle', '协作伙伴', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.recentListTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.headerName', '姓名', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.headerName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.headerId', '联系方式', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.headerId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.prevArrow', '〈', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.prevArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.nextArrow', '〉', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.nextArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.6 合同管理 contract-management.js -> page_key='contract.contractManagement' (51 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.defaultNickname', '小伙伴', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.defaultNickname' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.welcomeText', '欢迎回来', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.welcomeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.borrowText', '我借出', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.borrowText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.lendText', '我借入', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.lendText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabAll', '全部', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabAll' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabPendingConfirm', '待确认', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabPendingPayment', '待完成', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabPendingRepayment', '待还款', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabRepaid', '已完成', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabOverdue', '已延期', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabExpired', '已结束', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabRejected', '拒签', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.tabWithdrawn', '撤销', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.searchPlaceholder', '搜索伙伴姓名...', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.searchPlaceholderDebtor', '搜索对方姓名...', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchPlaceholderDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.searchPlaceholderCreditor', '搜索对方姓名...', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchPlaceholderCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.searchClear', '×', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchClear' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.sortDefault', '默认排序', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortDefault' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.sortRepaymentDate', '完成日期', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.sortAmount', '金额', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.sortStartDate', '开始日期', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statsTotalContracts', '约定总数', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsTotalContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statsTotalAmount', '约定金额', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsTotalAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statsUnitContracts', '个', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsUnitContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statsUnitAmount', '元', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsUnitAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusPendingConfirm', '待确认', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusPendingPayment', '待完成', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusPendingRepayment', '待还款', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusTodayPayment', '今日还款', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusTodayPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusRepaid', '已完成', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusOverdue', '已延期', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusExpired', '已结束', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusRejected', '拒签', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.statusWithdrawn', '撤销', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.contractTypeBorrow', '对方欠我', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.contractTypeBorrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.contractTypeLend', '我欠对方', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.contractTypeLend' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.labelDebtor', '对方姓名', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.labelCreditor', '对方姓名', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.labelInterestRate', '约定内容', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelInterestRate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.labelStartDate', '开始日', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.labelRepaymentDate', '完成日', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.labelExtendedRepayment', '📅 延期完成日', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelExtendedRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.btnShare', '分享', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnShare' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.btnDownload', '下载', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnDownload' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.btnSupplement', '补充', 450, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnSupplement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.btnEdit', '编辑', 460, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnEdit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.emptyText', '暂无约定记录', 470, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.emptyDesc', '快去创建您的第一个约定吧', 480, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.emptyDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.emptyAction', '创建约定', 490, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.emptyAction' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.loadingText', '加载中...', 500, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.fabNewContract', '新建约定', 510, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.fabNewContract' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.7 欠条确认 iou-confirm.js -> page_key='contract.iouConfirm' (22 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.pageTitle', '互助约定确认', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart1', '与', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart2', '达成互助约定，约定时间为', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart3', '至', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart4', '，双方承诺按照', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart4' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart5', '的方式，在约定时间内完成互助事项。', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart5' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.creditorLabel', '约定发起方:', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.creditorLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.creditorIdLabel', '联系方式:', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.creditorIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.debtorLabel', '约定参与方:', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.debtorLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.debtorIdLabel', '联系方式:', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.debtorIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.agreementText', '已阅读并同意', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.agreementText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.agreementLink', '互助约定', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.agreementLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmBtn', '确认约定', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.creditTip', '诚信互助共建美好', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.creditTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.modalTitle', '约定确认', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.modalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.modalCloseBtn', '×', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.modalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.modalMoreBtn', '...', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.modalMoreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.passwordTip', '密码为 6 位数字', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.passwordTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.doneText', '完成', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.doneText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.deleteText', 'X', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.deleteText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.8 分享合同 sharecontract.js -> page_key='contract.sharecontract' (12 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.dialogTitle', '发送给微信好友', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.dialogTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.contractTitle', '邀请您加入美好约定', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.contractTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.contentLabel', '约定内容', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.contentLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.completionDateLabel', '完成日期', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.completionDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeTitle', '扫描二维码查看详情', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeTip', '长按图片可以保存分享', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeLoading', '生成二维码中...', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeLoading' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeError', '生成二维码失败，请', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeError' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.retryButton', '重试', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.retryButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.closeButton', '关闭', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.closeButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.shareButton', '分享给好友', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.shareButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.confirmButton', '确认', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.confirmButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.9 补充合同 supplement-contract.js -> page_key='contract.supplementContract' (22 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.pageTitle', '完善约定', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.originalContractLabel', '原约定编号', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.originalContractLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.supplementTypeLabel', '补充类型', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.supplementTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newSalaryLabel', '调整后内容', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newSalaryLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newPositionLabel', '调整后事项', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newPositionLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newWorkLocationLabel', '调整后地点', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newWorkLocationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newDurationLabel', '调整后时间', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newDurationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.effectiveDateLabel', '生效日期', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.effectiveDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.reasonLabel', '补充说明', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.reasonLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.attachmentLabel', '相关材料', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.attachmentLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.originalContractPlaceholder', '请选择原约定', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.originalContractPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.supplementTypePlaceholder', '请选择补充类型', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.supplementTypePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newSalaryPlaceholder', '请输入调整后的内容', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newSalaryPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newPositionPlaceholder', '请输入调整后的事项', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newPositionPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newWorkLocationPlaceholder', '请输入调整后的地点', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newWorkLocationPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.newDurationPlaceholder', '请选择调整后的时间', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newDurationPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.effectiveDatePlaceholder', '请选择补充生效日期', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.effectiveDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.reasonPlaceholder', '请详细说明补充约定的原因', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.reasonPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.uploadPlaceholder', '点击上传相关材料（选填）', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.uploadPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.removeFileText', '删除', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.removeFileText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.submitBtnText', '提交补充', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.supplementContract', '', 'contract.supplementContract.fileNameText', '文件名', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.fileNameText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.10 步骤引导 step.js -> page_key='contract.step' (20 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.headerTitle', '选择您的角色', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.headerSubtitle', '请根据您在约定中的身份选择', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.headerSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderCardTitle', '帮助方', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderCardBadge', '提供支持', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderCardBadge' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderDesc1', '作为支持提供方', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderDesc1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderDesc2', '发起互助约定', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderDesc2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderFeature1', '• 设置支持内容', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderFeature1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderFeature2', '• 约定完成时间', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderFeature2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderFeature3', '• 跟进互助进度', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderFeature3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.lenderEnterText', '进入帮助方页面', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderEnterText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerCardTitle', '接收方', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerCardBadge', '获得支持', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerCardBadge' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerDesc1', '作为支持接收方', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerDesc1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerDesc2', '参与互助约定', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerDesc2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerFeature1', '• 提交参与申请', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerFeature1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerFeature2', '• 查看进展记录', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerFeature2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerFeature3', '• 管理约定状态', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerFeature3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.borrowerEnterText', '进入接收方页面', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerEnterText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.tipTitle', '温馨提示', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.tipTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.step', '', 'contract.step.tipDesc', '请根据您在约定中的实际角色选择相应入口，不同角色的操作权限和功能会有所区别。', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.tipDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- 2.11 签约 sign-contract.js -> page_key='contract.signContract' (41 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.pageTitle', '创建约定', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.indebtedNameLabel', '约定参与方', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.indebtedIdLabel', '参与方联系方式', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.creditorNameLabel', '约定发起方', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.creditorIdLabel', '发起方联系方式', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.salaryLabel', '约定事项', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.salaryLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.annualRateLabel', '重要程度', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.annualRateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.contractTypeLabel', '完成计划', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.contractTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.startDateLabel', '开始日期', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.startDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.endDateLabel', '完成日期', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.endDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.reasonLabel', '约定说明', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.reasonLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.indebtedNamePlaceholder', '请输入参与方姓名', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.indebtedIdPlaceholder', '请输入参与方联系方式', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedIdPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.creditorNamePlaceholder', '请输入发起方姓名', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.creditorIdPlaceholder', '请输入发起方联系方式', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorIdPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.salaryPlaceholder', '请输入约定事项', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.salaryPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.annualRatePlaceholder', '请选择重要程度', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.annualRatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.contractTypePlaceholder', '请选择完成方式', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.contractTypePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.startDatePlaceholder', '请选择开始日期', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.startDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.endDatePlaceholder', '请选择完成日期', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.endDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.remarksPlaceholder', '请详细说明约定内容（选填）', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.remarksPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.unitSymbol', '级', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.unitSymbol' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.quickLabel', '快速选择：', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.quickBtn6Days', '6天后', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn6Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.quickBtn7Days', '7天后', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn7Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.quickBtn10Days', '10天后', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn10Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.quickBtn14Days', '14天后', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn14Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.chooseFileText', '选择文件', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.chooseFileText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.fileSizeText', '文件大小:', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.fileSizeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.uploadProgressText', '上传进度:', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.uploadProgressText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.uploadSuccessText', '上传成功！', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.uploadSuccessText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.uploadFailText', '上传失败！', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.uploadFailText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.serverResponseText', '服务器返回:', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.serverResponseText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.errorInfoText', '错误信息:', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.errorInfoText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.radioOption1', '当面确认', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.radioOption2', '提前准备', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.radioOption3', '互相帮助', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.radioOption4', '物品交换', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption4' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.radioOption5', '其他方式', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption5' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.submitBtnText', '提交约定', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.signContract', '', 'contract.signContract.nextStepBtnText', '继续', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.nextStepBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

