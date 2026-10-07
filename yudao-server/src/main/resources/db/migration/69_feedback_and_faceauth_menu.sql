-- ----------------------------
-- 增量 SQL: 添加小程序反馈列表汇总、人脸识别记录的 Web 端管理菜单
-- ----------------------------

-- 1. 清理可能存在的历史脏数据，确保幂等性（通过 component 唯一性清理）
DELETE FROM system_role_menu WHERE menu_id IN (SELECT id FROM system_menu WHERE component IN ('system/feedback/index', 'system/faceAuth/index'));
DELETE FROM system_menu WHERE component IN ('system/feedback/index', 'system/faceAuth/index');

-- 2. 插入 "反馈列表汇总" 菜单 (挂载在 "系统管理" parent_id = 1 节点下)
INSERT INTO system_menu (
    name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show
) VALUES (
    '反馈列表汇总', '', 2, 99, 1, 'feedback', 'ep:chat-line-round', 'system/feedback/index', 'SystemFeedback', 0, b'1', b'1', b'1'
);
-- 为超级管理员 (role_id = 1) 授予该菜单权限
SET @feedback_menu_id = LAST_INSERT_ID();
INSERT INTO system_role_menu (role_id, menu_id) VALUES (1, @feedback_menu_id);

-- 3. 插入 "人脸识别记录" 菜单 (挂载在 "系统管理" parent_id = 1 节点下)
INSERT INTO system_menu (
    name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show
) VALUES (
    '人脸识别记录', '', 2, 100, 1, 'faceAuth', 'ep:video-play', 'system/faceAuth/index', 'SystemFaceAuthLog', 0, b'1', b'1', b'1'
);
-- 为超级管理员 (role_id = 1) 授予该菜单权限
SET @faceauth_menu_id = LAST_INSERT_ID();
INSERT INTO system_role_menu (role_id, menu_id) VALUES (1, @faceauth_menu_id);

