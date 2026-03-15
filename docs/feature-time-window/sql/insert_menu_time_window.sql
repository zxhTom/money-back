-- ============================================================
-- 时间窗口管理 - 挂到「综合查询」下 - 菜单与权限 INSERT SQL
-- 说明：在「综合查询」下新增「时间窗口」目录及列表页、新增/修改/删除按钮权限。
-- 完整路由：/custom/time-window/list（与前端 remaining 一致，假设综合查询 path 为 custom）
-- ============================================================

-- 父级：综合查询
SET @query_parent_id = (SELECT id FROM system_menu WHERE name = '综合查询' LIMIT 1);

-- 1）「时间窗口」目录（type=1），挂在综合查询下
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '时间窗口', '', 1, 15, @query_parent_id, 'time-window', 'ep:clock', '', '',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @time_window_menu_id = LAST_INSERT_ID();

-- 2）「时间窗口列表」菜单（type=2），path=list → 完整路由 /custom/time-window/list
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '时间窗口列表', 'custom:timeWindow:query', 2, 1, @time_window_menu_id, 'list', 'ep:list',
  'custom/timeWindow/index', 'TimeWindow',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @list_menu_id = LAST_INSERT_ID();

-- 3）按钮权限：新增、修改、删除（type=3）
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES
  ('时间窗口新增', 'custom:timeWindow:create', 3, 1, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('时间窗口修改', 'custom:timeWindow:update', 3, 2, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('时间窗口删除', 'custom:timeWindow:delete', 3, 3, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 说明：
-- 1. 综合查询的 path 若为 custom，则完整路由为 /custom/time-window/list，与前端 remaining 一致。
-- 2. 若综合查询 path 不同，请相应调整；或先查：SELECT id, name, path FROM system_menu WHERE name = '综合查询';
-- 3. 执行后需在角色管理中为对应角色分配「综合查询」及本菜单权限。
-- 4. 前端权限标识：v-hasPermi="['custom:timeWindow:create']" 等。
