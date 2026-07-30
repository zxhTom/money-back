-- ============================================================
-- 小程序静态信息配置 菜单与权限（幂等：可重复执行）
-- 跟「皮肤配置」「文案配置」「图标配置」「版本升级说明」共用同一个顶级目录「内容配置」(id=5193)。
-- ============================================================

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5211, '小程序信息配置', 'custom:miniprogram-config:query', 2, 5, 5193, 'miniprogram-config', 'ep:info-filled', 'custom/miniprogram/config/index', 'CustomMiniProgramConfig', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5211 OR `component_name` = 'CustomMiniProgramConfig');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5212, '小程序信息配置管理', 'custom:miniprogram-config:handle', 3, 1, 5211, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:miniprogram-config:handle');
