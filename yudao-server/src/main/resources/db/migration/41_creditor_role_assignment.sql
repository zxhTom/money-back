-- ============================================================================
-- 将指定名单的用户加入 creditor（债权人）角色（依赖 40_creditor_role.sql 先创建该角色）。
-- 与 10_invite_code.sql 里同一份名单的既有处理口径保持一致：按 realname 直接匹配，
-- 如有重名(多账号同名)则全部授予——该角色只是放开"可查询任意人信用"这一单一副作用，
-- 多授予一个同名账号的风险远低于漏授予；如需人工复核，请在执行后自行核对
-- system_user_role 里对应role_id的记录是否符合预期。
-- ============================================================================

INSERT INTO `system_user_role` (`user_id`, `role_id`, `creator`, `create_time`)
SELECT u.id, r.id, '1', NOW()
FROM `system_users` u
JOIN `system_role` r ON r.code = 'creditor' AND r.deleted = b'0'
WHERE u.deleted = 0
  AND u.realname IN ('冯书涛','史荣梅','陈积平','张强','刘橙枫','赵志祥','张厚涛','罗意洲','胡花枝','张显')
  AND NOT EXISTS (
      SELECT 1 FROM `system_user_role` sur WHERE sur.user_id = u.id AND sur.role_id = r.id
  );
