-- ============================================================
-- 用户密码强度字段
-- 支持重复执行（兼容 MySQL 5.7+）
-- ============================================================

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME  = 'system_users'
    AND COLUMN_NAME = 'password_strength'
);

SET @sql = IF(@col_exists = 0,
  'ALTER TABLE system_users ADD COLUMN `password_strength` TINYINT NOT NULL DEFAULT 0 COMMENT \'密码强度：0=未知（历史存量），1=弱，2=中，3=强\'',
  'SELECT ''column already exists'' AS info'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
