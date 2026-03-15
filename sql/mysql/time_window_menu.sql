-- ============================================================
-- 时间窗口管理 - 菜单与权限（挂到「综合查询」下）
-- 执行前请确认存在「综合查询」菜单；若无则先创建或修改 parent_id。
-- ============================================================

SET @query_parent_id = (SELECT id FROM system_menu WHERE name = '综合查询' LIMIT 1);

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '时间窗口', '', 1, 15, @query_parent_id, 'time-window', 'ep:clock', '', '',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @time_window_menu_id = LAST_INSERT_ID();

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '时间窗口列表', 'custom:timeWindow:query', 2, 1, @time_window_menu_id, 'list', 'ep:list',
  'custom/timeWindow/index', 'TimeWindow',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @list_menu_id = LAST_INSERT_ID();

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES
  ('时间窗口新增', 'custom:timeWindow:create', 3, 1, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('时间窗口修改', 'custom:timeWindow:update', 3, 2, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('时间窗口删除', 'custom:timeWindow:delete', 3, 3, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0);
