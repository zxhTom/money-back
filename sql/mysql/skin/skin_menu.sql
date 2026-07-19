-- ============================================================
-- 皮肤配置 菜单与权限（幂等版：可重复执行，配合自动迁移器）
-- 挂靠父级目录「内容配置」：项目暂无现成的内容类分组（对比 系统管理/综合查询/数据大盘 等既有顶级目录），
-- 因此新建一个顶级目录承载皮肤配置、文案配置等小程序内容配置类页面（若已存在则复用，不重复创建）。
-- ============================================================

-- 1. 顶级目录：内容配置（幂等：按 name 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5193, '内容配置', '', 1, 20, 0, '/content', 'ep:collection', '', '', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `name` = '内容配置' AND `parent_id` = 0);

SET @content_parent_id = (SELECT id FROM `system_menu` WHERE `name` = '内容配置' AND `parent_id` = 0 LIMIT 1);

-- 2. 页面菜单：皮肤配置（幂等：按 id 或 component_name 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5194, '皮肤配置', 'custom:skin:query', 2, 1, @content_parent_id, 'skin', 'ep:brush-filled', 'custom/skin/index', 'CustomSkin', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5194 OR `component_name` = 'CustomSkin');

SET @skin_page_id = (SELECT id FROM `system_menu` WHERE `component_name` = 'CustomSkin' LIMIT 1);

-- 3. 按钮权限：新增/修改/删除/启用（幂等：按 permission 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5195, '皮肤配置新增', 'custom:skin:create', 3, 1, @skin_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:skin:create');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5196, '皮肤配置修改', 'custom:skin:update', 3, 2, @skin_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:skin:update');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5197, '皮肤配置删除', 'custom:skin:delete', 3, 3, @skin_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:skin:delete');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5198, '皮肤配置启用', 'custom:skin:use', 3, 4, @skin_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:skin:use');
