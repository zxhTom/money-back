-- ============================================================
-- 文案配置 菜单与权限（幂等版：可重复执行，配合自动迁移器）
-- 与「皮肤配置」共用同一个顶级目录「内容配置」（见 sql/mysql/skin/skin_menu.sql）。
-- 本文件不依赖执行顺序：若该目录尚不存在会一并创建，若已存在则直接复用。
-- ============================================================

-- 1. 顶级目录：内容配置（幂等：按 name 判重，与 skin_menu.sql 中的语句一致，保证任一文件先执行都可用）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5193, '内容配置', '', 1, 20, 0, '/content', 'ep:collection', '', '', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `name` = '内容配置' AND `parent_id` = 0);

SET @content_parent_id = (SELECT id FROM `system_menu` WHERE `name` = '内容配置' AND `parent_id` = 0 LIMIT 1);

-- 2. 页面菜单：文案配置（幂等：按 id 或 component_name 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5199, '文案配置', 'custom:text:query', 2, 2, @content_parent_id, 'text', 'ep:edit-pen', 'custom/text/index', 'CustomText', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5199 OR `component_name` = 'CustomText');

SET @text_page_id = (SELECT id FROM `system_menu` WHERE `component_name` = 'CustomText' LIMIT 1);

-- 3. 按钮权限：新增/修改/删除/启用（幂等：按 permission 判重）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5200, '文案配置新增', 'custom:text:create', 3, 1, @text_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:text:create');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5201, '文案配置修改', 'custom:text:update', 3, 2, @text_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:text:update');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5202, '文案配置删除', 'custom:text:delete', 3, 3, @text_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:text:delete');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5203, '文案配置启用', 'custom:text:use', 3, 4, @text_page_id, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:text:use');
