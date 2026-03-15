# 真实姓名重名支持 - 后端改造说明

## 1. 功能概述

本需求在**保持身份证号（idNo）唯一**的前提下，支持**真实姓名（realname）重名**。即：多名用户可填写相同真实姓名，以身份证号区分唯一身份；注册、校验、删除等逻辑需按「身份证唯一、姓名可重复」规则调整。

### 1.1 业务规则摘要

- **身份证号**：全局唯一，注册与业务校验均以 idNo 区分用户。
- **真实姓名**：允许重复，不再做「姓名唯一」校验。
- **校验与删除**：凡依赖「当前用户」或「唯一身份」的逻辑，一律改为按 **idNo**（或 userId）识别，不得仅用 realname，避免误操作同名他人数据。

详细业务规则见：[business-rules.md](./business-rules.md)。

---

## 2. 后端需修改项

以下为按需求包规则列出的改造点，实现后需保证联调与回归通过。

### 2.1 注册：去掉 realname 唯一校验

- **位置**：`yudao-module-mini` → `CustomDefineServiceImpl.validateRegisterUserUnique`
- **修改**：删除或注释「校验真实姓名唯一」整段（`selectByRealnameEqual` + `USER_REALNAME_EXISTS`），保留手机号、身份证号、用户名的唯一性校验。
- **效果**：同姓名、不同身份证的用户可同时注册。

### 2.2 校验用户信息：按「姓名 + 身份证」匹配

- **位置**：`CustomDefineServiceImpl.checkUserInfo`
- **修改**：`getUserListByRealname(realname)` 得到列表后，遍历列表，用请求中的 `idNo` 与每条记录的 `idNo` 比较，存在匹配则返回 true，否则 false；不再仅取 `get(0)` 比较。
- **效果**：支持同名多用户时，仅当「姓名 + 身份证」都匹配才通过校验。

### 2.3 24 小时内合同删除：按当前用户 idNo 删

- **位置**：  
  - Service：`CustomDefineServiceImpl.delete24HourContract`  
  - Mapper：`CustomDefineMapper.delete24HourContract`、`CustomDefineMapper.xml`
- **修改**：  
  - 入参由「realname」改为「当前用户 idNo」；  
  - SQL 条件由 `(indebted_name = #{realname} or creditor_name = #{realname})` 改为 `(indebted_id = #{idNo} or creditor_id = #{idNo})`，并保留 24 小时时间条件。
- **效果**：只软删当前登录用户本人 24 小时内创建的合同，不影响同名其他用户。

### 2.4 可选：后台用户 realname 唯一放宽

- **位置**：`yudao-module-system` → `AdminUserServiceImpl.validateRealnameUnique`
- **说明**：若后台「用户管理」也需支持同名多用户，可在此放宽或去掉 realname 唯一校验；若仅 custom 注册允许重名，可不改。

### 2.5 数据库

- **检查**：若 `system_users` 表存在 `realname` 唯一索引/约束，需删除，否则无法写入重名。  
- **保留**：`idNo` 唯一约束必须保留。  
- 详见：[sql/README.md](./sql/README.md)（若有约束变更则提供 ALTER 说明）。

---

## 3. 受影响的既有接口

本需求**不新增接口**，仅改变以下既有接口或内部逻辑的行为：

| 接口/能力 | 变化说明 |
|-----------|----------|
| `POST /custom/contract/dashboard/register` | 允许 realname 与已有用户重复，仅 idNo/username 等保持唯一 |
| `POST /custom/contract/dashboard/checkUserInfo` | 请求体仍为 realname + idNo；匹配规则改为「姓名列表 + idNo 精确匹配」 |
| `DELETE /custom/contract/dashboard/delete24HourContract` | 仅删除当前登录用户（按 idNo）24 小时内合同 |

接口请求/响应结构不变，详见 [api.md](./api.md)。

---

## 4. 验收要点

- 两名同 realname、不同 idNo 的用户可成功注册。
- `checkUserInfo(realname, idNo)` 仅在同名用户中存在 idNo 一致时返回 true。
- `delete24HourContract` 只软删当前用户（按 idNo）24 小时内合同，不影响同名其他用户。
- 合同列表、创建合同、微信模板发送、找回密码等仍按 idNo 或 userId 识别用户，行为正确。

---

## 5. 文档索引

| 文档 | 说明 |
|------|------|
| [README.md](./README.md) | 本文件，改造总览 |
| [business-rules.md](./business-rules.md) | 业务规则（姓名可重、身份证唯一、校验与删除规则） |
| [api.md](./api.md) | 受影响的既有接口及行为变化 |
| [sql/README.md](./sql/README.md) | 表结构检查与约束说明（若有） |
