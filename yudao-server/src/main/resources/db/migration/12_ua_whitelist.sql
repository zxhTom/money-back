-- ============================================================================
-- 浏览器 User-Agent 白名单（增量）
--   模拟请求判定新增一条：UA 不像浏览器 → 可疑（宽松，累加到阈值）；
--   明确的自动化工具 UA（python-requests / curl / urllib 等）在代码里硬判定，立即封IP。
--   本表的关键词来自登录日志实测的合法浏览器家族（含微信内置浏览器）。
--   UA 里包含任一启用关键词（不区分大小写）即视为合法浏览器。可运营维护。
-- ============================================================================

CREATE TABLE IF NOT EXISTS `custom_ua_whitelist` (
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `keyword`     varchar(64) NOT NULL COMMENT 'UA 关键词（含即放行，不区分大小写）',
    `remark`      varchar(128) DEFAULT '' COMMENT '备注',
    `enabled`     tinyint     NOT NULL DEFAULT 1 COMMENT '是否启用(0-否 1-是)',
    `creator`     varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览器UA白名单';

-- 从登录日志实测的合法浏览器家族（Mozilla 作为最宽松的兜底，覆盖所有真实浏览器/内置webview）
INSERT INTO `custom_ua_whitelist` (`keyword`, `remark`, `enabled`, `creator`, `create_time`)
SELECT * FROM (
    SELECT 'Mozilla'        AS keyword, '所有真实浏览器/内置webview 前缀（最宽松兜底）' AS remark, 1 AS enabled, 'system' AS creator, NOW() AS create_time
    UNION ALL SELECT 'MicroMessenger', '微信内置浏览器（本项目主要来源）', 1, 'system', NOW()
    UNION ALL SELECT 'Chrome',  'Chrome/Chromium 系', 1, 'system', NOW()
    UNION ALL SELECT 'Safari',  'Safari', 1, 'system', NOW()
    UNION ALL SELECT 'Edg',     'Edge', 1, 'system', NOW()
    UNION ALL SELECT 'Firefox', 'Firefox', 1, 'system', NOW()
    UNION ALL SELECT 'Opera',   'Opera', 1, 'system', NOW()
) t
WHERE NOT EXISTS (SELECT 1 FROM `custom_ua_whitelist` w WHERE w.keyword = t.keyword);
