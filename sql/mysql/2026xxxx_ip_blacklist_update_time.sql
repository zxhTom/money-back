-- 黑名单增加 update_time：新封/再次封禁（ban_count+1 的 UPDATE）都会自动刷新，用于"最新被封排最前"
ALTER TABLE custom_ip_blacklist
    ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近封禁时间' AFTER create_time;
-- 存量数据：先对齐到各自的创建时间
UPDATE custom_ip_blacklist SET update_time = create_time;
