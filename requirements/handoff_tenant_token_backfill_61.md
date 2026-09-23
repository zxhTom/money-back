# 交接文档：61 号迁移，回填租户关闭期间产生的 0 值令牌

## 背景

2026-09-23 白天线上临时把 `yudao.tenant.enable` 关掉过一段时间（09:50 左右到 20:06），期间新生成的登录令牌 `tenant_id` 写成了列默认值 0：`system_oauth2_access_token` 约 32 行、`system_oauth2_refresh_token` 约 32 行。

目前不影响使用：`TenantSecurityWebFilter` 会把租户为 null 或 0 的令牌按 `default-tenant-id: 1` 处理。这次只是把数据补正。

60 号迁移已在线上执行过，执行器按文件名去重，不会重跑，所以另起 61 号。

## 改动（只改以下文件）

### 1. 新增 `yudao-server/src/main/resources/db/migration/61_tenant_token_backfill2.sql`

文件头部写一行简短注释说明背景（租户临时关闭期间产生的 0 值令牌）。要求幂等，不用 `SET @var` / `PREPARE`：

```sql
UPDATE `system_oauth2_access_token`  SET `tenant_id` = 1 WHERE `tenant_id` = 0;
UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
```

### 2. `yudao-server/src/test/java/cn/iocoder/yudao/server/framework/migration/TenantBackfillMigrationTest.java`

新增一个测试方法校验 61 号文件：拆分后共 2 条语句；两条都是 `tenant_id = 1 WHERE tenant_id = 0` 的 UPDATE；全文不含 `DELETE`、`SET @`、`PREPARE`。原有 59、60 的测试方法保持不变。

## 不要做的事

- 不改 54～60 号迁移，不改任何 Java 主代码。
- 不改小程序、money-ui。
- 不连接、不操作任何线上环境。
- 按 JDK8 写；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-server -am test -Dtest='TenantBackfillMigrationTest,SqlMigrationRunnerSplitTest' -Dsurefire.failIfNoSpecifiedTests=false
grep -nE "SET @|PREPARE|DELETE" yudao-server/src/main/resources/db/migration/61_tenant_token_backfill2.sql   # 期望无输出
```
