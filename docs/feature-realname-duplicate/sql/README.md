# 真实姓名重名支持 - 表结构说明

## 1. 本需求涉及的表

- **system_users**：用户表，含 `realname`、`id_no` 等字段，用于注册与校验。
- **custom_contract**：合同表，含 `indebted_id`、`creditor_id`（存身份证号）、`indebted_name`、`creditor_name`，用于 24 小时删除条件。

## 2. 约束检查与变更

### 2.1 realname 唯一约束

- **目标**：支持 realname 重名，因此 **不得** 对 `system_users.realname` 存在唯一索引或唯一约束。
- **操作**：若项目中已对 `realname` 建唯一索引或 UNIQUE 约束，需删除后再上线本需求。  
  - 示例（仅当存在时执行）：  
    `ALTER TABLE system_users DROP INDEX uk_realname;`  
    （索引名以实际库表为准，可用 `SHOW INDEX FROM system_users` 查看。）

### 2.2 idNo 唯一约束

- **目标**：身份证号全局唯一，必须保留。
- **操作**：确保 `system_users.id_no` 有唯一索引或 UNIQUE 约束；若没有，应新增，例如：  
  `ALTER TABLE system_users ADD UNIQUE INDEX uk_id_no (id_no);`  
  （若表结构已包含则无需执行。）

## 3. 无新增表与菜单

本需求不新增表、不新增菜单；仅调整既有逻辑与上述约束。
