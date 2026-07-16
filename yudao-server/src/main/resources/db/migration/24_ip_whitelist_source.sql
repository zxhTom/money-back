-- 白名单来源：MANUAL=手动添加(默认，同步永不触碰) / WECHAT=微信官方getcallbackip自动同步
ALTER TABLE custom_ip_whitelist
    ADD COLUMN source VARCHAR(16) NOT NULL DEFAULT 'MANUAL' COMMENT '来源: MANUAL手动 / WECHAT微信官方';
-- 把已灌入的89个微信IP标记为 WECHAT，其余保持 MANUAL
UPDATE custom_ip_whitelist SET source='WECHAT' WHERE remark LIKE '%getcallbackip%';
