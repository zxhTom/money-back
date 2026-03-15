-- ============================================================
-- 时间窗口管理 - 建表 SQL（与 docs/feature-time-window 一致）
-- ============================================================

CREATE TABLE IF NOT EXISTS `time_window` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `start_time`          DATETIME      NOT NULL COMMENT '开始时间',
  `end_time`            DATETIME      NOT NULL COMMENT '结束时间',
  `quick_select`        TINYINT       DEFAULT NULL COMMENT '快捷选择：1=+1小时 3=+3小时 7=+7小时（可选）',
  `target_type`         VARCHAR(10)   NOT NULL DEFAULT 'user' COMMENT '按用户或按角色：user=按用户 role=按角色',
  `selected_user_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '选定用户ID列表，JSON数组，targetType=user时使用',
  `excluded_user_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '排除用户ID列表，JSON数组，与选定重合时以排除为准',
  `selected_role_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '选定角色ID列表，JSON数组，targetType=role时使用',
  `excluded_role_ids`   VARCHAR(2000) DEFAULT '[]' COMMENT '排除角色ID列表，JSON数组，与选定重合时以排除为准',
  `status`              TINYINT       NOT NULL DEFAULT 1 COMMENT '激活状态：0=未激活 1=激活（仅激活记录参与重合校验）',
  `creator`             VARCHAR(64)   DEFAULT '' COMMENT '创建者',
  `create_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`             VARCHAR(64)   DEFAULT '' COMMENT '更新者',
  `update_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_time_window_status` (`status`, `deleted`),
  KEY `idx_time_window_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间窗口';
