-- ============================================================
-- IP黑名单：增加封禁次数 + 历史日志表
-- ============================================================

-- 1. 主表加字段
ALTER TABLE custom_ip_blacklist
  ADD COLUMN ban_count INT          NOT NULL DEFAULT 1    COMMENT '累计封禁次数' AFTER reason,
  ADD COLUMN status    TINYINT(1)   NOT NULL DEFAULT 0    COMMENT '0=封禁中 1=已解封' AFTER ban_count;

-- 2. 历史日志表（每次封禁写一条，永久保留）
CREATE TABLE custom_ip_blacklist_log (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  ip          VARCHAR(64)  NOT NULL                COMMENT 'IP地址',
  reason      VARCHAR(512)     NULL                COMMENT '封禁原因',
  auto_added  TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '0=手动 1=自动',
  expire_time DATETIME         NULL                COMMENT '本次封禁过期时间，NULL=永久',
  create_time DATETIME     NOT NULL                COMMENT '封禁时间',
  PRIMARY KEY (id),
  KEY idx_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP封禁历史日志';

-- 3. 将现有记录迁移到日志表
INSERT INTO custom_ip_blacklist_log (ip, reason, auto_added, expire_time, create_time)
SELECT ip, reason, IFNULL(auto_added, 0), expire_time, IFNULL(create_time, NOW())
FROM custom_ip_blacklist;
