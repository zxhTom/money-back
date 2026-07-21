-- ============================================================
-- 文案系统种子数据补齐（幂等版：可重复执行，配合自动迁移器）
-- Task 8.4：补齐"默认文案"（default-safe）套里 7 个原始页面缺失的 42 条陈旧文案
-- 背景：Task 5.1 灌种子（263条/11页面）时间点早于 Phase 6（6.1/6.2/6.4）
--      给这些同一批页面追加的"漏网"文案，数据库从未同步过这些追加内容。
-- 对应表：custom_text_profile / custom_text_item（Task 1.2 已建，见 31_text_profile_table.sql）
-- profile_id 通过子查询 (SELECT id FROM custom_text_profile WHERE code=...) 获取，不使用硬编码字面量
-- 只追加缺失 key，不修改/删除 default-safe 已有的 263+487 条数据，不涉及 default-offcial 套
-- ============================================================

-- B.1 首页 index.js -> page_key='index'（补齐 6 条：Task 6.1 公众号关注引导弹窗文案）
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.officialModalTitle', '提示', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.officialGuideTitle', '请关注我们的公众号', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialGuideTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.officialQrcodeTip', '长按识别二维码关注', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialQrcodeTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.officialAccountLabel', '微信号：', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialAccountLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.officialCopyText', '点击复制', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialCopyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'index', '', 'index.officialModalConfirm', '我知道了', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialModalConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- B.2 合同管理（主包）contract.js -> page_key='contract'（补齐 9 条：Task 6.1 状态标签/剩余天数）
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabPendingRepayment', '待还款', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabRejected', '拒签', 450, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.tabWithdrawn', '撤销', 460, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusPendingRepayment', '待还款', 470, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusTodayPayment', '今日还款', 480, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusTodayPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusRepaid', '已还款', 490, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusRejected', '拒签', 500, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.statusWithdrawn', '撤销', 510, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract', '', 'contract.labelDaysLeft', '剩余天数', 520, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelDaysLeft' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- B.3 征信查询（主包）credit.js -> page_key='credit'（补齐 6 条：Task 6.1 日历功能文案）
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.calendarPageTitle', '我的日历', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.calendarPageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.todayEventsTitle', '今日事件', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.todayEventsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.noEventsText', '今天没有安排事件', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.noEventsText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.noEventsDesc', '点击+号添加新事件', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.noEventsDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.addEventText', '添加事件', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.addEventText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit', '', 'credit.unlockIdManualInput', '改用手动输入', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.unlockIdManualInput' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- B.4 征信查询（子包）credit-query.js -> page_key='credit.creditQuery'（补齐 6 条：Task 6.4 同源日历功能文案）
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.calendarPageTitle', '我的日历', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.calendarPageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.todayEventsTitle', '今日事件', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.todayEventsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.noEventsText', '今天没有安排事件', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.noEventsText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.noEventsDesc', '点击+号添加新事件', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.noEventsDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.addEventText', '添加事件', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.addEventText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditQuery', '', 'credit.creditQuery.unlockIdManualInput', '改用手动输入', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.unlockIdManualInput' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- B.5 合同管理（子包）contract-management.js -> page_key='contract.contractManagement'（补齐 1 条：labelDaysLeft）
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractManagement', '', 'contract.contractManagement.labelDaysLeft', '剩余天数', 520, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelDaysLeft' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- B.6 欠条确认 iou-confirm.js -> page_key='contract.iouConfirm'（补齐 11 条：Task 6.2 密码弹窗/协议弹窗/文档导出文案）
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.rejectBtn', '拒绝签署', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.rejectBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.passwordSubtitle', '验证身份以继续操作', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.passwordSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.passwordLabel', '支付密码', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.protocolModalTitle', '相关协议', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.protocolModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.protocolModalCloseBtn', '×', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.protocolModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.loanAgreementOptionText', '借款协议', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.loanAgreementOptionText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmDocumentOptionText', '合同确认书', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmDocumentOptionText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.loanAgreementCloseBtn', '关闭', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.loanAgreementCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.loanAgreementExportBtn', '导出PDF', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.loanAgreementExportBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmDocumentCloseBtn', '关闭', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmDocumentCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmDocumentExportBtn', '导出PDF', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmDocumentExportBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- B.7 分享弹窗 sharecontract.js -> page_key='contract.sharecontract'（补齐 3 条：Task 6.2 支付/金额/完成日期文案）
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.payButton', '去支付', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.payButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.amountUnit', '元', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.amountUnit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.sharecontract', '', 'contract.sharecontract.repaymentDateLabel', '完成日期', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.repaymentDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
