-- ============================================================
-- 时间窗口 - 新增「按角色」相关字段（增量）
-- 执行前提：表 time_window 已存在
-- ============================================================

ALTER TABLE `time_window`
  ADD COLUMN `target_type` VARCHAR(10) NOT NULL DEFAULT 'user' COMMENT '按用户或按角色：user=按用户 role=按角色' AFTER `quick_select`,
  ADD COLUMN `selected_role_ids` VARCHAR(2000) NOT NULL DEFAULT '[]' COMMENT '选定角色ID列表，JSON数组，targetType=role时使用' AFTER `excluded_user_ids`,
  ADD COLUMN `excluded_role_ids` VARCHAR(2000) NOT NULL DEFAULT '[]' COMMENT '排除角色ID列表，JSON数组，与选定重合时以排除为准' AFTER `selected_role_ids`;
