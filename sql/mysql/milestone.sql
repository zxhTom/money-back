-- ============================================================
-- 系统关键时间点 / 节点提醒表建表 SQL 与 菜单权限
-- ============================================================

CREATE TABLE IF NOT EXISTS `system_milestone` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`               VARCHAR(128)  NOT NULL COMMENT '时间点名称（如：小程序年审、域名续费等）',
  `category`            VARCHAR(64)   NOT NULL DEFAULT 'OTHER' COMMENT '分类：MINI_APP=小程序, PAY=支付平台, DOMAIN=域名, SERVER=服务器, WECHAT_WORK=企业微信, OTHER=其他',
  `target_date`         DATE          NOT NULL COMMENT '关键时间节点（到期日/年审截止日）',
  `remind_days`         INT           NOT NULL DEFAULT 30 COMMENT '提前提醒天数（默认30天）',
  `status`              TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0=待处理 1=处理中 2=已完成 3=忽略',
  `priority`            VARCHAR(32)   NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：HIGH=紧急, MEDIUM=普通, LOW=低',
  `owner`               VARCHAR(64)   DEFAULT '' COMMENT '负责人',
  `cost`                DECIMAL(10,2) DEFAULT NULL COMMENT '费用金额（续费/年审费用）',
  `remark`              TEXT          DEFAULT NULL COMMENT '备注与操作说明路径（例如配置入口链接、年审所需材料清单）',
  `tenant_id`           BIGINT        NOT NULL DEFAULT 1 COMMENT '租户编号',
  `creator`             VARCHAR(64)   DEFAULT '' COMMENT '创建者',
  `create_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`             VARCHAR(64)   DEFAULT '' COMMENT '更新者',
  `update_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_milestone_target_date` (`target_date`, `status`, `deleted`),
  KEY `idx_milestone_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统关键时间节点表';

-- 初始化预设示例数据（避免空列表）
INSERT INTO `system_milestone` (`title`, `category`, `target_date`, `remind_days`, `status`, `priority`, `owner`, `cost`, `remark`, `creator`) VALUES
('微信小程序年度年审认证', 'MINI_APP', DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 30, 0, 'HIGH', '张主管', 300.00, '需要在微信公众平台提交营业执照与法人身份验证，缴纳年审服务费300元。', '1'),
('微信支付商户号主体及费率巡检', 'PAY', DATE_ADD(CURRENT_DATE, INTERVAL 45 DAY), 30, 0, 'MEDIUM', '财务部', 0.00, '检查微信支付API密钥更新状态、商户证书到期时间及年审要求。', '1'),
('微信开放平台/企业微信认证续期', 'WECHAT_WORK', DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 30, 0, 'HIGH', 'IT管理员', 300.00, '登录企业微信后台完成企微主体认证与小程序关联授权。', '1'),
('主域名 toms.chat SSL证书续期与替换', 'DOMAIN', DATE_ADD(CURRENT_DATE, INTERVAL 8 DAY), 15, 0, 'HIGH', '运维组', 0.00, '免费Let\'s Encrypt证书到期前完成自动续签或手动在阿里云/腾讯云下发新证书部署。', '1'),
('阿里云ECS服务器到期续费', 'SERVER', DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY), 30, 0, 'MEDIUM', '运维组', 2400.00, '47.121.182.177 核心服务器续费，评估是否需要升级带宽与云盘规格。', '1'),
('域名 toms.chat 注册局续费', 'DOMAIN', DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY), 60, 0, 'LOW', '行政部', 88.00, '在域名注册商后台完成续费1年，防止域名过期被抢注。', '1');

-- 菜单权限 SQL
SET @query_parent_id = (SELECT id FROM system_menu WHERE name = '综合查询' LIMIT 1);

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '时间节点预警', '', 1, 16, IFNULL(@query_parent_id, 0), 'milestone', 'ep:calendar', '', '',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @milestone_menu_id = LAST_INSERT_ID();

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES (
  '节点看板', 'custom:milestone:query', 2, 1, @milestone_menu_id, 'index', 'ep:timer',
  'custom/milestone/index', 'CustomMilestoneIndex',
  0, 1, 0, '1', NOW(), '1', NOW(), 0
);

SET @list_menu_id = LAST_INSERT_ID();

INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, creator, create_time, updater, update_time, deleted
) VALUES
  ('节点新增', 'custom:milestone:create', 3, 1, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('节点修改', 'custom:milestone:update', 3, 2, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0),
  ('节点删除', 'custom:milestone:delete', 3, 3, @list_menu_id, '', '', '', '', 0, 1, 0, '1', NOW(), '1', NOW(), 0);
