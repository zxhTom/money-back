-- 重名兼容改造：数据库收敛模板（执行前请先完成 compat-audit.sql 治理）
-- 注意：以下语句包含 DDL，请在低峰期执行并先备份。

-- A. 上线前确认：重复记录必须清零（否则不要执行唯一索引）
-- SELECT id_no, COUNT(*) cnt FROM system_users WHERE deleted=b'0' AND id_no IS NOT NULL AND id_no<>'' GROUP BY id_no HAVING COUNT(*)>1;
-- SELECT username, COUNT(*) cnt FROM system_users WHERE deleted=b'0' AND username IS NOT NULL AND username<>'' GROUP BY username HAVING COUNT(*)>1;

-- B. 登录与身份字段收敛
-- 1) 身份证号唯一（仅对未删除且非空生效）
-- MySQL 8 可使用函数索引实现条件唯一
CREATE UNIQUE INDEX idx_id_no_unique
    ON system_users (id_no, (CASE WHEN deleted = 0 AND id_no IS NOT NULL AND id_no <> '' THEN 0 ELSE NULL END));

-- 2) username 唯一（仅对未删除且非空生效）
CREATE UNIQUE INDEX idx_username_unique
    ON system_users (username, (CASE WHEN deleted = 0 AND username IS NOT NULL AND username <> '' THEN 0 ELSE NULL END));

-- C. 合同检索索引（提升同名场景按身份证检索性能）
CREATE INDEX idx_custom_contract_indebted_id ON custom_contract(indebted_id);
CREATE INDEX idx_custom_contract_creditor_id ON custom_contract(creditor_id);
CREATE INDEX idx_custom_contract_status_create_time ON custom_contract(status, create_time);

-- D. 小程序绑定检索索引（不改唯一键，仅增强 user_id 查询）
CREATE INDEX idx_mini_user_user_id ON mini_user(user_id);
