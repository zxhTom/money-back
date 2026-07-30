-- ============================================================
-- 版本升级说明 菜单与权限（幂等：可重复执行）
-- 跟「皮肤配置」「文案配置」「图标配置」共用同一个顶级目录「内容配置」(id=5193)。
-- ============================================================

-- 1. 页面菜单：版本升级说明（幂等：按 id 或 component_name 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5209, '版本升级说明', 'custom:version-changelog:query', 2, 4, 5193, 'version-changelog', 'ep:bell', 'custom/changelog/versionChangelog/index', 'CustomVersionChangelog', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5209 OR `component_name` = 'CustomVersionChangelog');

-- 2. 按钮权限：新增/修改/删除/发布下线，controller 里这几个操作共用一个 handle 权限（不像图标配置拆得更细）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5210, '版本升级说明管理', 'custom:version-changelog:handle', 3, 1, 5209, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:version-changelog:handle');
