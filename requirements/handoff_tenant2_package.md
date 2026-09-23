# 交接文档：租户 2 限定权限范围（止血第 1 步）

> **已作废**：线上核查后方案调整，改用 `handoff_tenant_backfill_v2.md`。本文档里操作 admin2 / user 2000 的步骤是错的，admin2 是租户 1 的真实账号。

## 目标

租户 2（`tdk.toms.chat`）的管理员只能用「合同管理」和「用户管理（基础）」两类功能，碰不到日志、文件、配置、归档查询等未做租户隔离的数据。

## 根因

54 号迁移用手写 SQL 建的租户 2 有三个问题：

1. `package_id = 0`：yudao 把套餐 0 视为**系统租户**（`TenantServiceImpl.java:313`），不做套餐菜单限制；并且系统租户在后台「租户管理」里**不能编辑也不能删除**。
2. admin2 被分配了 `role_id = 1`，这是租户 1 的角色。`RoleDO` 按租户过滤，在租户 2 下查不到，admin2 登录后没有任何权限和菜单。
3. admin2 的 `dept_id = 100` 属于租户 1。

另外，有一个接口挂在「用户管理」的基础权限下，但操作的是没有租户隔离的表：`AutoResetPwdUserController`，用的是 `system:user:query` / `update`。

## 方案

不再用手写 SQL 修补租户 2，改为**软删除后在后台「租户管理 → 新增」重建**。
这是 yudao 标准流程：`TenantServiceImpl.createTenant` 会自动为新租户创建 `tenant_admin` 角色（类型为系统角色，权限等于套餐菜单）和管理员账号。后续修改套餐时，`updateTenantRoleMenu` 会自动同步这些权限。
菜单 ID 在各个环境不一样（`custom:contract` 相关菜单不是通过迁移建的），所以套餐菜单由人在页面上勾选，不写死在 SQL 里。

### 1. 新增迁移 `yudao-server/src/main/resources/db/migration/59_tenant2_reset.sql`

- **只在租户 2 没有任何合同时生效**：租户 2 目前只是测试租户，按理没有合同；如果有，全部语句都不生效，交给人工处理。
- 写法要求：不用 `SET @var` / `PREPARE`，每条语句都要能单独重复执行（幂等）。

```sql
-- 1. 软删除 54 号迁移手写创建的租户 2（只有套餐仍是 0，且没有合同时才生效）
UPDATE `system_tenant` SET `deleted` = 1, `update_time` = NOW(), `updater` = 'system'
WHERE `id` = 2 AND `package_id` = 0 AND `deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `custom_contract` WHERE `tenant_id` = 2);

-- 2. 软删除 admin2（依赖第 1 步已生效）
UPDATE `system_users` SET `deleted` = 1, `update_time` = NOW(), `updater` = 'system'
WHERE `id` = 2000 AND `username` = 'admin2' AND `tenant_id` = 2 AND `deleted` = 0
  AND EXISTS (SELECT 1 FROM `system_tenant` WHERE `id` = 2 AND `deleted` = 1);

-- 3. 软删除 admin2 的角色关联
UPDATE `system_user_role` SET `deleted` = 1, `update_time` = NOW(), `updater` = 'system'
WHERE `user_id` = 2000 AND `deleted` = 0
  AND EXISTS (SELECT 1 FROM `system_users` WHERE `id` = 2000 AND `username` = 'admin2' AND `deleted` = 1);

-- 4. 删除 admin2 的登录令牌
DELETE FROM `system_oauth2_access_token`  WHERE `user_id` = 2000 AND `tenant_id` = 2;
DELETE FROM `system_oauth2_refresh_token` WHERE `user_id` = 2000 AND `tenant_id` = 2;
```

文件头部用简短注释写明原因：系统租户（套餐 0）无法在后台修改，所以改为重建；只有租户 2 没有合同时才生效。

**不要改 54 号迁移**：它在线上可能已经执行过。

### 2. 修改 `yudao-module-mini/src/main/java/cn/iocoder/yudao/module/custom/controller/admin/pwd/AutoResetPwdUserController.java`

`custom_auto_reset_pwd_user` 表没有租户隔离，而 `adminUserService.getUser` 会按当前租户过滤，可以借它判断用户是否属于本租户：

- `listUsers()`：`getUser` 返回 null（说明是其他租户的用户）时，**跳过这一条**，不再返回只有 userId 的空记录。
- `removeUser()`：删除前先用 `getUser(userId)` 校验，返回 null 就抛出 `exception(USER_NOT_EXISTS)`，写法与 `addUser` 一致。
- `addUser()` 已经有这个校验，不用改。

### 3. 新增测试 `yudao-module-mini/src/test/java/cn/iocoder/yudao/module/custom/controller/admin/pwd/AutoResetPwdUserControllerTest.java`

用 Mockito mock 掉 `AutoResetPwdUserMapper` 和 `AdminUserService`：

| # | 场景 | 期望 |
|---|------|------|
| 1 | `selectAll` 返回用户 A、B，`getUser(A)` 有值，`getUser(B)` 返回 null | `listUsers` 只返回 A |
| 2 | `removeUser(B)`，`getUser(B)` 返回 null | 抛出 `ServiceException`（`USER_NOT_EXISTS`），`deleteByUserId` 不被调用 |
| 3 | `removeUser(A)`，`getUser(A)` 有值 | `deleteByUserId(A)` 被调用一次，返回 true |

## 后台人工操作（负责人在测试环境先做一遍，确认没问题后再在生产上做，开发不要做）

前提：已发布包含 59 号迁移的后端，并确认租户 2 已被软删除。

1. 超级管理员登录 `org.toms.chat` → 租户管理 → **租户套餐** → 新增「合同租户套餐」，勾选：
   - 合同管理整棵菜单：查询、创建、修改、删除、导出、补合约、统计、回收站等。
     **不要勾**：解密（`custom:contract:decypt`），除非租户 2 确实需要看明文身份证。
   - 系统管理 → 用户管理：查询、新增、修改、删除、重置密码、分配角色。
     **不要勾**：邀请码（`system:user:invite`）。它按 userId 查询，没有租户隔离。
   - **其余一律不勾**：安全、日志、文件、皮肤/图标/文案/小程序配置、版本说明、限速、收费策略、归档查询、角色/部门/菜单/字典/租户管理等。
2. 租户管理 → **租户列表** → 新增：
   - 名称：新平台
   - 绑定域名：`tdk.toms.chat`
   - 套餐：选上一步建的「合同租户套餐」
   - 管理员账号：换一个新用户名（例如 `tdk_admin`），不要复用 admin2，并设置强密码
   - 过期时间、账号数量：按需要填写
3. 用 `tdk.toms.chat` 登录新管理员账号，检查：菜单里只有合同管理和用户管理；合同列表为空；用户列表里只有本租户的用户。

## 不要做的事

- 不改 54、57 号迁移，不新建其他迁移。
- 不用 SQL 插入套餐、角色或角色菜单，这些都由后台页面生成。
- 新租户的角色编码绝不能用 `super_admin`：超级管理员会跳过全部权限校验。
- 不改 `TenantSecurityWebFilter`、小程序、money-ui。
- 不连接、不操作任何线上环境。
- 按 JDK8 写；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-module-mini -am test -Dtest=AutoResetPwdUserControllerTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-server -am test -Dtest=SqlMigrationRunnerSplitTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
grep -nE "SET @|PREPARE" yudao-server/src/main/resources/db/migration/59_tenant2_reset.sql   # 期望无输出
```

## 测试环境回归（负责人在后台操作完成后执行）

1. 新租户管理员用 token 调这些接口，应返回 403 或无权限：
   - `/custom/security/archive/query`
   - 审计日志、文件列表、皮肤/文案配置的修改接口
   - `/system/user/invite/*`
2. 新租户管理员的 token 带上 `tenant-id: 1` 请求头，应返回 403。
3. 租户 1 的超级管理员功能不受影响；老版本小程序全流程正常。

## 已知遗留（不在本次范围）

- `WechatLoginController.send`（权限是 `custom:contract:create`）可以向任意 openid 发送模板消息，租户 2 有合同创建权限就能调用。后续应改为只能发给本租户的用户。
- 手写 SQL（`CustomDefineMapper` 的统计、首页等）在租户 2 下是否能被 JSqlParser 正确改写，需要在测试环境用租户 2 账号实际打开首页和统计页面验证。
