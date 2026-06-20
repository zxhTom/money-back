CREATE TABLE custom_user_ip_history (
  id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id    BIGINT      NOT NULL                COMMENT '用户ID',
  ip         VARCHAR(64) NOT NULL                COMMENT 'IP地址',
  first_seen DATETIME    NOT NULL                COMMENT '首次使用时间',
  last_seen  DATETIME    NOT NULL                COMMENT '最近使用时间',
  count      INT         NOT NULL DEFAULT 1      COMMENT '使用次数',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_ip (user_id, ip),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户IP使用记录（独立存储，不依赖日志表）';
