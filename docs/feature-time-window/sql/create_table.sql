-- ============================================================
-- 时间窗口管理 - 建表 SQL
-- 说明：存储时间窗口记录（开始/结束时间、选定用户、排除用户、激活状态）。
-- 执行前请根据项目规范调整：表名前缀、引擎、字符集、tenant_id 等。
-- ============================================================

-- 表名可按项目规范修改，如：biz_time_window、sys_time_window_config
CREATE TABLE IF NOT EXISTS `time_window` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `start_time`          DATETIME     NOT NULL COMMENT '开始时间',
  `end_time`            DATETIME     NOT NULL COMMENT '结束时间',
  `quick_select`        TINYINT      DEFAULT NULL COMMENT '快捷选择：1=+1小时 3=+3小时 7=+7小时（可选，用于回显）',
  `target_type`         VARCHAR(10)  NOT NULL DEFAULT 'user' COMMENT '按用户或按角色：user=按用户 role=按角色',
  `selected_user_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '选定用户ID列表，JSON数组，targetType=user时使用',
  `excluded_user_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '排除用户ID列表，JSON数组，与选定重合时以排除为准',
  `selected_role_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '选定角色ID列表，JSON数组，targetType=role时使用',
  `excluded_role_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '排除角色ID列表，JSON数组，与选定重合时以排除为准',
  `status`              TINYINT      NOT NULL DEFAULT 1 COMMENT '激活状态：0=未激活 1=激活（仅激活记录参与重合校验）',
  `creator`             VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`             VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             BIT(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`           BIGINT      NOT NULL DEFAULT 0 COMMENT '租户编号（若项目多租户则保留，否则可删或置0）',
  PRIMARY KEY (`id`),
  KEY `idx_time_window_status` (`status`, `deleted`),
  KEY `idx_time_window_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间窗口';

-- 说明：
-- 1. selected_user_ids / excluded_user_ids 存 JSON 数组字符串，如 "[1,2,3]"，后端解析为 List<Long>。
-- 2. 若使用 PostgreSQL，可将 VARCHAR(2000) 改为 TEXT，BIGINT/BIT 按 PG 习惯调整。
-- 3. 若无多租户，可删除 tenant_id 列或在业务中忽略。
-- 4. 重合阈值（默认 1 小时）建议放在系统参数表或配置表，如 key = time_window.overlap_threshold_hours，value = 1。
