# 合同安全改造实施记录 (SEC-04 / SEC-05)

## 1. 变更范围与核心设计决定

- **SEC-04（信用查询授权收紧）**：
  - 彻底移除 `existsContractRelation` 作为信用查询的授权条件；
  - 仅允许持有专门权限 `custom:contract:credit-query` 的用户，或已实名认证的本人查询自身信用；
  - 杜绝任意用户通过伪造单方合同或合同关系刺探第三方信用与明细。

- **SEC-05（合同汇总作用域与防注入/防大字段膨胀）**：
  - 引入统一作用域守卫 `ContractQueryScope`，将 `/page` 与 `/totalInfos` 的可见范围强制收敛一致；
  - 无筛选时，普通用户作用域强制限定为 `creator = 当前登录用户`，仅启用的 `super_admin` 或 `contract-manager` 具备租户级权限（禁用角色严格过滤）；
  - 信用查询按身份证筛选时，必须通过收紧后的 `CreditQueryAccessChecker`，否则抛出 `AccessDeniedException`；
  - `/totalInfos` 统一入参身份证密文归一化（`normalizeRequestToCipher`），避免明密文不一致；
  - `totalInfos` 数据库求和使用 `selectSumSalary`（SQL 显式包含 `deleted = 0`），排除任何 `ORDER BY`，避免内存加载全量附件大字段或 MySQL `ONLY_FULL_GROUP_BY` 报错。

## 2. 代码与测试变更

- **修改文件**:
  - `yudao-module-mini/.../custom/service/custom/CreditQueryAccessChecker.java`
  - `yudao-module-mini/.../custom/dal/mysql/contract/ContractMapper.java`
  - `yudao-module-mini/.../custom/service/contract/ContractServiceImpl.java`
  - `yudao-module-mini/.../custom/service/custom/CustomDefineServiceImpl.java`
- **新增文件**:
  - `yudao-module-mini/.../custom/service/contract/ContractQueryScope.java`
  - `yudao-module-mini/.../custom/service/contract/ContractQueryScopeTest.java`
  - `yudao-module-mini/.../custom/service/contract/ContractTotalInfosSecurityTest.java`
- **关联测试修复**:
  - `CreditQueryAccessCheckerTest.java`: 将旧的“有合同关系即可查”预期更新为“无专门权限拒绝查他人”，保留所有正反例断言。
  - `PrivacyHardeningTest.java`: 修复 Mockito varargs `hasAnyRoles` 与 SpEL 多参匹配夹具，15 项隐私加固用例全部通过。

## 3. 部署后建议与待办动作

1. 检查各租户管理员与业务用户是否已分配专门权限 `custom:contract:credit-query`；未分配专门权限的业务员将仅能查看自己创建的合同及查询本人实名信用。
2. 线上数据库确保 `custom_contract` 的 `deleted` 字段建立索引（或复合索引 `tenant_id, deleted, creator`），以保证 `selectSumSalary` 的高性能执行。
