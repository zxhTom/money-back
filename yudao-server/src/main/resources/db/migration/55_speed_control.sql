-- ============================================================
-- 访问速度控制（限速）：单例配置表 + 菜单权限（幂等：可重复执行）
-- 菜单挂在现有「内容配置」顶级目录 (id=5193) 下，与皮肤/文案/图标/版本说明同级。
--
-- 紧急关闭（限速配置得离谱、后台进不去时的兜底）：
--   UPDATE custom_speed_control_config SET enabled = 0 WHERE id = 1;
-- 配置快照缓存 10 秒，执行后最多 10 秒生效，无需重启。
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_speed_control_config` (
    `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '主键，固定只有一行，约定用 id=1',
    `enabled`          bit(1)        NOT NULL DEFAULT b'0' COMMENT '限速总开关，默认关闭',
    `rate`             decimal(6,2)  NOT NULL DEFAULT 100.00 COMMENT '速率 1-100，越大越快；100=不降速。仅在 range_enabled=0 时生效',
    `range_enabled`    bit(1)        NOT NULL DEFAULT b'0' COMMENT '是否启用响应时间范围模式（优先于 rate）',
    `min_ms`           bigint        NOT NULL DEFAULT 0 COMMENT '时间范围下限(ms)',
    `max_ms`           bigint        NOT NULL DEFAULT 0 COMMENT '时间范围上限(ms)',
    `ref_fast_ms`      bigint        NOT NULL DEFAULT 100 COMMENT '参考区间下界(ms)：净耗时<=该值的接口映射到 min_ms',
    `ref_slow_ms`      bigint        NOT NULL DEFAULT 10000 COMMENT '参考区间上界(ms)：净耗时>=该值的接口映射到 max_ms',
    `max_delay_ms`     bigint        NOT NULL DEFAULT 600000 COMMENT '单次注入延迟硬上限(ms)，兜底防止误配置把请求挂死',
    `jitter_percent`   decimal(5,2)  NOT NULL DEFAULT 2.00 COMMENT '抖动百分比，最终时长上下浮动该比例，避免时长过于机械',
    `exempt_user_ids`  varchar(2000) NOT NULL DEFAULT '' COMMENT '豁免用户ID，逗号分隔（黑名单式：默认全员受控，名单内豁免）',
    `exempt_role_ids`  varchar(2000) NOT NULL DEFAULT '' COMMENT '豁免角色ID，逗号分隔；用户任一角色命中即豁免',
    `creator`          varchar(64)   DEFAULT '' COMMENT '创建者',
    `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64)   DEFAULT '' COMMENT '更新者',
    `update_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问速度控制配置（单例）';

INSERT INTO `custom_speed_control_config` (id, enabled, rate, range_enabled, min_ms, max_ms, creator, updater)
SELECT 1, b'0', 100.00, b'0', 0, 0, 'system', 'system'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_speed_control_config` WHERE id = 1);

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`)
SELECT 5213, '访问速度控制', 'custom:speed-control:query', 2, 6, 5193, 'speed-control', 'ep:stopwatch', 'custom/speedControl/index', 'CustomSpeedControl', 0, b'1', b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 5213 OR `component_name` = 'CustomSpeedControl');

INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `status`, `visible`, `keep_alive`, `creator`, `create_time`)
SELECT 5214, '访问速度控制管理', 'custom:speed-control:handle', 3, 1, 5213, 0, b'1', b'1', 'system', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'custom:speed-control:handle');
