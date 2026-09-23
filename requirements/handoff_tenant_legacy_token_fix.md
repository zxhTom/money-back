# 交接文档：兼容租户为空的老令牌（线上事故修复）

## 事故

2026-09-22 22:39 发布并开启租户后，线上老版本小程序大量报「请求的租户标识未传递，请进行排查」（`TenantSecurityWebFilter:96`），已关闭租户开关回滚。

## 根因

- Redis 里缓存的 205 个访问令牌 `oauth2_access_token:*`，`tenantId` 全部是 **null**。这些令牌是租户关闭期间生成的，当时 Java 对象里根本没有设置租户。
- `TenantSecurityWebFilter` 的逻辑是：有登录用户且请求没带头时，用 `user.getTenantId()` 作为租户，得到 null；而默认租户兜底只在 `user == null` 时才生效，所以最终走进了 400 分支。
- 另外，租户关闭期间新生成的令牌，数据库里 `tenant_id` 是列默认值 **0**。Redis 缓存过期后会回库重新加载，`LoginUser.tenantId=0`，`validTenant(0)` 会失败。
- 新版小程序会带 `tenant-id: 1` 请求头。碰到 tenantId 为 null 的令牌时，`Objects.equals(null, 1)` 为 false，会被误判为越权，返回 403。

## 改动（只改以下文件）

### 1. `yudao-framework/yudao-spring-boot-starter-biz-tenant/src/main/java/cn/iocoder/yudao/framework/tenant/core/security/TenantSecurityWebFilter.java`

在 `doFilterInternal` 的「1. 登陆的用户」块之前，先算出用户的有效租户：

```java
Long defaultTenantId = tenantProperties.getDefaultTenantId();
Long userTenantId = user != null ? user.getTenantId() : null;
if (user != null && defaultTenantId != null && (userTenantId == null || userTenantId == 0L)) {
    userTenantId = defaultTenantId;
}
```

然后在「1. 登陆的用户」块里，把 `user.getTenantId()` **全部**替换为 `userTenantId`，包括：取租户、越权比对、错误日志。其余逻辑不变。

效果：

| 场景 | 结果 |
|------|------|
| 老令牌（null / 0）+ 不带头 | 用租户 1 |
| 老令牌 + 带头 1 | 通过 |
| 老令牌 + 带头 2 | 403 |
| 租户 2 的正常令牌 | 完全按原逻辑 |
| `defaultTenantId` 未配置 | 完全是框架原行为 |

原有的「兜底默认租户编号」块保留不动，它处理的是没有登录用户的场景。

### 2. `yudao-framework/yudao-spring-boot-starter-biz-tenant/src/test/java/cn/iocoder/yudao/framework/tenant/core/security/TenantSecurityWebFilterTest.java`

保留现有 6 个用例，新增：

| # | 场景 | 期望 |
|---|------|------|
| 7 | defaultTenantId=1，无头，登录用户 tenantId=null | chain 被调用，执行时租户=1，`validTenant(1)` 被调用 |
| 8 | defaultTenantId=1，头=1，登录用户 tenantId=null | chain 被调用，没有 403 |
| 9 | defaultTenantId=1，头=2，登录用户 tenantId=null | 返回 403「您无权访问该租户的数据」，chain 不调用 |
| 10 | defaultTenantId=1，无头，登录用户 tenantId=0 | chain 被调用，执行时租户=1 |
| 11 | defaultTenantId=null，无头，登录用户 tenantId=null，普通 URL | 返回 400「请求的租户标识未传递」（原行为） |

### 3. 新增 `yudao-server/src/main/resources/db/migration/60_tenant_token_rebackfill.sql`

59 号已经在线上执行过。回滚期间（租户关闭）又会产生 `tenant_id=0` 的令牌等数据，再兜底一次。要求幂等，不用 `SET @var` / `PREPARE`，文件头部加一行简短注释说明原因：

```sql
UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `custom_contract`             SET `tenant_id` = 1, `update_time` = `update_time` WHERE `tenant_id` = 0;
UPDATE `custom_contract_daily_stats` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_users`                SET `tenant_id` = 1 WHERE `tenant_id` = 0;
```

### 4. `yudao-server/src/test/java/cn/iocoder/yudao/server/framework/migration/TenantBackfillMigrationTest.java`

新增一个测试方法，校验 60 号文件：拆分后共 5 条语句；合同那条包含 `` `update_time` = `update_time` ``；不含 `DELETE`、`SET @`、`PREPARE`、`admin2`。原有 59 号的测试保持不变。

## 不要做的事

- 不改 54～59 号迁移。
- 不改 `TenantContextWebFilter`、`TenantProperties`、`application.yaml`。
- 不改小程序、money-ui。
- 不连接、不操作任何线上环境。
- 按 JDK8 写；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-framework/yudao-spring-boot-starter-biz-tenant -am test -Dtest=TenantSecurityWebFilterTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-server -am test -Dtest='TenantBackfillMigrationTest,SqlMigrationRunnerSplitTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
```
