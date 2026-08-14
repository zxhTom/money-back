-- ============================================================
-- 多租户隔离改造（方案B）：custom_contract 表加 tenant_id
-- 并初始化默认租户 + 新建测试租户
--
-- 幂等策略：不要用 SET @var / PREPARE / EXECUTE。
-- SqlMigrationRunner 对每条语句单独调 JdbcTemplate.execute()，没有事务包裹、
-- 也不保证同一条物理连接，MySQL 的会话级用户变量和 prepared statement 跨语句可能丢失。
-- 重复列由执行器的 isAlreadyExists() 兜底（错误码 1060 直接跳过），
-- 其余记录级插入一律用 INSERT ... SELECT ... WHERE NOT EXISTS，与本目录其余迁移文件写法一致。
-- ============================================================

-- 1. custom_contract 新增 tenant_id 列，存量数据默认归租户 1
--    列已存在时 MySQL 报 1060，执行器识别为"对象已存在"自动跳过，可重复执行
ALTER TABLE `custom_contract` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';

-- 2. 默认租户改名 + 绑定总域名 org.toms.chat（幂等：已改过则匹配不到，不变）
UPDATE system_tenant SET name = '总平台', websites = 'org.toms.chat' WHERE id = 1 AND name = '芋道源码';

-- 3. 新建测试租户，绑定专属域名 tdk.toms.chat（幂等：已存在不重复插入）
INSERT INTO system_tenant (id, name, contact_name, contact_mobile, status, websites, package_id, expire_time, account_count, deleted, create_time, update_time, creator, updater)
SELECT 2, '新平台', '管理员', '', 0, 'tdk.toms.chat', 0, '2099-02-19 17:14:16', 9999, 0, NOW(), NOW(), 'system', 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_tenant WHERE id = 2);

-- 4. 为新租户创建一个初始管理员账号 username=admin2（幂等）
--    密码为 bcrypt 哈希，明文不落库、不进仓库。口令由项目负责人单独保管，首次登录后请立即修改。
INSERT INTO system_users (id, username, password, nickname, status, tenant_id, dept_id, deleted, create_time, update_time, creator, updater)
SELECT 2000, 'admin2', '$2a$10$J8kEyF/no3q0oqX7mjMGdu9DvYYIdi0GsWRCYgFvVvbj../Gc.q7a', '新平台管理员', 0, 2, 100, 0, NOW(), NOW(), 'system', 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_users WHERE username = 'admin2');

-- 5. 为 admin2 分配角色（最低限度能登录后台看到菜单），复用已有的管理员角色 id=1
INSERT INTO system_user_role (user_id, role_id, tenant_id, creator, create_time, updater, update_time, deleted)
SELECT 2000, 1, 2, 'system', NOW(), 'system', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_user_role WHERE user_id = 2000 AND role_id = 1);
