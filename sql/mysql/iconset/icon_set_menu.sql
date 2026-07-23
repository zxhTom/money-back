-- ============================================================
-- 图标集配置 菜单与权限（幂等版：可重复执行，配合自动迁移器）
-- 与「皮肤配置」「文案配置」共用同一个顶级目录「内容配置」(id=5193)。
-- 本文件不依赖执行顺序：若该目录尚不存在会一并创建，若已存在则直接复用。
-- ============================================================

-- 1. 顶级目录：内容配置（幂等：按 name 判重，与 skin_menu.sql/text_menu.sql 中的语句一致）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5193, '内容配置', '', 1, 20, 0, '/content', 'ep:collection', '', '', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `name` = '内容配置' AND `parent_id` = 0);

-- 2. 页面菜单：图标配置（幂等：按 id 或 component_name 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5204, '图标配置', 'custom:icon-set:query', 2, 3, 5193, 'icon-set', 'ep:picture-filled', 'custom/iconSet/index', 'CustomIconSet', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5204 OR `component_name` = 'CustomIconSet');

-- 3. 按钮权限：新增/修改/删除/启用（幂等：按 permission 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5205, '图标配置新增', 'custom:icon-set:create', 3, 1, 5204, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:icon-set:create');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5206, '图标配置修改', 'custom:icon-set:update', 3, 2, 5204, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:icon-set:update');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5207, '图标配置删除', 'custom:icon-set:delete', 3, 3, 5204, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:icon-set:delete');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5208, '图标配置启用', 'custom:icon-set:use', 3, 4, 5204, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:icon-set:use');
