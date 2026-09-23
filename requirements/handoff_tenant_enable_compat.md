# 交接文档：开启多租户，同时兼容线上老版本小程序

## 目标

生产开启 `yudao.tenant.enable=true` 后，**线上老版本小程序（请求不带 `tenant-id` 头）必须完全照常使用**：启动、注册、登录、已登录用户免重登、合同全流程、人脸核身回调、公众号回调。小程序本次不发版。

## 根因

1. `TenantSecurityWebFilter`：请求没带 `tenant-id`、也没有登录用户（未登录接口、第三方回调）时，直接返回 400「请求的租户标识未传递」。老小程序和百度核身回调、公众号回调都不带这个头。
2. `57_tenant_backfill.sql` 直接 DELETE `tenant_id=0` 的令牌，老用户全部被踢下线。
3. `FaceAuthExpireJob`（`@Scheduled`）没有租户上下文，查 `system_users` 会在 `getRequiredTenantId()` 抛异常。

## 改动（只改以下文件）

### 1. `yudao-framework/yudao-spring-boot-starter-biz-tenant/src/main/java/cn/iocoder/yudao/framework/tenant/config/TenantProperties.java`
新增字段 `private Long defaultTenantId;`（无默认值，null 表示关闭兜底，保持框架原行为）。

### 2. `yudao-framework/yudao-spring-boot-starter-biz-tenant/src/main/java/cn/iocoder/yudao/framework/tenant/core/security/TenantSecurityWebFilter.java`
在 `doFilterInternal` 里，**登录用户判断之后**、「如果非允许忽略租户的 URL」判断之前加兜底：

```
if (tenantId == null && user == null && tenantProperties.getDefaultTenantId() != null) {
    tenantId = tenantProperties.getDefaultTenantId();
    TenantContextHolder.setTenantId(tenantId);
}
```

要点：
- 必须放在这个 filter 里、放在登录用户判断之后，**不要改 `TenantContextWebFilter`**。原因：`TenantContextWebFilter` 在认证之前执行，若在那里兜底为 1，租户 2 的用户不带头请求时会被当作越权 403。
- 有登录用户时逻辑不变：没带头就用 token 里的租户；带了头且不一致仍然 403。
- 兜底后照常走 `validTenant(tenantId)` 校验。
- 忽略 URL（`@TenantIgnore` / ignore-urls）因为兜底后 tenantId 不再为 null，会带上租户 1 上下文而不是 ignore。这对 `WxaSecCheckCallbackController` 没影响，但为保持原语义：**兜底条件里再加 `&& !isIgnoreUrl(request)`**，忽略 URL 维持原来的 `setIgnore(true)`。

### 3. `yudao-server/src/main/resources/application.yaml`
`yudao.tenant` 下加 `default-tenant-id: 1`，并加一行短注释：老版本小程序不带 tenant-id 头，兜底到默认租户。

### 4. `yudao-server/src/main/resources/db/migration/57_tenant_backfill.sql`（当前未提交的文件）
- 删除两条 `DELETE FROM system_oauth2_access_token / system_oauth2_refresh_token`，改为：
  ```
  UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
  UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
  ```
- 更新文件里对应的注释：说明改为原地修正以免老用户被踢下线，Redis 里的 `oauth2_access_token:*` 缓存需要在发布时手动清除（见下方发布步骤），清除后后端会回库读到 tenant_id=1。
- 其余 UPDATE 保持不变。不要用 `SET @var` / `PREPARE`。

### 5. `yudao-module-mini/src/main/java/cn/iocoder/yudao/module/custom/job/face/FaceAuthExpireJob.java`
`run()` 里的查询/更新包进 `TenantUtils.executeIgnore(...)`，写法参照 `ContractDailyStatsJob`。**不要**用 `@TenantIgnore` 注解（同类调用和 `@Scheduled` 场景下不可靠）。

### 6. 新增测试 `yudao-framework/yudao-spring-boot-starter-biz-tenant/src/test/java/cn/iocoder/yudao/framework/tenant/core/security/TenantSecurityWebFilterTest.java`
用 Mockito + `MockHttpServletRequest/Response`，mock `TenantFrameworkService`、`GlobalExceptionHandler`，`SecurityFrameworkUtils` 的登录用户通过设置 Spring Security 上下文或 mockStatic 实现。每个用例前后 `TenantContextHolder.clear()`。在 chain 里捕获执行时的 `TenantContextHolder.getTenantId()` / `isIgnore()`。

## 测试用例

| # | 场景 | 期望 |
|---|------|------|
| 1 | defaultTenantId=1，无头，无登录用户，普通 URL | chain 被调用，执行时租户=1，`validTenant(1)` 被调用 |
| 2 | defaultTenantId=1，无头，登录用户租户=2 | chain 被调用，租户=2，无 403 |
| 3 | defaultTenantId=1，头=1，登录用户租户=2 | 返回 403「您无权访问该租户的数据」，chain 不调用 |
| 4 | defaultTenantId=null，无头，无登录用户，普通 URL | 返回 400「请求的租户标识未传递」（原行为不变） |
| 5 | defaultTenantId=1，无头，无登录用户，忽略 URL | chain 被调用，`isIgnore()=true`，租户为 null |
| 6 | defaultTenantId=1，头=2，无登录用户 | 租户=2（显式头优先于兜底） |

## 不要做的事

- 不改小程序、不改 money-ui。
- 不改 `TenantContextWebFilter`、`TenantDatabaseInterceptor`。
- 不改 54 号迁移（线上可能已执行过）；不新建其它迁移文件。
- 不处理租户 2 / admin2 的角色和套餐问题（另行处理）。
- 不连接、不操作任何线上环境（数据库、Redis、服务器）。
- money-back 按 JDK8 写：禁用 `String.isBlank()`、`var`、`List.of()` 等 JDK9+ API。
- 注释从简，不写多行 Javadoc。

## 验收命令

```
cd money-back
mvn -q -pl yudao-framework/yudao-spring-boot-starter-biz-tenant -am test -Dtest=TenantSecurityWebFilterTest -DfailIfNoTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
mvn -q -pl yudao-server -am test -Dtest=SqlMigrationRunnerSplitTest -DfailIfNoTests=false
grep -n "DELETE" yudao-server/src/main/resources/db/migration/57_tenant_backfill.sql   # 期望无输出
```

## 发布步骤（由负责人执行，开发不要做）

1. 发布后端（带 57 号迁移），确认迁移执行成功。
2. 清除 Redis 访问令牌缓存：`oauth2_access_token:*`。
3. 用老版本小程序回归：启动、注册、登录、旧 token 免重登、建/查合同、人脸核身回调、公众号回调。
4. 用租户 2 token + `tenant-id: 1` 请求，确认 403。
