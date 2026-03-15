-- 为 custom_contract 表添加 dept_id 字段，用于数据权限控制
-- 执行此 SQL 前，请确保已备份数据库

-- 添加 dept_id 字段
ALTER TABLE `custom_contract` 
ADD COLUMN `dept_id` bigint NULL COMMENT '部门ID，用于数据权限控制，关联 system_dept 表的 id 字段' AFTER `refund`;

-- 为已有数据设置默认部门ID（可选，根据实际业务需求调整）
-- 如果合同创建时没有指定部门，可以设置为创建人所属的部门
-- UPDATE custom_contract cc
-- INNER JOIN system_users su ON cc.creator = su.id
-- SET cc.dept_id = su.dept_id
-- WHERE cc.dept_id IS NULL;

-- 添加索引（可选，如果经常按部门查询可以添加）
-- CREATE INDEX `idx_dept_id` ON `custom_contract` (`dept_id`);

