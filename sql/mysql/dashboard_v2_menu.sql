-- ============================================================
-- 数据大盘 v2 - 菜单与权限（MySQL）
-- 执行一次即可；重复执行不会报错（INSERT 不含唯一冲突）
-- ============================================================

-- ① 顶级目录「数据大盘」（parent_id=0 表示根级）
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '数据大盘', '', 1, 5, 0, '/dashboard-v2', 'ep:data-line', '', '',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @dashboard_dir_id = LAST_INSERT_ID();

-- ② 页面「数据大盘」（type=2，绑定前端组件 Dashboard/index）
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '数据大盘', '', 2, 1, @dashboard_dir_id, 'index', 'ep:data-line',
  'Dashboard/index', 'DashboardV2',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @dashboard_page_id = LAST_INSERT_ID();

-- ③ 按钮权限「数据大盘查询」（type=3，对应 @PreAuthorize 中的 custom:dashboard:query）
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '数据大盘查询', 'custom:dashboard:query', 3, 1, @dashboard_page_id, '', '', '', '',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @dashboard_query_perm_id = LAST_INSERT_ID();

-- ④ 将以上三个菜单项授权给超级管理员角色（role_id=1）
--    若超级管理员走 data_scope=1 自动全权，可跳过这步；加上也无副作用
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
VALUES
  (1, @dashboard_dir_id,       '1', NOW(), '1', NOW(), 0),
  (1, @dashboard_page_id,      '1', NOW(), '1', NOW(), 0),
  (1, @dashboard_query_perm_id,'1', NOW(), '1', NOW(), 0);
