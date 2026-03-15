# 真实姓名重名支持 - 接口行为说明

本需求**不新增接口**，仅对以下既有接口或内部行为做调整。请求/响应结构保持不变，下表仅说明「行为变化」。

## 1. 受影响的接口

### 1.1 注册

- **接口**：`POST /custom/contract/dashboard/register`
- **请求体**：沿用现有（如 username、realname、password、idNo、mobile 等）。
- **行为变化**：  
  - **原**：realname 与已有用户重复时，报错「真实姓名已经存在」。  
  - **现**：不再校验 realname 唯一；仅校验 username、idNo、mobile（若填）唯一。同姓名、不同身份证可成功注册。

### 1.2 校验用户信息

- **接口**：`POST /custom/contract/dashboard/checkUserInfo`
- **请求体**：沿用现有（如 realname、idNo）。
- **行为变化**：  
  - **原**：按 realname 查用户，取第一条，再比较 idNo 是否一致。  
  - **现**：按 realname 查用户列表，在列表中查找 idNo 与请求 idNo 一致的一条；存在则返回 true，否则 false。支持同名多用户时仍能正确匹配「当前人」。

### 1.3 24 小时内合同删除

- **接口**：`DELETE /custom/contract/dashboard/delete24HourContract`（或项目实际路径）
- **请求**：无 body，依赖当前登录用户。
- **行为变化**：  
  - **原**：按当前用户的 realname 软删 `custom_contract` 中 `indebted_name` 或 `creditor_name` 等于该 realname 且 24 小时内更新的记录。  
  - **现**：按当前用户的 **idNo** 软删 `indebted_id` 或 `creditor_id` 等于该 idNo 且 24 小时内更新的记录。仅影响当前用户本人，不影响同名其他用户。

## 2. 统一响应格式

与项目现有约定一致，无新增错误码；仅原有「真实姓名已经存在」在注册场景下不再出现。

- 成功：`{ code: 0 | 200, msg: '', data: ... }`
- 失败：`{ code: 非0/非200, msg: '错误说明', data: null }`
