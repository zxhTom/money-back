-- ============================================================
-- 文案系统扩展种子数据（幂等版：可重复执行，配合自动迁移器）
-- Task 8.3：事项A补齐"默认文案"套缺失的26个页面 + 事项B新建"官方文案"套覆盖全部37个页面
-- 对应表：custom_text_profile / custom_text_item（Task 1.2 已建，见 31_text_profile_table.sql）
-- profile_id 通过子查询 (SELECT id FROM custom_text_profile WHERE code=...) 获取，不使用硬编码字面量
-- ============================================================

-- ------------------------------------------------------------
-- 事项A：补齐"默认文案"（default-safe）缺失的 26 个页面
-- ------------------------------------------------------------

-- ============================================================
-- Task 8.3 事项A：补齐"默认文案"套（default-safe）缺失的 26 个页面
-- 来源：contract-miniprogram 仓库 26 个页面的 lang/safe/*.js 文件
-- 幂等追加，不影响已有 263 条数据（11 个页面）
-- ============================================================

-- A.1 我的 -> page_key='profile' (41 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.notLoggedInNickname', '未登录', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.notLoggedInNickname' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.loginBtn', '点击登录', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.loginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuEditProfile', '编辑资料', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuEditProfile' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuSettings', '设置', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSettings' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuChangePassword', '密码修改', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuChangePassword' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuChangePayPassword', '修改支付密码', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuChangePayPassword' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuFaceVerify', '肖像认证', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuFaceVerify' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuInviteCode', '我的邀请码', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuInviteCode' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuDashboard', '数据大盘', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuDashboard' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuDashboardChart', '数据图表', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuDashboardChart' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuSecurityAlert', '安全告警', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSecurityAlert' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuSecurityBlacklist', 'IP 黑名单', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSecurityBlacklist' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuSecurityDiagnose', 'IP 诊断', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSecurityDiagnose' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuTestFaceAuth', '测试人脸识别成功', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestFaceAuth' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuTestCompleteInfo', '测试完善个人信息', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestCompleteInfo' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuTestIouConfirm', '测试确认合同', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestIouConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuTestOfficialAccountModal', '测试公众号未关注弹窗', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestOfficialAccountModal' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuContractManagement', '合同管理', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuContractManagement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuHelp', '帮助与反馈', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuAbout', '关于我们', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuAbout' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.menuContactService', '联系客服', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuContactService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.logoutBtn', '退出登录', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.logoutBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.closeIcon', '×', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.closeIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.changePasswordTitle', '修改密码', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.changePasswordTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.oldPasswordLabel', '原密码：', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.oldPasswordPlaceholder', '请输入原密码', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.newPasswordLabel', '新密码：', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.newPasswordPlaceholder', '请输入新密码', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.confirmPasswordLabel', '确认新密码：', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.confirmPasswordPlaceholder', '请再次输入新密码', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.passwordCancelBtn', '取消', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.passwordCancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.passwordConfirmBtn', '确定', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.passwordConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.changePayPasswordTitle', '修改支付密码', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.changePayPasswordTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.oldPayPasswordLabel', '原支付密码：', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.oldPayPasswordPlaceholder', '请输入原支付密码', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.newPayPasswordLabel', '新支付密码：', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.newPayPasswordPlaceholder', '请输入新支付密码（6位数字）', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.confirmPayPasswordLabel', '确认新支付密码：', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.confirmPayPasswordPlaceholder', '请再次输入新支付密码', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.payPasswordCancelBtn', '取消', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.payPasswordCancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'profile', '', 'profile.payPasswordConfirmBtn', '确定', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.payPasswordConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.2 合同详情 -> page_key='contract.contractDetail' (56 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.pageTitle', '合同详情', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.contractIdLabel', '编号: ', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.contractIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.borrowerRoleLabel', '借款人', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.borrowerRoleLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.lenderRoleLabel', '出借人', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.lenderRoleLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.idNoLabelPrefix', '身份证 ', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.idNoLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusTodayPayment', '今日还款', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusTodayPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusPendingConfirm', '待确认', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusPendingPayment', '待收款', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusPendingRepayment', '待还款', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusRepaid', '已还款', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusOverdue', '已逾期', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusExpired', '已失效', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusRejected', '拒签', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.statusWithdrawn', '撤销', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelDebtAmount', '欠款金额', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelDebtAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelAnnualRate', '年化利率', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelAnnualRate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelTotalInterest', '应收利息', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelTotalInterest' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelTotalAmount', '本息合计', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelTotalAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelRefundAmount', '已还金额', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelRefundAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelRemainingAmount', '待还金额', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelRemainingAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelRepaymentMethod', '还款方式', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelRepaymentMethod' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.repaymentMethodValue', '一次性还款', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.repaymentMethodValue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelStartDate', '起始日', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.labelEndDate', '到期日', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelEndDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.loanAgreementLabel', '借款协议', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.loanAgreementLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.viewLoanAgreementBtn', '查看借款协议', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.viewLoanAgreementBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.terminateContractLabel', '解除合同', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.terminateContractLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.terminateContractBtn', '解除合同', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.terminateContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.disputeText', '遇到争议怎么办?', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.extendBtn', '一键展期', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.extendBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.settleBtn', '一键销账', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.settleBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareContractBtn', '分享合同', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.withdrawContractBtn', '撤回合约', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.withdrawContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.confirmContractBtn', '确认合同', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.confirmContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.rejectSignBtn', '拒绝签署', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.rejectSignBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.agreementModalCloseBtn', '×', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.agreementModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.agreementModalCloseText', '关闭', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.agreementModalCloseText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.agreementModalExportBtn', '导出PDF', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.agreementModalExportBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalTitle', '合同详情', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalSubtitle', '风速合约小程序', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalCloseIcon', '×', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalCloseIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareTipLine1', '点击右上角分享给微信好友/朋友圈', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareTipLine1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareTipLine2', '或长按保存图片 使用二维码邀请', 450, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareTipLine2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareInviteSuffix', '在风速合约', 460, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareInviteSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareInviteLine2', '向您发起风速借条', 470, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareInviteLine2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareRateLabelPrefix', '年化利率: ', 480, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareRateLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareRepaymentDateLabelPrefix', '还款日期 ', 490, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareRepaymentDateLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalCloseBtn', '关闭弹窗', 500, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalShareBtn', '分享给好友', 510, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalShareBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.testButton1', '测试本人中转', 520, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.testButton1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.testButton2', '测试对方中转', 530, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.testButton2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.disputeModalTitle', '争议解决指引', 540, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.disputeModalCloseIcon', '×', 550, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeModalCloseIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractDetail', '', 'contract.contractDetail.disputeModalConfirmBtn', '我知道了', 560, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeModalConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.3 合同分享入口 -> page_key='contract.contractShareEntry' (11 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.title', '正在验证您的合同身份', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.subtitle', '请稍候...', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.subtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.loadingText', '正在加载合同信息，请稍候', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.readyText', '即将为您跳转到对应页面', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.readyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.backHomeBtn', '返回首页', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.backHomeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorMissingId', '缺少合同编号，无法识别二维码', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorMissingId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorNotLoggedIn', '请先登录后再扫码查看合同', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorNotLoggedIn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorNotFound', '未找到对应合同或合同已失效', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorNotFound' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorIdentityMismatch', '您无权限查看该合同，合同中的身份信息与您的账号信息不匹配，请联系合同双方确认', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorIdentityMismatch' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorLoadFailed', '合同信息加载失败，请稍后重试', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorLoadFailed' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorNoPermission', '您无权限查看该合同，请联系合同双方确认', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorNoPermission' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.4 选择债务人 -> page_key='contract.selectDebtor' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.tip', '请确保输入的姓名与对方真实姓名一致，否则合约无法生效。', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.tip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.realNameLabel', '真实姓名', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.realNamePlaceholder', '请输入真实姓名', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.idNumberLabel', '身份证号', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.idNumberLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.idNumberPlaceholder', '请输入身份证号', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.idNumberPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.idErrorTip', '请输入合法的身份证号', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.idErrorTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.desc', '为保证交易安全，建议您填写对方身份证号。', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.desc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.submitBtn', '提交', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.historyTitle', '最近往来', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.historyTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.tableHeaderName', '姓名', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.tableHeaderName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.tableHeaderId', '身份证号', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.tableHeaderId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeModeTitle', '选择联系人', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeModeTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeModeDesc', '请选择您要创建约定的联系人', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeModeDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeInfoText', '此功能暂未开放', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeInfoText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeInfoSubtext', '请联系管理员获取更多信息', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeInfoSubtext' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeBackBtn', '返回', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeBackBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.5 债务结算 -> page_key='contract.debtSettlement' (28 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.headerTitle', '合约销账', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountCardTitle', '销账金额', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.fullPaymentBtn', '全额销账', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.fullPaymentBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.summaryLabelTotal', '待还总额', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.summaryLabelTotal' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.inputLabelAmount', '本次销账金额', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.inputLabelAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.inputTipPrefix', '最多可销账 ', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.inputTipPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmount100', '100元', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmount100' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmount500', '500元', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmount500' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmount1000', '1000元', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmount1000' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmountFull', '全额', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmountFull' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountErrorInvalid', '请输入有效的金额', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountErrorInvalid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountErrorExceeds', '销账金额不能超过剩余待还金额', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountErrorExceeds' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountErrorTooSmall', '销账金额不能小于0.01元', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountErrorTooSmall' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.remarkCardTitle', '备注信息', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.remarkCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.remarkOptional', '（选填）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.remarkOptional' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.remarkPlaceholder', '请输入销账备注信息，如：部分还款、延期说明等...', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.remarkPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.previewLabel', '本次销账', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.previewLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.submitBtnLoadingText', '提交中...', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.submitBtnLoadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.submitBtnText', '确认销账', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.menuViewHistory', '查看历史记录', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.menuViewHistory' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.menuExportData', '导出数据', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.menuExportData' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.menuHelp', '使用帮助', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.menuHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordModalTitle', '请输入交易密码', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordModalSubtitle', '验证身份以继续操作', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordModalSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordLabel', '支付密码', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordHint', '请输入6位数字密码', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.doneText', '完成', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.doneText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.debtSettlement', '', 'contract.debtSettlement.deleteText', 'X', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.deleteText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.6 展期 -> page_key='contract.extension' (23 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.headerTitle', '展期申请', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.infoCardTitle', '展期信息', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.infoCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.labelAvailableAmount', '可展期金额', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.labelAvailableAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.labelCurrentDueDate', '当前到期时间', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.labelCurrentDueDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.extensionSettingsTitle', '展期设置', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.extensionSettingsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.limitText', '最长不超过10年', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.limitText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.newDueDateLabel', '新的还款日期', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.newDueDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.dueDatePlaceholder', '请选择还款日期', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.dueDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.extensionDaysLabel', '展期天数', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.extensionDaysLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.extensionTipText', '展期后将产生相应的展期费用', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.extensionTipText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.feeSectionTitle', '费用估算', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeSectionTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.feeLabelService', '展期服务费', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeLabelService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.feeLabelInterest', '展期利息', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeLabelInterest' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.feeLabelTotal', '总计费用', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeLabelTotal' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.agreementText', '我已阅读并同意', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.agreementText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.agreementLink1', '《展期服务协议》', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.agreementLink1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.agreementLink2', '《费用说明》', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.agreementLink2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.previewLabel', '预计费用', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.previewLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.submitBtnLoadingText', '提交中...', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.submitBtnLoadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.submitBtnText', '确认展期', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.menuHistory', '展期记录', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.menuHistory' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.menuContactService', '联系客服', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.menuContactService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'contract.extension', '', 'contract.extension.menuHelp', '使用帮助', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.menuHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.7 个人信息 -> page_key='user.userinfo' (24 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.pageTitle', '编辑个人信息', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.changeAvatarText', '更换头像', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.changeAvatarText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.realNameLabel', '真实姓名', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.requiredMark', '*', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.requiredMark' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.verifiedReadonlyTip', '（已认证，不可修改）', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.verifiedReadonlyTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.realNamePlaceholder', '请输入真实姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.nicknameLabel', '昵称', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.nicknameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.nicknamePlaceholder', '请输入昵称', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.nicknamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.genderLabel', '性别', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.genderLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.birthdayLabel', '生日', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.birthdayLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.mobileLabel', '手机号（选填）', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.mobileLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.mobilePlaceholder', '请输入手机号（选填）', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.mobilePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.emailLabel', '邮箱', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.emailLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.emailPlaceholder', '请输入邮箱地址', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.emailPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.idNoLabel', '身份证号', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.idNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.idNoPlaceholder', '请输入身份证号码', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.idNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.addressLabel', '居住地址', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.addressLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.addressPlaceholder', '请输入详细居住地址', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.addressPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.occupationLabel', '职业', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.occupationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.educationLabel', '学历', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.educationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.bioLabel', '个人简介', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.bioLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.bioPlaceholder', '请输入个人简介（选填）', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.bioPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.saveBtn', '保存修改', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.saveBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userinfo', '', 'user.userinfo.cancelBtn', '取消', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.cancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.8 设置 -> page_key='user.settings' (17 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.userDescText', '点击修改个人信息', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.userDescText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.groupGeneral', '通用设置', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupGeneral' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.themeLabel', '主题设置', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.themeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.clearCacheLabel', '清除缓存', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.clearCacheLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.languageLabel', '语言设置', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.languageLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.languageValue', '简体中文', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.languageValue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.groupHelp', '使用帮助', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.helpFeedbackLabel', '帮助与反馈', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.helpFeedbackLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.groupPrivacy', '隐私与安全', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupPrivacy' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.privacyPolicyLabel', '隐私政策', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.privacyPolicyLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.userAgreementLabel', '用户协议', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.userAgreementLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.changePasswordLabel', '修改密码', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.changePasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.groupAbout', '支持与关于', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupAbout' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.aboutUsLabel', '关于我们', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.aboutUsLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.checkUpdateLabel', '检查更新', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.checkUpdateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.customerServiceLabel', '联系客服', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.customerServiceLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.settings', '', 'user.settings.logoutBtn', '退出登录', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.logoutBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.9 关于我们 -> page_key='user.abouts' (10 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.versionLabel', '版本号', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.versionLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.updateTimeLabel', '更新时间', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.updateTimeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.introTitle', '应用介绍', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.introTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.checkUpdateText', '检查更新', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.checkUpdateText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.feedbackText', '意见反馈', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.feedbackText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.userAgreementText', '用户协议', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.userAgreementText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.privacyPolicyText', '隐私政策', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.privacyPolicyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.updateHintIcon', '↻', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.updateHintIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.copyrightPrefix', '©', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.copyrightPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.abouts', '', 'user.abouts.copyrightSuffix', '版权所有', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.copyrightSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.10 意见反馈 -> page_key='user.feedbacks' (23 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.searchPlaceholder', '请输入您遇到的问题', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.searchPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.searchBtn', '搜索', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.searchBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.quickActionsTitle', '常用问题', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.quickActionAccount', '账号问题', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionAccount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.quickActionPayment', '支付问题', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.quickActionFunction', '功能使用', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionFunction' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.quickActionOther', '其他问题', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionOther' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.helpSectionTitle', '问题分类', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.helpSectionTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.helpViewDetail', '查看详情', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.helpViewDetail' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.helpContactService', '联系客服', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.helpContactService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.feedbackFormTitle', '问题反馈', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackFormTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.feedbackTypeLabel', '反馈类型', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.feedbackContentLabel', '问题描述', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackContentLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.feedbackContentPlaceholder', '请详细描述您遇到的问题，帮助我们更好的解决', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackContentPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.feedbackImageLabel', '相关截图（选填）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackImageLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.deleteImageIcon', '×', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.deleteImageIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.addImageText', '添加图片', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.addImageText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.contactInfoLabel', '联系方式', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.contactInfoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.contactInfoPlaceholder', '请输入联系方式（选填）', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.contactInfoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.submitBtn', '提交反馈', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.contactCardTitle', '需要更多帮助？', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.contactCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.wechatAccountPrefix', '微信公众号：', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.wechatAccountPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.feedbacks', '', 'user.feedbacks.serviceEmailPrefix', '客服邮箱：', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.serviceEmailPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.11 人脸认证 -> page_key='user.faceAuth' (23 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.headerTitle', '身份认证', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.headerDesc', '请确认您的身份信息，完成实名认证', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.headerDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.realNameLabel', '真实姓名', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.autoFilledTip', '（已自动填入，不可修改）', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.autoFilledTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.realNamePlaceholder', '请输入您的真实姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.idNoLabel', '身份证号', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.idNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.idNoPlaceholder', '请输入18位身份证号码', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.idNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.tipIcon', 'ℹ️', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.tipTitle', '认证说明：', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.tipLine1', '1. 请确保姓名和身份证号准确无误', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipLine1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.tipLine2', '2. 点击确认后将进入人脸识别环节', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipLine2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.tipLine3', '3. 请保持网络畅通，按提示完成认证', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipLine3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.testStep1Btn', '1. 开始核验', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.testStep1Btn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.testStep2Btn', '2. 提交身份验证', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.testStep2Btn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.testStep3Btn', '3. 开始人脸识别', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.testStep3Btn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.confirmBtn', '确认认证', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.confirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.processingBtn', '处理中...', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.processingBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.footerTip', '🔒 您的信息安全加密，仅用于身份验证', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.footerTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.actionPanelTitle', '其他操作', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.actionPanelTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.actionPanelSub', '资料有误可先修改，或返回「我的」', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.actionPanelSub' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.editProfileBtn', '修改资料', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.editProfileBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.backToProfileBtn', '返回我的', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.backToProfileBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.faceAuth', '', 'user.faceAuth.reloginBtn', '⇄ 切换账号 · 重新登录', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.reloginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.12 网页视图 -> page_key='user.webView' (0 条)

-- A.13 认证结果 -> page_key='user.authResult' (8 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.loadingText', '正在验证身份信息，请稍候...', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.successText', '人脸识别验证已完成', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.successText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.successHint', '请返回原页面查看结果', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.successHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.resultPassedText', '✔ 核验通过', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.resultPassedText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.resultFailedPrefix', '✘ 核验未通过: ', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.resultFailedPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.failText', '流程中断或失败', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.failText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.backHomeBtn', '返回首页', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.backHomeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.authResult', '', 'user.authResult.retryBtn', '重新验证', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.retryBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.14 验证码 -> page_key='user.captcha' (10 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.backText', '返回', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.backText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.headerTitle', '安全验证', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.refreshBtn', '换一张', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.refreshBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.sliderHint', '拖动滑块完成拼图', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.sliderHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.clickTipPrefix', '请依次点击：', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.clickTipPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.loadingText', '加载验证码中...', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.retryBtn', '重新加载', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.retryBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.verifyingText', '验证中...', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.verifyingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.loadFailedText', '加载验证码失败', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.loadFailedText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.captcha', '', 'user.captcha.imageLoadFailedText', '图片加载失败', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.imageLoadFailedText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.15 隐私政策 -> page_key='user.privacyPolicy' (2 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.privacyPolicy', '', 'user.privacyPolicy.title', '隐私政策说明', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.privacyPolicy.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.privacyPolicy', '', 'user.privacyPolicy.updateTimePrefix', '最后更新日期：', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.privacyPolicy.updateTimePrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.16 用户协议 -> page_key='user.userAgreement' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.title', '身份信息使用授权协议', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.introText', '在您注册并填写个人信息前，请仔细阅读以下条款：', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.introText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.consentIntro', '本人知悉并同意，本次注册所提供的', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentIntro' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.consentHighlight1', '身份证信息', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentHighlight1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.consentParenthetical', '（含真实姓名）', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentParenthetical' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.consentPrefix', '，将被作为本人在【', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.consentSuffix', '】中的', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.consentHighlight2', '唯一主体身份标识', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentHighlight2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.consentPeriod', '。', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentPeriod' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.usageLabel', '信息用途说明：', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.usageLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.listItem1', '该身份信息将用于生成、关联及锁定您名下的所有电子合约，确保合约的法律效力与唯一性。', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.listItem1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.listItem2', '您可使用此身份信息快速查询、检索及管理您所签署的全部合约，提升使用体验。', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.listItem2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.listItem3', '程序后续所有与合约相关的功能与服务，均将基于此身份信息进行，请确保其真实、准确、有效。', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.listItem3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.importantNoticeLabel', '重要提示：', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.importantNoticeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.importantNoticeText', '身份信息一经提交将不可随意更改。因填写错误、虚假信息或他人信息所引发的一切法律责任和后果，均由您自行承担。', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.importantNoticeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.userAgreement', '', 'user.userAgreement.finalNotice', '点击"注册"即表示您已阅读、理解并同意以上全部内容。', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.finalNotice' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.17 维护中 -> page_key='user.maintenance' (15 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.pageTitle', '系统维护中', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.subtitle', '当前命中维护时间窗口，功能临时不可用', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.subtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.timeRangeLabel', '维护时间范围', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.timeRangeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.windowIdPrefix', '窗口ID：', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.windowIdPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.targetTypeLabel', '目标类型：', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.targetTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.statusLabelWithSep', ' ｜ 状态：', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.statusLabelWithSep' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.refreshingTip', '正在从接口刷新维护信息...', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.refreshingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.unitDays', '天', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitDays' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.unitHours', '时', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitHours' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.unitMinutes', '分', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitMinutes' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.unitSeconds', '秒', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitSeconds' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.tipsNotEnded', '请稍后重试，倒计时结束后会自动解除限制。', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.tipsNotEnded' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.tipsEnded', '维护窗口理论已结束，正在等待状态刷新。', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.tipsEnded' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.refreshBtn', '刷新状态', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.refreshBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.maintenance', '', 'user.maintenance.loginBtn', '返回登录页', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.loginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.18 服务不可用 -> page_key='user.serviceUnavailable' (6 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.title', '服务暂时不可用', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.desc', '系统正在进行自动恢复，请稍后重试', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.desc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.defaultTip', '服务暂时不可用，请稍后重试', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.defaultTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.checkingText', '正在检测服务状态...', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.checkingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.autoPollingText', '系统将自动轮询恢复状态', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.autoPollingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.retryBtn', '立即重试', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.retryBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.19 登录 -> page_key='auth.login' (44 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.backIcon', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.backIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.logoAppName', '合约服务', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.logoAppName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.logoSlogan', '安全便捷的合约管理平台', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.logoSlogan' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.formTitle', '欢迎回来', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.formTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.formSubtitle', '请登录您的账户', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.formSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.loginTypeTabLabel', '账号密码登录', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginTypeTabLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.usernameLabel', '登录账号', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.usernameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.usernamePlaceholder', '请输入登录账号（姓名或身份证号）', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.usernamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.accountHistorySavedTag', '已保存密码', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.accountHistorySavedTag' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.loginIdNoLabel', '身份证号', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginIdNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.loginIdNoPlaceholder', '请输入18位身份证号', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginIdNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.passwordLabel', '密码', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.passwordPlaceholder', '请输入密码', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.passwordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotPasswordLink', '忘记密码？', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotPasswordLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.rememberPasswordLabel', '记住密码（本地加密存储）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.rememberPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.loginBtn', '登录', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.noAccountText', '还没有账户？', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.noAccountText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.registerLink', '立即注册', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.registerLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.popupTitle', '完善个人信息', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.popupCloseBtn', '×', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.avatarEditTip', '点击更换头像', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.avatarEditTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.nicknameLabel', '昵称', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.nicknameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.nicknamePlaceholder', '请输入昵称', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.nicknamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.popupRealNameLabel', '真实姓名', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupRealNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.popupRealNamePlaceholder', '请输入真实姓名', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupRealNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.popupIdNoLabel', '身份证号', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupIdNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.popupIdNoPlaceholder', '请输入18位身份证号码', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupIdNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.genderLabel', '性别', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.genderLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.birthdayLabel', '生日', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.birthdayLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.submitCompleteBtn', '完成', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.submitCompleteBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotModalTitle', '忘记密码', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotModalCloseBtn', '×', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotPasswordTip', '请填写注册时的真实姓名与身份证号，重置后登录密码与支付密码将一并更新为新密码。', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotPasswordTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotRealNameLabel', '真实姓名', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotRealNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotRealNamePlaceholder', '请输入真实姓名', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotRealNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotIdNoLabel', '身份证号', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotIdNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotIdNoPlaceholder', '请输入身份证号（15-20位）', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotIdNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotNewPasswordLabel', '新密码', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotNewPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotNewPasswordPlaceholder', '请输入新密码（4-16位）', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotNewPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotConfirmPasswordLabel', '确认密码', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotConfirmPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotConfirmPasswordPlaceholder', '请再次输入新密码', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotConfirmPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotCancelBtn', '取消', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotCancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.forgotConfirmBtn', '确定', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.login', '', 'auth.login.requiredMark', '*', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.requiredMark' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.20 注册 -> page_key='auth.register' (31 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.logoAppName', '合约服务', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.logoAppName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.logoSlogan', '创建您的账户', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.logoSlogan' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.formTitle', '欢迎注册', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.formTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.formSubtitle', '请填写以下信息完成注册', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.formSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.realnameLabel', '姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.realnameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.realnamePlaceholder', '请输入姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.realnamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.idCardLabel', '身份证号', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.idCardPlaceholder', '请输入身份证号', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.inviteCodeLabel', '邀请码', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.inviteCodeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.inviteCodePlaceholder', '请输入邀请码（如有）', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.inviteCodePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.passwordLabel', '登录密码', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.confirmPasswordLabel', '确认登录密码', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.confirmPasswordPlaceholder', '请再次输入登录密码', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.payPasswordLabel', '支付密码', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.payPasswordPlaceholder', '请设置6位数字支付密码（不能连续）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.confirmPayPasswordLabel', '确认支付密码', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.confirmPayPasswordPlaceholder', '请再次输入支付密码', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.registerBtn', '注册', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.registerBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.haveAccountText', '已有账户？', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.haveAccountText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.loginLink', '立即登录', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.requiredMark', '*', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.requiredMark' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.realnameTipMinLength', '请输入至少2个字符的姓名', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.realnameTipMinLength' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.idCardTipLength', '请输入18位身份证号', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardTipLength' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.idCardTipInvalid', '身份证号格式不正确，请检查', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardTipInvalid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.loginPwdTipMinLength', '密码至少6位', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginPwdTipMinLength' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.loginPwdTipNumericOnly', '登录密码需为6位数字', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginPwdTipNumericOnly' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.loginPwdTipForbidPureNumber', '登录密码不能全为数字，请包含字母', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginPwdTipForbidPureNumber' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.payPwdTipInvalid', '请设置6位数字支付密码', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPwdTipInvalid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.payPwdTipWeak', '支付密码不能连续或过于简单（如123456、223344）', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPwdTipWeak' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.passwordPlaceholderNumeric', '请设置6位数字密码', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.passwordPlaceholderNumeric' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'auth.register', '', 'auth.register.passwordPlaceholderComplex', '请设置密码（字母+数字，至少6位）', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.passwordPlaceholderComplex' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.21 征信详情 -> page_key='credit.creditDetail' (17 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.idNoLabelPrefix', '身份证号：', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.idNoLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.tabStat', '借入统计', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tabStat' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.tabDetail', '借入明细', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tabDetail' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.tableHeaderStatus', '状态', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tableHeaderStatus' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.tableHeaderCount', '笔数', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tableHeaderCount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.tableHeaderAmount', '金额(元)', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tableHeaderAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.summaryLabel', '总计', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.summaryLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.emptyText', '没有更多了', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.contractIdPrefix', '合同号：', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.contractIdPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.amountPrefix', '合同金额：￥', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.amountPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.dueDatePrefix', '还款时间：', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.dueDatePrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.statusPendingConfirm', '待确认', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.statusPendingRepayment', '待还款', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.statusRepaid', '已还款', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.statusOverdue', '已逾期', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.statusExpired', '已失效', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'credit.creditDetail', '', 'credit.creditDetail.statusUnknown', '未知状态', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusUnknown' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.22 数据大盘 -> page_key='admin.dashboard' (15 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.loadingTip', '加载中...', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.sectionCoreMetrics', '核心指标', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.sectionCoreMetrics' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelTotalUsers', '总用户', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalUsers' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelTodayNewUsers', '今日新增', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTodayNewUsers' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelVerifiedUsers', '已认证', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelVerifiedUsers' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelActiveUsers7d', '7日活跃', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelActiveUsers7d' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.sectionContractStats', '合同统计', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.sectionContractStats' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelTotalContracts', '总合同', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelTodayNewContracts', '今日新增', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTodayNewContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelOverdueContracts', '逾期', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelOverdueContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelTotalContractAmount', '总金额', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalContractAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.sectionPaymentStats', '支付统计', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.sectionPaymentStats' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelTotalPaySuccess', '累计成功', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalPaySuccess' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelTodayPayAmount', '今日金额', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTodayPayAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboard', '', 'admin.dashboard.labelMonthPayAmount', '本月金额', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelMonthPayAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.23 数据图表 -> page_key='admin.dashboardChart' (12 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.loadingTip', '加载中...', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.swipeHint', '← 左右滑动切换图表 →', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.swipeHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle0', '7日用户趋势', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle0' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle1', '合同状态分布', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle2', '合同新增趋势', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle3', '支付金额趋势', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle4', '人脸认证占比', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle4' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.legendNewUser', '新增用户', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.legendNewUser' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.legendVerified', '已认证', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.legendVerified' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.legendUnverified', '未认证', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.legendUnverified' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.noDataText', '暂无数据', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.noDataText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.dashboardChart', '', 'admin.dashboardChart.donutTotalLabel', '总用户', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.donutTotalLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.24 安全告警 -> page_key='admin.securityAlert' (11 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.labelAll', '全部', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.labelAll' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.labelUnhandled', '未处理', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.labelUnhandled' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.labelHandled', '已处理', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.labelHandled' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.loadingTip', '加载中...', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.handleBtnText', '处理', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.handleBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.noMoreText', '已加载全部', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.noMoreText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.emptyText', '暂无告警', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.severityLow', '低', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityLow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.severityMedium', '中', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityMedium' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.severityHigh', '高', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityHigh' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityAlert', '', 'admin.securityAlert.severityCritical', '严重', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityCritical' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.25 安全黑名单 -> page_key='admin.securityBlacklist' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.totalPrefix', '共 ', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.totalPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.totalSuffix', ' 条', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.totalSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.addBtnText', '+ 添加', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.addBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.loadingTip', '加载中...', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.removeBtnText', '移除', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.removeBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.reasonPrefix', '原因：', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.reasonPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.banCountPrefix', '封禁 ', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.banCountPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.banCountSuffix', ' 次', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.banCountSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.autoTagText', '自动', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.autoTagText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.noMoreText', '已加载全部', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.noMoreText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.emptyText', '黑名单为空', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.modalTitle', '添加 IP 黑名单', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.modalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.ipPlaceholder', 'IP 地址', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.ipPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.reasonPlaceholder', '封禁原因（可选）', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.reasonPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.cancelBtnText', '取消', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.cancelBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.confirmBtnText', '确认', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.confirmBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- A.26 安全诊断 -> page_key='admin.securityDiagnose' (12 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.scanDesc', '扫描近 7 天所有登录/操作 IP 的风险情况', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.scanDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.scanningText', '扫描中...', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.scanningText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.startScanText', '开始扫描', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.startScanText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.resultSummaryPrefix', '共 ', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.resultSummaryPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.resultSummaryMid', ' 个 IP，风险 ', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.resultSummaryMid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.resultSummarySuffix', ' 个', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.resultSummarySuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.riskyText', '有风险', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.riskyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.safeText', '安全', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.safeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.riskTagsPrefix', '风险标签：', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.riskTagsPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.typePrefix', '类型：', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.typePrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.userLabel', '关联用户：', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.userLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.emptyText', '未发现任何 IP', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-safe'));

-- ============================================================
-- Task 8.3 事项B：新建"官方文案"套（default-offcial），覆盖全部 37 个页面
-- 来源：contract-miniprogram 仓库 37 个页面的 lang/offcial/*.js 文件
-- is_active=0，不影响现有运行时唯一生效套（default-safe）
-- ============================================================

INSERT INTO `custom_text_profile`
    (`name`, `code`, `seed_from`, `is_active`, `sort`, `remark`, `creator`, `create_time`)
SELECT '官方文案', 'default-offcial', 'offcial', 0, 2, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_profile` WHERE `code` = 'default-offcial');

-- B.1 首页 -> page_key='index' (22 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.pageTitle', '合约服务', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.wantToSign', '我要签约', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.wantToSign' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.programName', '用飞速合约', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.programName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.signGreeting', '签合约更安心', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.signGreeting' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.wantToSupplement', '我要补合约', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.wantToSupplement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.supplementSubtitle1', '朋友找我帮忙', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.supplementSubtitle1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.supplementSubtitle2', '我们解决痛点', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.supplementSubtitle2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.contractManagement', '合约管理', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.contractManagement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.manageSubtitle', '管理更方便', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.manageSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.healthQuery', '信用查询', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.healthQuery' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.healthQuerySubtitle', '一键查询信用报告', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.healthQuerySubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.moreTitle', '更多', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.moreTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.bannerTitle', '提需求 提建议 求反馈', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.bannerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.joinButton', '我要参加', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.joinButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.disclaimerTitle', '免责声明', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.disclaimerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.disclaimerContent', '本平台是有偿提供合约合同的居间平台，因个人资质及填写信息等相关行为产生的法律责任、纠纷等概与本平台无关，由关系人自行承担，请知悉。', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.disclaimerContent' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.officialModalTitle', '提示', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.officialGuideTitle', '请关注我们的公众号', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialGuideTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.officialQrcodeTip', '长按识别二维码关注', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialQrcodeTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.officialAccountLabel', '微信号：', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialAccountLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.officialCopyText', '点击复制', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialCopyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'index', '', 'index.officialModalConfirm', '我知道了', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'index.officialModalConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.2 合同主包 -> page_key='contract' (52 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.defaultNickname', '飞速', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.defaultNickname' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.welcomeText', '欢迎回来', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.welcomeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.borrowText', '借入', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.borrowText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.lendText', '借出', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.lendText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabAll', '全部', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabAll' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabPendingConfirm', '待确认', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabPendingPayment', '待收款', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabPendingRepayment', '待还款', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabRepaid', '已还款', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabOverdue', '已逾期', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabExpired', '已失效', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabRejected', '拒签', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.tabWithdrawn', '撤销', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.tabWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.searchPlaceholder', '搜索债务人姓名...', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.searchPlaceholderDebtor', '搜索债务人姓名...', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchPlaceholderDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.searchPlaceholderCreditor', '搜索债权人姓名...', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchPlaceholderCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.searchClear', '×', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.searchClear' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.sortDefault', '默认排序', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortDefault' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.sortRepaymentDate', '还款日期', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.sortAmount', '金额', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.sortStartDate', '起始日期', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sortStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statsTotalContracts', '合同总数', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsTotalContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statsTotalAmount', '合同金额', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsTotalAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statsUnitContracts', '张', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsUnitContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statsUnitAmount', '元', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statsUnitAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusPendingConfirm', '待确认', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusPendingPayment', '待收款', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusPendingRepayment', '待还款', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusTodayPayment', '今日还款', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusTodayPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusRepaid', '已还款', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusOverdue', '已逾期', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusExpired', '已失效', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusRejected', '拒签', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.statusWithdrawn', '撤销', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.statusWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.contractTypeBorrow', 'TA欠我', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractTypeBorrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.contractTypeLend', '我欠TA', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractTypeLend' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.labelDebtor', '债务人', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.labelCreditor', '债权人', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.labelInterestRate', '利率', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelInterestRate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.labelStartDate', '起始日', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.labelRepaymentDate', '还款日', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.labelExtendedRepayment', '📅 展期还款日', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelExtendedRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.btnShare', '分享', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnShare' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.btnDownload', '下载', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnDownload' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.btnSupplement', '补充', 450, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnSupplement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.btnEdit', '编辑', 460, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.btnEdit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.emptyText', '暂无合约记录', 470, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.emptyDesc', '快去创建您的第一份合约吧', 480, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.emptyDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.emptyAction', '创建合约', 490, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.emptyAction' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.loadingText', '加载中...', 500, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.fabNewContract', '新建合约', 510, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.fabNewContract' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract', '', 'contract.labelDaysLeft', '剩余天数', 520, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.labelDaysLeft' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.3 征信主包 -> page_key='credit' (22 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.pageTitle', '查询个人信用', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.warningText', '请确保输入的姓名与对方的真实姓名一致，否则无法生效。', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.warningText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.realNameLabel', '真实姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.realNamePlaceholder', '请填写真实姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.idNumberLabel', '身份证号', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.idNumberLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.idNumberPlaceholder', '请填写身份证号', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.idNumberPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.idErrorText', '请正确填写身份证号', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.idErrorText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.securityTip', '为保证交易安全，建议您填写对方身份证号。', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.securityTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.submitBtn', '提交', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.recentListTitle', '最近往来', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.recentListTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.headerName', '姓名', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.headerName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.headerId', '身份证', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.headerId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.prevArrow', '〈', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.prevArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.nextArrow', '〉', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.nextArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.calendarPageTitle', '我的日历', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.calendarPageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.todayEventsTitle', '今日事件', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.todayEventsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.noEventsText', '今天没有安排事件', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.noEventsText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.noEventsDesc', '点击+号添加新事件', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.noEventsDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.addEventText', '添加事件', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.addEventText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit', '', 'credit.unlockIdManualInput', '改用手动输入', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.unlockIdManualInput' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.4 我的 -> page_key='profile' (41 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.notLoggedInNickname', '未登录', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.notLoggedInNickname' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.loginBtn', '点击登录', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.loginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuEditProfile', '编辑资料', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuEditProfile' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuSettings', '设置', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSettings' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuChangePassword', '密码修改', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuChangePassword' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuChangePayPassword', '修改支付密码', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuChangePayPassword' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuFaceVerify', '肖像认证', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuFaceVerify' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuInviteCode', '我的邀请码', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuInviteCode' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuDashboard', '数据大盘', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuDashboard' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuDashboardChart', '数据图表', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuDashboardChart' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuSecurityAlert', '安全告警', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSecurityAlert' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuSecurityBlacklist', 'IP 黑名单', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSecurityBlacklist' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuSecurityDiagnose', 'IP 诊断', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuSecurityDiagnose' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuTestFaceAuth', '测试人脸识别成功', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestFaceAuth' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuTestCompleteInfo', '测试完善个人信息', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestCompleteInfo' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuTestIouConfirm', '测试确认合同', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestIouConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuTestOfficialAccountModal', '测试公众号未关注弹窗', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuTestOfficialAccountModal' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuContractManagement', '合同管理', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuContractManagement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuHelp', '帮助与反馈', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuAbout', '关于我们', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuAbout' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.menuContactService', '联系客服', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.menuContactService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.logoutBtn', '退出登录', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.logoutBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.closeIcon', '×', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.closeIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.changePasswordTitle', '修改密码', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.changePasswordTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.oldPasswordLabel', '原密码：', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.oldPasswordPlaceholder', '请输入原密码', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.newPasswordLabel', '新密码：', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.newPasswordPlaceholder', '请输入新密码', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.confirmPasswordLabel', '确认新密码：', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.confirmPasswordPlaceholder', '请再次输入新密码', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.passwordCancelBtn', '取消', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.passwordCancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.passwordConfirmBtn', '确定', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.passwordConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.changePayPasswordTitle', '修改支付密码', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.changePayPasswordTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.oldPayPasswordLabel', '原支付密码：', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.oldPayPasswordPlaceholder', '请输入原支付密码', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.oldPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.newPayPasswordLabel', '新支付密码：', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.newPayPasswordPlaceholder', '请输入新支付密码（6位数字）', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.newPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.confirmPayPasswordLabel', '确认新支付密码：', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.confirmPayPasswordPlaceholder', '请再次输入新支付密码', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.confirmPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.payPasswordCancelBtn', '取消', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.payPasswordCancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'profile', '', 'profile.payPasswordConfirmBtn', '确定', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'profile.payPasswordConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.5 底部导航 -> page_key='tab' (4 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'tab', '', 'tab.home', '首页', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.home' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'tab', '', 'tab.contract', '合同', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.contract' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'tab', '', 'tab.credit', '信用查询', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.credit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'tab', '', 'tab.profile', '我的', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'tab.profile' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.6 登录 -> page_key='auth.login' (44 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.backIcon', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.backIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.logoAppName', '合约服务', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.logoAppName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.logoSlogan', '安全便捷的合约管理平台', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.logoSlogan' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.formTitle', '欢迎回来', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.formTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.formSubtitle', '请登录您的账户', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.formSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.loginTypeTabLabel', '账号密码登录', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginTypeTabLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.usernameLabel', '登录账号', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.usernameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.usernamePlaceholder', '请输入登录账号（姓名或身份证号）', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.usernamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.accountHistorySavedTag', '已保存密码', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.accountHistorySavedTag' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.loginIdNoLabel', '身份证号', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginIdNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.loginIdNoPlaceholder', '请输入18位身份证号', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginIdNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.passwordLabel', '密码', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.passwordPlaceholder', '请输入密码', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.passwordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotPasswordLink', '忘记密码？', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotPasswordLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.rememberPasswordLabel', '记住密码（本地加密存储）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.rememberPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.loginBtn', '登录', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.loginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.noAccountText', '还没有账户？', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.noAccountText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.registerLink', '立即注册', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.registerLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.popupTitle', '完善个人信息', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.popupCloseBtn', '×', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.avatarEditTip', '点击更换头像', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.avatarEditTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.nicknameLabel', '昵称', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.nicknameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.nicknamePlaceholder', '请输入昵称', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.nicknamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.popupRealNameLabel', '真实姓名', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupRealNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.popupRealNamePlaceholder', '请输入真实姓名', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupRealNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.popupIdNoLabel', '身份证号', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupIdNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.popupIdNoPlaceholder', '请输入18位身份证号码', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.popupIdNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.genderLabel', '性别', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.genderLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.birthdayLabel', '生日', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.birthdayLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.submitCompleteBtn', '完成', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.submitCompleteBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotModalTitle', '忘记密码', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotModalCloseBtn', '×', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotPasswordTip', '请填写注册时的真实姓名与身份证号，重置后登录密码与支付密码将一并更新为新密码。', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotPasswordTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotRealNameLabel', '真实姓名', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotRealNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotRealNamePlaceholder', '请输入真实姓名', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotRealNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotIdNoLabel', '身份证号', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotIdNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotIdNoPlaceholder', '请输入身份证号（15-20位）', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotIdNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotNewPasswordLabel', '新密码', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotNewPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotNewPasswordPlaceholder', '请输入新密码（4-16位）', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotNewPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotConfirmPasswordLabel', '确认密码', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotConfirmPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotConfirmPasswordPlaceholder', '请再次输入新密码', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotConfirmPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotCancelBtn', '取消', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotCancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.forgotConfirmBtn', '确定', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.forgotConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.login', '', 'auth.login.requiredMark', '*', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.login.requiredMark' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.7 注册 -> page_key='auth.register' (31 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.logoAppName', '合约服务', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.logoAppName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.logoSlogan', '创建您的账户', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.logoSlogan' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.formTitle', '欢迎注册', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.formTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.formSubtitle', '请填写以下信息完成注册', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.formSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.realnameLabel', '姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.realnameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.realnamePlaceholder', '请输入姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.realnamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.idCardLabel', '身份证号', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.idCardPlaceholder', '请输入身份证号', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.inviteCodeLabel', '邀请码', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.inviteCodeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.inviteCodePlaceholder', '请输入邀请码（如有）', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.inviteCodePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.passwordLabel', '登录密码', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.confirmPasswordLabel', '确认登录密码', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.confirmPasswordPlaceholder', '请再次输入登录密码', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.payPasswordLabel', '支付密码', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.payPasswordPlaceholder', '请设置6位数字支付密码（不能连续）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.confirmPayPasswordLabel', '确认支付密码', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPayPasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.confirmPayPasswordPlaceholder', '请再次输入支付密码', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.confirmPayPasswordPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.registerBtn', '注册', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.registerBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.haveAccountText', '已有账户？', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.haveAccountText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.loginLink', '立即登录', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.requiredMark', '*', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.requiredMark' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.realnameTipMinLength', '请输入至少2个字符的姓名', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.realnameTipMinLength' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.idCardTipLength', '请输入18位身份证号', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardTipLength' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.idCardTipInvalid', '身份证号格式不正确，请检查', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.idCardTipInvalid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.loginPwdTipMinLength', '密码至少6位', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginPwdTipMinLength' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.loginPwdTipNumericOnly', '登录密码需为6位数字', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginPwdTipNumericOnly' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.loginPwdTipForbidPureNumber', '登录密码不能全为数字，请包含字母', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.loginPwdTipForbidPureNumber' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.payPwdTipInvalid', '请设置6位数字支付密码', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPwdTipInvalid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.payPwdTipWeak', '支付密码不能连续或过于简单（如123456、223344）', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.payPwdTipWeak' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.passwordPlaceholderNumeric', '请设置6位数字密码', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.passwordPlaceholderNumeric' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'auth.register', '', 'auth.register.passwordPlaceholderComplex', '请设置密码（字母+数字，至少6位）', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'auth.register.passwordPlaceholderComplex' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.8 合同详情 -> page_key='contract.contractDetail' (56 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.pageTitle', '合同详情', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.contractIdLabel', '编号: ', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.contractIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.borrowerRoleLabel', '借款人', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.borrowerRoleLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.lenderRoleLabel', '出借人', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.lenderRoleLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.idNoLabelPrefix', '身份证 ', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.idNoLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusTodayPayment', '今日还款', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusTodayPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusPendingConfirm', '待确认', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusPendingPayment', '待收款', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusPendingRepayment', '待还款', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusRepaid', '已还款', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusOverdue', '已逾期', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusExpired', '已失效', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusRejected', '拒签', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.statusWithdrawn', '撤销', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.statusWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelDebtAmount', '欠款金额', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelDebtAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelAnnualRate', '年化利率', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelAnnualRate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelTotalInterest', '应收利息', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelTotalInterest' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelTotalAmount', '本息合计', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelTotalAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelRefundAmount', '已还金额', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelRefundAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelRemainingAmount', '待还金额', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelRemainingAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelRepaymentMethod', '还款方式', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelRepaymentMethod' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.repaymentMethodValue', '一次性还款', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.repaymentMethodValue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelStartDate', '起始日', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.labelEndDate', '到期日', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.labelEndDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.loanAgreementLabel', '借款协议', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.loanAgreementLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.viewLoanAgreementBtn', '查看借款协议', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.viewLoanAgreementBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.terminateContractLabel', '解除合同', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.terminateContractLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.terminateContractBtn', '解除合同', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.terminateContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.disputeText', '遇到争议怎么办?', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.extendBtn', '一键展期', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.extendBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.settleBtn', '一键销账', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.settleBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareContractBtn', '分享合同', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.withdrawContractBtn', '撤回合约', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.withdrawContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.confirmContractBtn', '确认合同', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.confirmContractBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.rejectSignBtn', '拒绝签署', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.rejectSignBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.agreementModalCloseBtn', '×', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.agreementModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.agreementModalCloseText', '关闭', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.agreementModalCloseText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.agreementModalExportBtn', '导出PDF', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.agreementModalExportBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalTitle', '合同详情', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalSubtitle', '风速合约小程序', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalCloseIcon', '×', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalCloseIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareTipLine1', '点击右上角分享给微信好友/朋友圈', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareTipLine1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareTipLine2', '或长按保存图片 使用二维码邀请', 450, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareTipLine2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareInviteSuffix', '在风速合约', 460, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareInviteSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareInviteLine2', '向您发起风速借条', 470, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareInviteLine2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareRateLabelPrefix', '年化利率: ', 480, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareRateLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareRepaymentDateLabelPrefix', '还款日期 ', 490, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareRepaymentDateLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalCloseBtn', '关闭弹窗', 500, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.shareModalShareBtn', '分享给好友', 510, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.shareModalShareBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.testButton1', '测试本人中转', 520, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.testButton1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.testButton2', '测试对方中转', 530, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.testButton2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.disputeModalTitle', '争议解决指引', 540, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.disputeModalCloseIcon', '×', 550, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeModalCloseIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractDetail', '', 'contract.contractDetail.disputeModalConfirmBtn', '我知道了', 560, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractDetail.disputeModalConfirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.9 合同管理 -> page_key='contract.contractManagement' (52 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.defaultNickname', '飞速', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.defaultNickname' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.welcomeText', '欢迎回来', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.welcomeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.borrowText', '借入', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.borrowText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.lendText', '借出', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.lendText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabAll', '全部', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabAll' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabPendingConfirm', '待确认', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabPendingPayment', '待收款', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabPendingRepayment', '待还款', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabRepaid', '已还款', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabOverdue', '已逾期', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabExpired', '已失效', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabRejected', '拒签', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.tabWithdrawn', '撤销', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.tabWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.searchPlaceholder', '搜索债务人姓名...', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.searchPlaceholderDebtor', '搜索债务人姓名...', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchPlaceholderDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.searchPlaceholderCreditor', '搜索债权人姓名...', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchPlaceholderCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.searchClear', '×', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.searchClear' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.sortDefault', '默认排序', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortDefault' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.sortRepaymentDate', '还款日期', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.sortAmount', '金额', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.sortStartDate', '起始日期', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.sortStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statsTotalContracts', '合同总数', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsTotalContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statsTotalAmount', '合同金额', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsTotalAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statsUnitContracts', '张', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsUnitContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statsUnitAmount', '元', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statsUnitAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusPendingConfirm', '待确认', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusPendingPayment', '待收款', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusPendingPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusPendingRepayment', '待还款', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusTodayPayment', '今日还款', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusTodayPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusRepaid', '已还款', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusOverdue', '已逾期', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusExpired', '已失效', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusRejected', '拒签', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusRejected' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.statusWithdrawn', '撤销', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.statusWithdrawn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.contractTypeBorrow', 'TA欠我', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.contractTypeBorrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.contractTypeLend', '我欠TA', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.contractTypeLend' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.labelDebtor', '债务人', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelDebtor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.labelCreditor', '债权人', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelCreditor' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.labelInterestRate', '利率', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelInterestRate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.labelStartDate', '起始日', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelStartDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.labelRepaymentDate', '还款日', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelRepaymentDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.labelExtendedRepayment', '📅 展期还款日', 420, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelExtendedRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.labelDaysLeft', '剩余天数', 430, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.labelDaysLeft' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.btnShare', '分享', 440, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnShare' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.btnDownload', '下载', 450, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnDownload' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.btnSupplement', '补充', 460, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnSupplement' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.btnEdit', '编辑', 470, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.btnEdit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.emptyText', '暂无合约记录', 480, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.emptyDesc', '快去创建您的第一份合约吧', 490, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.emptyDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.emptyAction', '创建合约', 500, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.emptyAction' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.loadingText', '加载中...', 510, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractManagement', '', 'contract.contractManagement.fabNewContract', '新建合约', 520, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractManagement.fabNewContract' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.10 合同分享入口 -> page_key='contract.contractShareEntry' (11 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.title', '正在验证您的合同身份', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.subtitle', '请稍候...', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.subtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.loadingText', '正在加载合同信息，请稍候', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.readyText', '即将为您跳转到对应页面', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.readyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.backHomeBtn', '返回首页', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.backHomeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorMissingId', '缺少合同编号，无法识别二维码', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorMissingId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorNotLoggedIn', '请先登录后再扫码查看合同', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorNotLoggedIn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorNotFound', '未找到对应合同或合同已失效', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorNotFound' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorIdentityMismatch', '您无权限查看该合同，合同中的身份信息与您的账号信息不匹配，请联系合同双方确认', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorIdentityMismatch' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorLoadFailed', '合同信息加载失败，请稍后重试', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorLoadFailed' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.contractShareEntry', '', 'contract.contractShareEntry.errorNoPermission', '您无权限查看该合同，请联系合同双方确认', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.contractShareEntry.errorNoPermission' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.11 分享合同 -> page_key='contract.sharecontract' (15 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.dialogTitle', '发送到微信好友或朋友圈', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.dialogTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.contractTitle', '在风速合约向您发起风速借条', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.contractTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.contentLabel', '约定内容', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.contentLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.completionDateLabel', '完成日期', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.completionDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.amountUnit', '元', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.amountUnit' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.repaymentDateLabel', '还款日期', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.repaymentDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeTitle', '扫描二维码支付费用', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeTip', '长按图片可以保存到相册', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeLoading', '生成二维码中...', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeLoading' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.qrcodeError', '生成二维码失败，请', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.qrcodeError' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.retryButton', '重试', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.retryButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.closeButton', '关闭弹窗', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.closeButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.shareButton', '分享给好友', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.shareButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.confirmButton', '确认', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.confirmButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.sharecontract', '', 'contract.sharecontract.payButton', 'Pay', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.sharecontract.payButton' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.12 签约 -> page_key='contract.signContract' (41 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.pageTitle', '我要签约', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.indebtedNameLabel', '合约欠款人', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.indebtedIdLabel', '欠款人身份证', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.creditorNameLabel', '合约债权人姓名', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.creditorIdLabel', '债权人身份证', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.salaryLabel', '交易金额', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.salaryLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.annualRateLabel', '年化利率', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.annualRateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.contractTypeLabel', '履约计划', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.contractTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.startDateLabel', '起始日期', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.startDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.endDateLabel', '截止日期', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.endDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.reasonLabel', '原因', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.reasonLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.indebtedNamePlaceholder', '请输入欠款人姓名', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.indebtedIdPlaceholder', '请输入欠款人身份证', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.indebtedIdPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.creditorNamePlaceholder', '请输入债权人姓名', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.creditorIdPlaceholder', '请输入债权人身份证', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.creditorIdPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.salaryPlaceholder', '请输入金额', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.salaryPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.annualRatePlaceholder', '请输入年化利率', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.annualRatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.contractTypePlaceholder', '请选择合同类型', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.contractTypePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.startDatePlaceholder', '请选择起始日期', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.startDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.endDatePlaceholder', '请选择还款日期', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.endDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.remarksPlaceholder', '请输入详细原因（选填）', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.remarksPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.unitSymbol', '%', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.unitSymbol' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.quickLabel', '快捷选择：', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.quickBtn6Days', '6天前', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn6Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.quickBtn7Days', '7天前', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn7Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.quickBtn10Days', '10天前', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn10Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.quickBtn14Days', '14天前', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.quickBtn14Days' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.chooseFileText', '选择文件', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.chooseFileText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.fileSizeText', '文件大小:', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.fileSizeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.uploadProgressText', '上传进度:', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.uploadProgressText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.uploadSuccessText', '上传成功！', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.uploadSuccessText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.uploadFailText', '上传失败！', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.uploadFailText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.serverResponseText', '服务器返回:', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.serverResponseText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.errorInfoText', '错误信息:', 340, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.errorInfoText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.radioOption1', '线下结算', 350, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.radioOption2', '提前预支', 360, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.radioOption3', '劳务', 370, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.radioOption4', '买卖', 380, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption4' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.radioOption5', '其他', 390, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.radioOption5' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.submitBtnText', '提交签约申请', 400, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.signContract', '', 'contract.signContract.nextStepBtnText', '下一步', 410, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.signContract.nextStepBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.13 补充合同 -> page_key='contract.supplementContract' (22 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.pageTitle', '我要补合约', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.originalContractLabel', '原合约编号', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.originalContractLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.supplementTypeLabel', '补充类型', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.supplementTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newSalaryLabel', '调整后薪资', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newSalaryLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newPositionLabel', '调整后职位', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newPositionLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newWorkLocationLabel', '调整后工作地点', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newWorkLocationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newDurationLabel', '调整后期限', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newDurationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.effectiveDateLabel', '截止日期', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.effectiveDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.reasonLabel', '申请原因', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.reasonLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.attachmentLabel', '相关附件', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.attachmentLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.originalContractPlaceholder', '请选择原合约', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.originalContractPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.supplementTypePlaceholder', '请选择补充类型', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.supplementTypePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newSalaryPlaceholder', '请输入调整后的月薪金额', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newSalaryPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newPositionPlaceholder', '请输入调整后的职位名称', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newPositionPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newWorkLocationPlaceholder', '请输入调整后的工作地点', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newWorkLocationPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.newDurationPlaceholder', '请选择调整后的合同期限', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.newDurationPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.effectiveDatePlaceholder', '请选择补充协议生效日期', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.effectiveDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.reasonPlaceholder', '请详细说明申请补充合约的原因', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.reasonPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.uploadPlaceholder', '点击上传相关证明文件（选填）', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.uploadPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.removeFileText', '删除', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.removeFileText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.submitBtnText', '提交补充申请', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.supplementContract', '', 'contract.supplementContract.fileNameText', '文件名', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.supplementContract.fileNameText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.14 选择债务人 -> page_key='contract.selectDebtor' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.tip', '请确保输入的姓名与对方真实姓名一致，否则合约无法生效。', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.tip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.realNameLabel', '真实姓名', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.realNamePlaceholder', '请输入真实姓名', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.idNumberLabel', '身份证号', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.idNumberLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.idNumberPlaceholder', '请输入身份证号', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.idNumberPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.idErrorTip', '请输入合法的身份证号', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.idErrorTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.desc', '为保证交易安全，建议您填写对方身份证号。', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.desc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.submitBtn', '提交', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.historyTitle', '最近往来', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.historyTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.tableHeaderName', '姓名', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.tableHeaderName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.tableHeaderId', '身份证号', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.tableHeaderId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeModeTitle', '选择联系人', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeModeTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeModeDesc', '请选择您要创建约定的联系人', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeModeDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeInfoText', '此功能暂未开放', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeInfoText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeInfoSubtext', '请联系管理员获取更多信息', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeInfoSubtext' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.selectDebtor', '', 'contract.selectDebtor.safeBackBtn', '返回', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.selectDebtor.safeBackBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.15 欠条确认 -> page_key='contract.iouConfirm' (33 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.pageTitle', '合约确认', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart1', '欠', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart2', '元，欠款期限为', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart3', '至', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart4', '，并承诺按年化利率', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart4' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.iouContentPart5', '计算支付利息，于到期日偿还本息。', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.iouContentPart5' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.creditorLabel', '债权人:', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.creditorLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.creditorIdLabel', '身份证号:', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.creditorIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.debtorLabel', '债务人:', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.debtorLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.debtorIdLabel', '身份证号:', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.debtorIdLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.agreementText', '已阅读并同意', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.agreementText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.agreementLink', '相关协议', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.agreementLink' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmBtn', '确认合约', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.creditTip', '违约上传百行征信', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.creditTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.modalTitle', '合约确认', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.modalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.modalCloseBtn', '×', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.modalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.modalMoreBtn', '...', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.modalMoreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.passwordTip', '密码为 6 位数字', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.passwordTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.doneText', '完成', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.doneText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.deleteText', 'X', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.deleteText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.rejectBtn', '拒绝签署', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.rejectBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.passwordSubtitle', '验证身份以继续操作', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.passwordSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.passwordLabel', '支付密码', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.protocolModalTitle', '相关协议', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.protocolModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.protocolModalCloseBtn', '×', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.protocolModalCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.loanAgreementOptionText', '借款协议', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.loanAgreementOptionText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmDocumentOptionText', '合同确认书', 290, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmDocumentOptionText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.loanAgreementCloseBtn', '关闭', 300, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.loanAgreementCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.loanAgreementExportBtn', '导出PDF', 310, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.loanAgreementExportBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmDocumentCloseBtn', '关闭', 320, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmDocumentCloseBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.iouConfirm', '', 'contract.iouConfirm.confirmDocumentExportBtn', '导出PDF', 330, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.iouConfirm.confirmDocumentExportBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.16 债务结算 -> page_key='contract.debtSettlement' (28 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.headerTitle', '合约销账', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountCardTitle', '销账金额', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.fullPaymentBtn', '全额销账', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.fullPaymentBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.summaryLabelTotal', '待还总额', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.summaryLabelTotal' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.inputLabelAmount', '本次销账金额', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.inputLabelAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.inputTipPrefix', '最多可销账 ', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.inputTipPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmount100', '100元', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmount100' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmount500', '500元', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmount500' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmount1000', '1000元', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmount1000' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.quickAmountFull', '全额', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.quickAmountFull' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountErrorInvalid', '请输入有效的金额', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountErrorInvalid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountErrorExceeds', '销账金额不能超过剩余待还金额', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountErrorExceeds' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.amountErrorTooSmall', '销账金额不能小于0.01元', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.amountErrorTooSmall' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.remarkCardTitle', '备注信息', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.remarkCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.remarkOptional', '（选填）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.remarkOptional' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.remarkPlaceholder', '请输入销账备注信息，如：部分还款、延期说明等...', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.remarkPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.previewLabel', '本次销账', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.previewLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.submitBtnLoadingText', '提交中...', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.submitBtnLoadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.submitBtnText', '确认销账', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.menuViewHistory', '查看历史记录', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.menuViewHistory' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.menuExportData', '导出数据', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.menuExportData' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.menuHelp', '使用帮助', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.menuHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordModalTitle', '请输入交易密码', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordModalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordModalSubtitle', '验证身份以继续操作', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordModalSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordLabel', '支付密码', 250, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.passwordHint', '请输入6位数字密码', 260, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.passwordHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.doneText', '完成', 270, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.doneText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.debtSettlement', '', 'contract.debtSettlement.deleteText', 'X', 280, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.debtSettlement.deleteText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.17 展期 -> page_key='contract.extension' (23 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.headerTitle', '展期申请', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.infoCardTitle', '展期信息', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.infoCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.labelAvailableAmount', '可展期金额', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.labelAvailableAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.labelCurrentDueDate', '当前到期时间', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.labelCurrentDueDate' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.extensionSettingsTitle', '展期设置', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.extensionSettingsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.limitText', '最长不超过10年', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.limitText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.newDueDateLabel', '新的还款日期', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.newDueDateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.dueDatePlaceholder', '请选择还款日期', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.dueDatePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.extensionDaysLabel', '展期天数', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.extensionDaysLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.extensionTipText', '展期后将产生相应的展期费用', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.extensionTipText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.feeSectionTitle', '费用估算', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeSectionTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.feeLabelService', '展期服务费', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeLabelService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.feeLabelInterest', '展期利息', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeLabelInterest' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.feeLabelTotal', '总计费用', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.feeLabelTotal' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.agreementText', '我已阅读并同意', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.agreementText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.agreementLink1', '《展期服务协议》', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.agreementLink1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.agreementLink2', '《费用说明》', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.agreementLink2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.previewLabel', '预计费用', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.previewLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.submitBtnLoadingText', '提交中...', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.submitBtnLoadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.submitBtnText', '确认展期', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.submitBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.menuHistory', '展期记录', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.menuHistory' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.menuContactService', '联系客服', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.menuContactService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.extension', '', 'contract.extension.menuHelp', '使用帮助', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.extension.menuHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.18 步骤引导 -> page_key='contract.step' (20 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.headerTitle', '选择您的身份', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.headerSubtitle', '请根据您在合约中的角色选择', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.headerSubtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderCardTitle', '资方', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderCardBadge', '资金提供方', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderCardBadge' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderDesc1', '作为资金出借方', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderDesc1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderDesc2', '创建借款合约', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderDesc2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderFeature1', '• 设置出资金额', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderFeature1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderFeature2', '• 约定还款期限', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderFeature2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderFeature3', '• 管理收款计划', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderFeature3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.lenderEnterText', '进入出借方页面', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.lenderEnterText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerCardTitle', '借方', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerCardBadge', '资金需求方', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerCardBadge' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerDesc1', '作为资金借入方', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerDesc1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerDesc2', '申请合约', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerDesc2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerFeature1', '• 提交合同申请', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerFeature1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerFeature2', '• 查看结算记录', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerFeature2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerFeature3', '• 管理合同状态', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerFeature3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.borrowerEnterText', '进入借方页面', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.borrowerEnterText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.tipTitle', '温馨提示', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.tipTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'contract.step', '', 'contract.step.tipDesc', '请根据您在合约中的实际身份选择相应入口，不同身份的操作权限和功能会有所区别。', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'contract.step.tipDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.19 征信详情 -> page_key='credit.creditDetail' (17 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.idNoLabelPrefix', '身份证号：', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.idNoLabelPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.tabStat', '借入统计', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tabStat' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.tabDetail', '借入明细', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tabDetail' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.tableHeaderStatus', '状态', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tableHeaderStatus' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.tableHeaderCount', '笔数', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tableHeaderCount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.tableHeaderAmount', '金额(元)', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.tableHeaderAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.summaryLabel', '总计', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.summaryLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.emptyText', '没有更多了', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.contractIdPrefix', '合同号：', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.contractIdPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.amountPrefix', '合同金额：￥', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.amountPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.dueDatePrefix', '还款时间：', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.dueDatePrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.statusPendingConfirm', '待确认', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusPendingConfirm' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.statusPendingRepayment', '待还款', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusPendingRepayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.statusRepaid', '已还款', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusRepaid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.statusOverdue', '已逾期', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusOverdue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.statusExpired', '已失效', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusExpired' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditDetail', '', 'credit.creditDetail.statusUnknown', '未知状态', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditDetail.statusUnknown' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.20 征信查询 -> page_key='credit.creditQuery' (22 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.closeBtn', '×', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.closeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.pageTitle', '查询个人信用', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.moreBtn', '...', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.moreBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.warningText', '请确保输入的姓名与对方的真实姓名一致，否则无法生效。', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.warningText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.realNameLabel', '真实姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.realNamePlaceholder', '请填写真实姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.idNumberLabel', '身份证号', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.idNumberLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.idNumberPlaceholder', '请填写身份证号', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.idNumberPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.idErrorText', '请正确填写身份证号', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.idErrorText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.securityTip', '为保证交易安全，建议您填写对方身份证号。', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.securityTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.submitBtn', '提交', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.recentListTitle', '最近往来', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.recentListTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.headerName', '姓名', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.headerName' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.headerId', '身份证', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.headerId' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.prevArrow', '〈', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.prevArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.nextArrow', '〉', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.nextArrow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.calendarPageTitle', '我的日历', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.calendarPageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.todayEventsTitle', '今日事件', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.todayEventsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.noEventsText', '今天没有安排事件', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.noEventsText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.noEventsDesc', '点击+号添加新事件', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.noEventsDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.addEventText', '添加事件', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.addEventText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'credit.creditQuery', '', 'credit.creditQuery.unlockIdManualInput', '改用手动输入', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'credit.creditQuery.unlockIdManualInput' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.21 个人信息 -> page_key='user.userinfo' (24 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.pageTitle', '编辑个人信息', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.changeAvatarText', '更换头像', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.changeAvatarText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.realNameLabel', '真实姓名', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.requiredMark', '*', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.requiredMark' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.verifiedReadonlyTip', '（已认证，不可修改）', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.verifiedReadonlyTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.realNamePlaceholder', '请输入真实姓名', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.nicknameLabel', '昵称', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.nicknameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.nicknamePlaceholder', '请输入昵称', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.nicknamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.genderLabel', '性别', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.genderLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.birthdayLabel', '生日', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.birthdayLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.mobileLabel', '手机号（选填）', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.mobileLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.mobilePlaceholder', '请输入手机号（选填）', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.mobilePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.emailLabel', '邮箱', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.emailLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.emailPlaceholder', '请输入邮箱地址', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.emailPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.idNoLabel', '身份证号', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.idNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.idNoPlaceholder', '请输入身份证号码', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.idNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.addressLabel', '居住地址', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.addressLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.addressPlaceholder', '请输入详细居住地址', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.addressPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.occupationLabel', '职业', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.occupationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.educationLabel', '学历', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.educationLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.bioLabel', '个人简介', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.bioLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.bioPlaceholder', '请输入个人简介（选填）', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.bioPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.saveBtn', '保存修改', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.saveBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userinfo', '', 'user.userinfo.cancelBtn', '取消', 240, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userinfo.cancelBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.22 设置 -> page_key='user.settings' (17 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.userDescText', '点击修改个人信息', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.userDescText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.groupGeneral', '通用设置', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupGeneral' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.themeLabel', '主题设置', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.themeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.clearCacheLabel', '清除缓存', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.clearCacheLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.languageLabel', '语言设置', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.languageLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.languageValue', '简体中文', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.languageValue' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.groupHelp', '使用帮助', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupHelp' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.helpFeedbackLabel', '帮助与反馈', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.helpFeedbackLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.groupPrivacy', '隐私与安全', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupPrivacy' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.privacyPolicyLabel', '隐私政策', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.privacyPolicyLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.userAgreementLabel', '用户协议', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.userAgreementLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.changePasswordLabel', '修改密码', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.changePasswordLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.groupAbout', '支持与关于', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.groupAbout' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.aboutUsLabel', '关于我们', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.aboutUsLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.checkUpdateLabel', '检查更新', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.checkUpdateLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.customerServiceLabel', '联系客服', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.customerServiceLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.settings', '', 'user.settings.logoutBtn', '退出登录', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.settings.logoutBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.23 关于我们 -> page_key='user.abouts' (10 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.versionLabel', '版本号', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.versionLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.updateTimeLabel', '更新时间', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.updateTimeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.introTitle', '应用介绍', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.introTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.checkUpdateText', '检查更新', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.checkUpdateText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.feedbackText', '意见反馈', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.feedbackText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.userAgreementText', '用户协议', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.userAgreementText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.privacyPolicyText', '隐私政策', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.privacyPolicyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.updateHintIcon', '↻', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.updateHintIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.copyrightPrefix', '©', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.copyrightPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.abouts', '', 'user.abouts.copyrightSuffix', '版权所有', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.abouts.copyrightSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.24 意见反馈 -> page_key='user.feedbacks' (23 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.searchPlaceholder', '请输入您遇到的问题', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.searchPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.searchBtn', '搜索', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.searchBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.quickActionsTitle', '常用问题', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionsTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.quickActionAccount', '账号问题', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionAccount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.quickActionPayment', '支付问题', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionPayment' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.quickActionFunction', '功能使用', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionFunction' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.quickActionOther', '其他问题', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.quickActionOther' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.helpSectionTitle', '问题分类', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.helpSectionTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.helpViewDetail', '查看详情', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.helpViewDetail' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.helpContactService', '联系客服', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.helpContactService' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.feedbackFormTitle', '问题反馈', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackFormTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.feedbackTypeLabel', '反馈类型', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.feedbackContentLabel', '问题描述', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackContentLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.feedbackContentPlaceholder', '请详细描述您遇到的问题，帮助我们更好的解决', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackContentPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.feedbackImageLabel', '相关截图（选填）', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.feedbackImageLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.deleteImageIcon', '×', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.deleteImageIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.addImageText', '添加图片', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.addImageText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.contactInfoLabel', '联系方式', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.contactInfoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.contactInfoPlaceholder', '请输入联系方式（选填）', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.contactInfoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.submitBtn', '提交反馈', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.submitBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.contactCardTitle', '需要更多帮助？', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.contactCardTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.wechatAccountPrefix', '微信公众号：', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.wechatAccountPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.feedbacks', '', 'user.feedbacks.serviceEmailPrefix', '客服邮箱：', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.feedbacks.serviceEmailPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.25 人脸认证 -> page_key='user.faceAuth' (23 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.headerTitle', '身份认证', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.headerDesc', '请确认您的身份信息，完成实名认证', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.headerDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.realNameLabel', '真实姓名', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.realNameLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.autoFilledTip', '（已自动填入，不可修改）', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.autoFilledTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.realNamePlaceholder', '请输入您的真实姓名', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.realNamePlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.idNoLabel', '身份证号', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.idNoLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.idNoPlaceholder', '请输入18位身份证号码', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.idNoPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.tipIcon', 'ℹ️', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipIcon' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.tipTitle', '认证说明：', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.tipLine1', '1. 请确保姓名和身份证号准确无误', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipLine1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.tipLine2', '2. 点击确认后将进入人脸识别环节', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipLine2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.tipLine3', '3. 请保持网络畅通，按提示完成认证', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.tipLine3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.testStep1Btn', '1. 开始核验', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.testStep1Btn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.testStep2Btn', '2. 提交身份验证', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.testStep2Btn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.testStep3Btn', '3. 开始人脸识别', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.testStep3Btn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.confirmBtn', '确认认证', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.confirmBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.processingBtn', '处理中...', 170, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.processingBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.footerTip', '🔒 您的信息安全加密，仅用于身份验证', 180, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.footerTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.actionPanelTitle', '其他操作', 190, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.actionPanelTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.actionPanelSub', '资料有误可先修改，或返回「我的」', 200, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.actionPanelSub' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.editProfileBtn', '修改资料', 210, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.editProfileBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.backToProfileBtn', '返回我的', 220, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.backToProfileBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.faceAuth', '', 'user.faceAuth.reloginBtn', '⇄ 切换账号 · 重新登录', 230, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.faceAuth.reloginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.26 网页视图 -> page_key='user.webView' (0 条)

-- B.27 认证结果 -> page_key='user.authResult' (8 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.loadingText', '正在验证身份信息，请稍候...', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.successText', '人脸识别验证已完成', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.successText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.successHint', '请返回原页面查看结果', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.successHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.resultPassedText', '✔ 核验通过', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.resultPassedText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.resultFailedPrefix', '✘ 核验未通过: ', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.resultFailedPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.failText', '流程中断或失败', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.failText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.backHomeBtn', '返回首页', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.backHomeBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.authResult', '', 'user.authResult.retryBtn', '重新验证', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.authResult.retryBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.28 验证码 -> page_key='user.captcha' (10 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.backText', '返回', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.backText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.headerTitle', '安全验证', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.headerTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.refreshBtn', '换一张', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.refreshBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.sliderHint', '拖动滑块完成拼图', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.sliderHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.clickTipPrefix', '请依次点击：', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.clickTipPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.loadingText', '加载验证码中...', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.loadingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.retryBtn', '重新加载', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.retryBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.verifyingText', '验证中...', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.verifyingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.loadFailedText', '加载验证码失败', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.loadFailedText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.captcha', '', 'user.captcha.imageLoadFailedText', '图片加载失败', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.captcha.imageLoadFailedText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.29 隐私政策 -> page_key='user.privacyPolicy' (2 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.privacyPolicy', '', 'user.privacyPolicy.title', '隐私政策说明', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.privacyPolicy.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.privacyPolicy', '', 'user.privacyPolicy.updateTimePrefix', '最后更新日期：', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.privacyPolicy.updateTimePrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.30 用户协议 -> page_key='user.userAgreement' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.title', '身份信息使用授权协议', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.introText', '在您注册并填写个人信息前，请仔细阅读以下条款：', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.introText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.consentIntro', '本人知悉并同意，本次注册所提供的', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentIntro' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.consentHighlight1', '身份证信息', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentHighlight1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.consentParenthetical', '（含真实姓名）', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentParenthetical' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.consentPrefix', '，将被作为本人在【', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.consentSuffix', '】中的', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.consentHighlight2', '唯一主体身份标识', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentHighlight2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.consentPeriod', '。', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.consentPeriod' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.usageLabel', '信息用途说明：', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.usageLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.listItem1', '该身份信息将用于生成、关联及锁定您名下的所有电子合约，确保合约的法律效力与唯一性。', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.listItem1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.listItem2', '您可使用此身份信息快速查询、检索及管理您所签署的全部合约，提升使用体验。', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.listItem2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.listItem3', '程序后续所有与合约相关的功能与服务，均将基于此身份信息进行，请确保其真实、准确、有效。', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.listItem3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.importantNoticeLabel', '重要提示：', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.importantNoticeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.importantNoticeText', '身份信息一经提交将不可随意更改。因填写错误、虚假信息或他人信息所引发的一切法律责任和后果，均由您自行承担。', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.importantNoticeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.userAgreement', '', 'user.userAgreement.finalNotice', '点击"注册"即表示您已阅读、理解并同意以上全部内容。', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.userAgreement.finalNotice' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.31 维护中 -> page_key='user.maintenance' (15 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.pageTitle', '系统维护中', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.pageTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.subtitle', '当前命中维护时间窗口，功能临时不可用', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.subtitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.timeRangeLabel', '维护时间范围', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.timeRangeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.windowIdPrefix', '窗口ID：', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.windowIdPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.targetTypeLabel', '目标类型：', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.targetTypeLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.statusLabelWithSep', ' ｜ 状态：', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.statusLabelWithSep' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.refreshingTip', '正在从接口刷新维护信息...', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.refreshingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.unitDays', '天', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitDays' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.unitHours', '时', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitHours' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.unitMinutes', '分', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitMinutes' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.unitSeconds', '秒', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.unitSeconds' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.tipsNotEnded', '请稍后重试，倒计时结束后会自动解除限制。', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.tipsNotEnded' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.tipsEnded', '维护窗口理论已结束，正在等待状态刷新。', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.tipsEnded' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.refreshBtn', '刷新状态', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.refreshBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.maintenance', '', 'user.maintenance.loginBtn', '返回登录页', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.maintenance.loginBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.32 服务不可用 -> page_key='user.serviceUnavailable' (6 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.title', '服务暂时不可用', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.title' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.desc', '系统正在进行自动恢复，请稍后重试', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.desc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.defaultTip', '服务暂时不可用，请稍后重试', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.defaultTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.checkingText', '正在检测服务状态...', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.checkingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.autoPollingText', '系统将自动轮询恢复状态', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.autoPollingText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'user.serviceUnavailable', '', 'user.serviceUnavailable.retryBtn', '立即重试', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'user.serviceUnavailable.retryBtn' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.33 数据大盘 -> page_key='admin.dashboard' (15 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.loadingTip', '加载中...', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.sectionCoreMetrics', '核心指标', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.sectionCoreMetrics' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelTotalUsers', '总用户', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalUsers' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelTodayNewUsers', '今日新增', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTodayNewUsers' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelVerifiedUsers', '已认证', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelVerifiedUsers' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelActiveUsers7d', '7日活跃', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelActiveUsers7d' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.sectionContractStats', '合同统计', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.sectionContractStats' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelTotalContracts', '总合同', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelTodayNewContracts', '今日新增', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTodayNewContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelOverdueContracts', '逾期', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelOverdueContracts' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelTotalContractAmount', '总金额', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalContractAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.sectionPaymentStats', '支付统计', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.sectionPaymentStats' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelTotalPaySuccess', '累计成功', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTotalPaySuccess' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelTodayPayAmount', '今日金额', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelTodayPayAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboard', '', 'admin.dashboard.labelMonthPayAmount', '本月金额', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboard.labelMonthPayAmount' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.34 数据图表 -> page_key='admin.dashboardChart' (12 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.loadingTip', '加载中...', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.swipeHint', '← 左右滑动切换图表 →', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.swipeHint' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle0', '7日用户趋势', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle0' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle1', '合同状态分布', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle1' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle2', '合同新增趋势', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle2' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle3', '支付金额趋势', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle3' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.chartTitle4', '人脸认证占比', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.chartTitle4' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.legendNewUser', '新增用户', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.legendNewUser' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.legendVerified', '已认证', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.legendVerified' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.legendUnverified', '未认证', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.legendUnverified' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.noDataText', '暂无数据', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.noDataText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.dashboardChart', '', 'admin.dashboardChart.donutTotalLabel', '总用户', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.dashboardChart.donutTotalLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.35 安全告警 -> page_key='admin.securityAlert' (11 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.labelAll', '全部', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.labelAll' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.labelUnhandled', '未处理', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.labelUnhandled' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.labelHandled', '已处理', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.labelHandled' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.loadingTip', '加载中...', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.handleBtnText', '处理', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.handleBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.noMoreText', '已加载全部', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.noMoreText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.emptyText', '暂无告警', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.severityLow', '低', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityLow' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.severityMedium', '中', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityMedium' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.severityHigh', '高', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityHigh' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityAlert', '', 'admin.securityAlert.severityCritical', '严重', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityAlert.severityCritical' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.36 安全黑名单 -> page_key='admin.securityBlacklist' (16 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.totalPrefix', '共 ', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.totalPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.totalSuffix', ' 条', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.totalSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.addBtnText', '+ 添加', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.addBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.loadingTip', '加载中...', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.loadingTip' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.removeBtnText', '移除', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.removeBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.reasonPrefix', '原因：', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.reasonPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.banCountPrefix', '封禁 ', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.banCountPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.banCountSuffix', ' 次', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.banCountSuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.autoTagText', '自动', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.autoTagText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.noMoreText', '已加载全部', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.noMoreText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.emptyText', '黑名单为空', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.modalTitle', '添加 IP 黑名单', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.modalTitle' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.ipPlaceholder', 'IP 地址', 130, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.ipPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.reasonPlaceholder', '封禁原因（可选）', 140, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.reasonPlaceholder' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.cancelBtnText', '取消', 150, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.cancelBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityBlacklist', '', 'admin.securityBlacklist.confirmBtnText', '确认', 160, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityBlacklist.confirmBtnText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));

-- B.37 安全诊断 -> page_key='admin.securityDiagnose' (12 条)
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.scanDesc', '扫描近 7 天所有登录/操作 IP 的风险情况', 10, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.scanDesc' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.scanningText', '扫描中...', 20, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.scanningText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.startScanText', '开始扫描', 30, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.startScanText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.resultSummaryPrefix', '共 ', 40, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.resultSummaryPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.resultSummaryMid', ' 个 IP，风险 ', 50, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.resultSummaryMid' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.resultSummarySuffix', ' 个', 60, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.resultSummarySuffix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.riskyText', '有风险', 70, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.riskyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.safeText', '安全', 80, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.safeText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.riskTagsPrefix', '风险标签：', 90, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.riskTagsPrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.typePrefix', '类型：', 100, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.typePrefix' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.userLabel', '关联用户：', 110, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.userLabel' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
INSERT INTO `custom_text_item`
    (`profile_id`, `page_key`, `module_key`, `item_key`, `item_value`, `sort`, `remark`, `creator`, `create_time`)
SELECT (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'), 'admin.securityDiagnose', '', 'admin.securityDiagnose.emptyText', '未发现任何 IP', 120, '', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_text_item` WHERE `item_key` = 'admin.securityDiagnose.emptyText' AND `profile_id` = (SELECT id FROM `custom_text_profile` WHERE `code` = 'default-offcial'));
