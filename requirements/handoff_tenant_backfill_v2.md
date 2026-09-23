# 交接文档：开启租户前的数据回填 + 租户 2 重建准备（v2）

> 本文档取代 `handoff_tenant2_package.md`。`handoff_tenant_enable_compat.md` 已完成（默认租户兜底、FaceAuthExpireJob），不要回退。

## 线上只读核查结论（2026-09-22）

- `custom_contract.tenant_id` 在线上的默认值是 **0**，8636 条合同**全部**是 `tenant_id=0`。54 号迁移里"加列、默认值 1"的语句，因为列已存在被跳过了（执行器把 1060 视为已存在）。
- `custom_contract_daily_stats`：886 条，全部是 `tenant_id=0`。
- 当前有效的令牌全部是 `tenant_id=0`：访问令牌 209 个、刷新令牌 875 个。
- 57 号迁移已于 2026-08-26 在线上执行过。执行器按**文件名**判断是否执行过，所以 57 不会再跑。
- `system_users`、`system_role`、`system_role_menu`、`system_dept`、`system_social_client` 目前没有 `tenant_id=0` 的行。
- 租户 2（`tdk.toms.chat`，`package_id=0`）存在，名下没有用户，也没有合同。
- `admin2`（id=1768547539312）是**租户 1 的真实账号**，不是 54 号迁移创建的。**不许动。**
- `SqlMigrationRunner` 用 `@PostConstruct` 执行，早于 Web 服务开始接收请求。

## 改动（只改以下文件）

### 1. 新增 `yudao-server/src/main/resources/db/migration/59_tenant_enable_backfill.sql`

要求：
- 每条语句都要能单独重复执行（幂等）；不用 `SET @var` / `PREPARE`。
- 文件头部用简短注释写明背景：54 号加列被跳过，线上合同和令牌都是租户 0；57 已执行过、不会重跑，所以另起此文件。

```sql
-- 1. 合同表默认租户改为 1（只改元数据，不动数据；重复执行无副作用）
ALTER TABLE `custom_contract` ALTER COLUMN `tenant_id` SET DEFAULT 1;

-- 2. 业务数据回填到租户 1
UPDATE `custom_contract`             SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `custom_contract_daily_stats` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 3. 令牌原地修正，老用户免重登（发布后需清 Redis oauth2_access_token:*）
UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 4. 系统表兜底（线上当前为 0 行，57 执行后到本次发布之间若有新增会被带上）
UPDATE `system_users`         SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_dept`          SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_role`          SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_role_menu`     SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_social_client` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 5. 软删除 54 号手写创建的租户 2：套餐 0 会被当成系统租户，后台既不能编辑也不能删除，
--    所以改为软删除后在后台用标准流程重建。只有名下没有用户和合同时才生效。
UPDATE `system_tenant` SET `deleted` = 1, `update_time` = NOW(), `updater` = 'system'
WHERE `id` = 2 AND `package_id` = 0 AND `deleted` = 0
  AND NOT EXISTS (SELECT 1 FROM `system_users`    WHERE `tenant_id` = 2)
  AND NOT EXISTS (SELECT 1 FROM `custom_contract` WHERE `tenant_id` = 2);
```

不要出现任何针对 `admin2`、`system_users.id=2000`、`system_user_role` 的语句。

### 2. `yudao-module-mini/src/main/java/cn/iocoder/yudao/module/custom/controller/admin/pwd/AutoResetPwdUserController.java`

`custom_auto_reset_pwd_user` 这张表没有租户隔离；而 `adminUserService.getUser` 会按当前租户过滤，可以借它判断用户属不属于本租户。

- `listUsers()`：`getUser` 返回 null（说明是其他租户的用户）时，**跳过这一条**。
- `removeUser()`：删除前先用 `getUser(userId)` 校验，返回 null 就 `throw exception(USER_NOT_EXISTS)`，写法与 `addUser` 一致。
- `addUser()` 不用改。

### 3. 新增 `yudao-module-mini/src/test/java/cn/iocoder/yudao/module/custom/controller/admin/pwd/AutoResetPwdUserControllerTest.java`

用 Mockito mock 掉 `AutoResetPwdUserMapper` 和 `AdminUserService`：

| # | 场景 | 期望 |
|---|------|------|
| 1 | `selectAll` 返回 A、B；`getUser(A)` 有值，`getUser(B)` 返回 null | `listUsers` 只返回 A |
| 2 | `removeUser(B)`，`getUser(B)` 返回 null | 抛出 `ServiceException`，`deleteByUserId` 不被调用 |
| 3 | `removeUser(A)`，`getUser(A)` 有值 | `deleteByUserId(A)` 被调用一次，返回 true |

### 4. 新增 `yudao-server/src/test/java/cn/iocoder/yudao/server/framework/migration/TenantBackfillMigrationTest.java`

读取 classpath 下的 `db/migration/59_tenant_enable_backfill.sql`，用 `SqlMigrationRunner` 现有的拆分方法拆成语句（参照 `SqlMigrationRunnerSplitTest` 的调用方式），断言：

- 共 **11** 条语句；
- 包含 `ALTER TABLE \`custom_contract\` ALTER COLUMN \`tenant_id\` SET DEFAULT 1`；
- 有 `custom_contract` 和 `custom_contract_daily_stats` 的 `tenant_id = 1 WHERE tenant_id = 0` 回填语句；
- 全文不含 `admin2`、`2000`、`system_user_role`、`SET @`、`PREPARE`，也不含 `DELETE`。

## 不要做的事

- 不改 54、57、58 号迁移。
- 不修改 `handoff_tenant_enable_compat.md` 里已完成的改动：`TenantSecurityWebFilter`、`TenantProperties`、`application.yaml`、`FaceAuthExpireJob`。
- 不用 SQL 插入套餐、角色或角色菜单。
- 不动 `admin2`，不动演示租户 121、122。
- 不改小程序、money-ui。
- 不连接、不操作任何线上环境。
- 按 JDK8 写；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-server -am test -Dtest='TenantBackfillMigrationTest,SqlMigrationRunnerSplitTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am test -Dtest=AutoResetPwdUserControllerTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-framework/yudao-spring-boot-starter-biz-tenant -am test -Dtest=TenantSecurityWebFilterTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
```

## 发布与后台操作（负责人执行，开发不要做）

1. 发布后端时，把 `application-prod.yaml` 的 `tenant.enable` 设为 true。启动后确认 `custom_sql_migration` 里 `59_tenant_enable_backfill.sql` 是 SUCCESS，并确认 `custom_contract` 已经没有 `tenant_id=0` 的行。
2. 清掉 Redis 里的 `oauth2_access_token:*`。
3. 用老版本小程序回归：启动、注册、登录、旧 token 免重登、查看历史合同、新建合同、人脸核身回调、公众号回调。后台首页统计要有历史数据。
4. 超级管理员在「租户套餐」新增「合同租户套餐」：
   - 勾选合同管理整棵菜单，但**不勾解密**（`custom:contract:decypt`）；
   - 勾选系统管理 → 用户管理的基础按钮，但**不勾邀请码**（`system:user:invite`）；
   - 其余一律不勾。
5. 在「租户列表」新增租户：名称「新平台」，绑定域名 `tdk.toms.chat`，套餐选上一步建的，管理员用新用户名（例如 `tdk_admin`）并设置强密码。
6. 用 `tdk.toms.chat` 登录新管理员，确认：只有两类菜单；合同为空；带 `tenant-id: 1` 请求头访问会返回 403。

## 评审退回（第 1 轮）

线上 `custom_contract.update_time` 带 `ON UPDATE CURRENT_TIMESTAMP`。合同列表默认按 `update_time DESC` 排序（`ContractMapper.xml:136`），直接 UPDATE 会把 8636 条合同的修改时间全部刷成发布时刻，列表顺序会被打乱。

修改要求（只改这两个文件）：
1. `59_tenant_enable_backfill.sql`：合同回填语句改为
   ```sql
   UPDATE `custom_contract` SET `tenant_id` = 1, `update_time` = `update_time` WHERE `tenant_id` = 0;
   ```
   在上方加一行注释：显式赋值 update_time，避免 ON UPDATE 把修改时间刷成当前时间、打乱列表排序。其余语句不变。
2. `TenantBackfillMigrationTest.java`：合同回填的断言改为匹配新语句，并额外断言该语句包含 `` `update_time` = `update_time` ``。语句数仍为 11。
