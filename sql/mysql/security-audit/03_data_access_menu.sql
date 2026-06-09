-- ============================================================
-- 数据访问监控 菜单权限（MySQL）
-- 前置条件：02_menu.sql 已执行，「安全监控」目录已存在
-- 本脚本将「数据访问配置」和「访问日志」挂在「安全监控」目录下
-- 注意：需将下方 @security_dir_id 替换为实际的「安全监控」目录 ID
-- ============================================================

-- 查找安全监控目录 ID（执行前先确认 path='security-monitor' 的菜单存在）
SET @security_dir_id = (SELECT id FROM system_menu WHERE path = 'security-monitor' AND deleted = 0 LIMIT 1);

-- 「数据访问配置」页面
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '访问监控配置', '', 2, 3, @security_dir_id, 'data-access-config', 'ep:setting',
  'custom/security/dataAccess/config/index', 'DataAccessConfig',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @config_menu_id = LAST_INSERT_ID();

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES
  ('配置查询', 'system:data-access:config:query',  3, 1, @config_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('创建配置', 'system:data-access:config:create', 3, 2, @config_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('更新配置', 'system:data-access:config:update', 3, 3, @config_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('删除配置', 'system:data-access:config:delete', 3, 4, @config_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0);

-- 「数据访问日志」页面
INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '数据访问日志', '', 2, 4, @security_dir_id, 'data-access-log', 'ep:document',
  'custom/security/dataAccess/log/index', 'DataAccessLog',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @log_menu_id = LAST_INSERT_ID();

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES
  ('日志查询', 'system:data-access:log:query', 3, 1, @log_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0);
