# 租户 2 上线前检查清单（2026-09-23 复核）

## 结论

租户 2 只要**用套餐把菜单限死在合同管理 + 基础用户管理**，就可以安全上线，不需要再改代码。
未做租户隔离的表全部只能通过被排除的菜单访问，租户 2 的管理员够不到。

## 菜单权限与表的对应关系（按权限前缀盘点）

### 允许给租户 2 的

| 权限前缀 | 涉及接口 | 数据隔离情况 |
|---|---|---|
| `custom:contract` | ContractController、CustomDefineController、ContractFileController、ContractRecycleController、WechatLoginController | `custom_contract` 已继承 TenantBaseDO，查询自动按租户过滤；回收站接口显式带 tenantId |
| `system:user` | 用户增删改查、重置密码 | `system_users` 已是 TenantBaseDO，自动过滤；AutoResetPwdUserController 已加租户校验 |

两个例外，**不要勾**：

- `custom:contract:decypt`（身份证解密）——除非租户 2 确有需要
- `system:user:invite`（邀请码）——InviteAdminController 按 userId 查 `custom_invite_code`，该表无租户隔离

### 必须排除的

| 权限前缀 | 接口 | 排除原因 |
|---|---|---|
| `custom:security` | ArchiveQueryController、SecurityPwdResetLogController | **ClickHouse 归档查询完全没有租户过滤**，能查到所有租户的归档数据 |
| `custom:audit-log` | AuditLogController | `custom_audit_log` 无租户字段 |
| `custom:skin` / `custom:text` / `custom:icon-set` / `custom:miniprogram-config` / `custom:version-changelog` | 各配置管理接口 | 配置类表全平台共用，**改了会立刻影响租户 1 的线上小程序** |
| `custom:speed-control` | SpeedControlController | 同上，全局限速配置 |
| `fee:strategy` | StrategyController、CustomStragegyController | 收费策略全局共用 |
| `system:data-access` / `system:data-key` | DataAccessReverseController、DataKeyController | 数据访问配置与密钥，全局 |
| 角色、部门、菜单、字典、租户管理等 | yudao 自带 | 非本阶段范围 |

## 复核过的疑点（结论：不是问题）

- **微信模板消息 `WechatLoginController.send`**：收件人是用身份证号经 `adminUserService.getUserByIdNo` 查出来的，
  该查询走 MyBatis、会被自动追加租户条件，所以只能发给本租户用户。之前记为遗留风险，现撤销。
- **信用查询 `creditSearch`**：查的是 `custom_contract`，自动按租户过滤。
- **小程序侧接口**（无 `@PreAuthorize` 的那些）：都是查当前登录用户自己的数据，或落在已隔离的表上。

## 仍然存在、但与租户 2 无关的既有问题

- 文件下载 `/admin-api/infra/file/{configId}/get/**` 是免登录的，知道 URL 即可下载。这是既有设计，和租户无关。

## 上线步骤

1. 超级管理员在「租户套餐」新增「合同租户套餐」，按上表勾选。
2. 「租户列表」新增租户：名称、**新域名**（不能用 `tdk.toms.chat`，那是后台在用的）、选上面的套餐、
   管理员用新用户名并设强密码。框架会自动建好该租户的 `tenant_admin` 角色。
3. 用新域名登录新管理员，确认：只有两类菜单；合同列表为空；用户列表只有本租户用户。
4. 用新管理员的 token 加 `tenant-id: 1` 请求头访问，应返回 403。
5. 再用它访问被排除的接口（如 `/custom/security/archive/query`、`/system/user/invite/*`），应返回无权限。

## 下一阶段（不在本次范围）

日志、文件、反馈、邀请码等表加 `tenant_id`；ClickHouse 归档查询补租户条件；配置类表租户化。
在这些做完之前，租户 2 的套餐不要放开上表中「必须排除」的菜单。
