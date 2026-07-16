-- ============================================================================
-- 16: UA 白名单改为「结构校验 + 客户端 token」的配套数据（增量，无 DELETE）
--   判定逻辑在 UaClassifyService：UA 需先在结构上像真实浏览器/webview，
--   再包含本表任一启用 token 才算白名单。故此处：
--     1) 停用万能词 Mozilla（它会放行任意含 Mozilla 的伪造 UA）——仅置 enabled=0，保留历史；
--     2) 从登录日志 + crawler_ua_pool 实测，补齐真实客户端 token。
-- ============================================================================

UPDATE `custom_ua_whitelist`
SET `enabled` = 0,
    `remark`  = '结构校验上线后停用：万能词会放行任意含 Mozilla 的伪造 UA',
    `updater` = 'system'
WHERE `keyword` = 'Mozilla';

INSERT INTO `custom_ua_whitelist` (`keyword`, `remark`, `enabled`, `creator`, `create_time`)
SELECT * FROM (
    SELECT 'MQQBrowser'          AS keyword, '手机QQ浏览器'      AS remark, 1 AS enabled, 'system' AS creator, NOW() AS create_time
    UNION ALL SELECT 'QQBrowser',          'QQ浏览器',        1, 'system', NOW()
    UNION ALL SELECT 'UCBrowser',          'UC浏览器',        1, 'system', NOW()
    UNION ALL SELECT 'Quark',              '夸克浏览器',      1, 'system', NOW()
    UNION ALL SELECT 'HuaweiBrowser',      '华为浏览器',      1, 'system', NOW()
    UNION ALL SELECT 'HarmonyOS',          '鸿蒙内置',        1, 'system', NOW()
    UNION ALL SELECT 'VivoBrowser',        'vivo浏览器',      1, 'system', NOW()
    UNION ALL SELECT 'MiuiBrowser',        '小米浏览器',      1, 'system', NOW()
    UNION ALL SELECT 'HeyTapBrowser',      'OPPO/一加浏览器', 1, 'system', NOW()
    UNION ALL SELECT 'OppoBrowser',        'OPPO浏览器',      1, 'system', NOW()
    UNION ALL SELECT 'baiduboxapp',        '百度App内置',     1, 'system', NOW()
    UNION ALL SELECT 'SogouMobileBrowser', '搜狗浏览器',      1, 'system', NOW()
    UNION ALL SELECT 'MetaSr',             '搜狗内核',        1, 'system', NOW()
    UNION ALL SELECT 'CriOS',              'iOS 版 Chrome',   1, 'system', NOW()
    UNION ALL SELECT 'FxiOS',              'iOS 版 Firefox',  1, 'system', NOW()
    UNION ALL SELECT 'EdgA',               'Android 版 Edge', 1, 'system', NOW()
    UNION ALL SELECT 'SamsungBrowser',     '三星浏览器',      1, 'system', NOW()
    UNION ALL SELECT 'OPR',                'Opera(新内核)',   1, 'system', NOW()
) x
WHERE NOT EXISTS (SELECT 1 FROM `custom_ua_whitelist` w WHERE w.`keyword` = x.`keyword`);
