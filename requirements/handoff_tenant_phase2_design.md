# 设计：租户隔离第二阶段（未隔离表的处理）

## 一条决定性约束

`SecurityDetectFilter` 注册顺序是 **-300**，`ApiAccessLogFilter` 是 **-103**，
而租户上下文来自 `TenantContextWebFilter`(-104，只读请求头) 和 `TenantSecurityWebFilter`(-99，兜底默认租户)。
也就是说：**在过滤器阶段写入的表，拿不到租户上下文**（尤其是不带 `tenant-id` 头的老版本小程序请求，
要到 -99 才会被兜底成租户 1）。这些表如果改成 `TenantBaseDO`，插入时会在 `getRequiredTenantId()` 抛异常。

结论：按"谁写的"把表分成三类，区别对待。

## 分类

### A 类：平台运维数据，**保持不隔离**（不改）

| 表 | 写入位置 |
|---|---|
| `infra_api_access_log`、`infra_api_error_log` | ApiAccessLogFilter(-103) |
| `custom_security_alert`、`custom_ip_access_log`、`custom_ip_blacklist*`、`custom_ua_whitelist` | SecurityDetectFilter(-300) 及其异步告警 |
| `system_login_log`、`system_operate_log`、`infra_job_log` | 框架层，部分在无上下文的任务线程 |

这些是平台运营者（总平台）的数据，不是租户业务数据。**做法：永远不给租户这些菜单**（套餐里已排除）。
真要给租户看，应该另做"按租户过滤的视图接口"，而不是把表改成租户表。

### B 类：业务数据，**改造为租户表**（本阶段目标）

| 表 | 行数 | 写入位置 | 备注 |
|---|---|---|---|
| `custom_audit_log` | 8.4 万 | AuditLogAspect（控制器内，有上下文） | 清理 Job 用 `@TenantIgnore`，不受影响 |
| `custom_invite_code` | 56 | 注册/邀请接口（请求内） | 现在 InviteAdminController 按 userId 查，可跨租户 |
| `custom_invite_register_log` | 1 | 同上 | |
| `system_feedback` | 10 | 反馈提交接口（请求内） | |
| `custom_user_ip_history` | 2655 | 登录流程（请求内） | 需确认不是在过滤器里写 |
| `custom_password_history` | 2596 | 改密流程（请求内） | |

每张表的改造步骤：
1. 迁移加列 `tenant_id bigint NOT NULL DEFAULT 1`（存量自动归总平台）；
2. 对应 DO 从 `BaseDO` 改为 `TenantBaseDO`；
3. 核对所有写入路径都在请求线程内；定时任务/异步写入要用 `TenantUtils.executeIgnore` 或显式指定租户；
4. 核对手写 SQL（XML / `@Select`）——JSqlParser 会自动改写，但要确认能解析；
5. 加单元测试：有上下文时按租户过滤，无上下文时行为符合预期。

### C 类：配置数据，**需要产品决策**（不在本阶段）

皮肤、文案、图标、小程序配置、版本说明、限速配置、收费策略、合同模板、时间窗口。

两种可能的产品形态，取决于你的业务：
- **共用**：所有租户用同一套配置，只有总平台能改（=现状，只要不给租户这些菜单即可，零改造）；
- **各租户独立**：每张表加 `tenant_id`，每个租户一套配置，新建租户时要初始化默认配置。

这一步改造量最大（还涉及小程序按租户取配置），**建议等真的有第二个业务方在用时再做**。

## D：ClickHouse 归档查询

`ArchiveQueryController.query` 直接拼 SQL 查 `arc_*` 表，**完全没有租户条件**。
归档的源表都属于 A 类（平台运维数据），所以正确做法不是加租户列，而是：
**这个接口只允许总平台访问**，其他租户一律拒绝。

## 实施顺序

1. **第 1 步（本次，改动小、见效快）**：
   - 归档查询接口加总平台校验；
   - 邀请码接口加租户校验（与 AutoResetPwdUserController 同款写法）。
2. **第 2 步**：B 类六张表的租户化改造（一张一张来，每张一个迁移 + 一个 DO + 测试）。
3. **第 3 步**：C 类等产品决策。

---

# 交接：第 1 步实现

只改以下两个文件，外加各自的测试。

## 1. `yudao-module-mini/.../controller/admin/archive/ArchiveQueryController.java`

归档表都是平台运维数据（接口日志、登录日志、安全告警等），ClickHouse 里没有租户列，
查询也没有任何租户条件。加一道校验：**只有默认租户（总平台）能用这个控制器的全部接口**。

- 注入 `TenantProperties`（`cn.iocoder.yudao.framework.tenant.config.TenantProperties`）。
- 新增私有方法 `checkDefaultTenantOnly()`：
  - `Long current = TenantContextHolder.getRequiredTenantId();`
  - `Long allowed = tenantProperties.getDefaultTenantId() != null ? tenantProperties.getDefaultTenantId() : 1L;`
  - 不相等则 `throw exception0(GlobalErrorCodeConstants.FORBIDDEN.getCode(), "归档数据仅总平台可查询")`
    （`cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0`）。
- 在 `tables()`、`runNow()`、`query(...)` 三个方法体的**第一行**调用它。
- 其余逻辑不动。

## 2. `yudao-module-mini/.../controller/admin/invite/InviteAdminController.java`

`custom_invite_code` / `custom_invite_register_log` 没有租户列，现在按 userId / codeId 直接查，
其他租户的管理员能查到别人的邀请数据。借 `adminUserService.getUser(...)` 会按租户过滤这一点做校验：

- 注入 `AdminUserService`（`cn.iocoder.yudao.module.system.service.user.AdminUserService`）。
- `getCodes(userId)`：先 `adminUserService.getUser(userId)`，为 null 则
  `throw exception(USER_NOT_EXISTS)`（`cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS`）。
- `getRegistrations(codeId)`：先取邀请码（`inviteCodeService` 里已有按 id 取的方法就用，
  没有就用 `listCodesByInviter` 之外的最小改动方式，**不要改 service 接口**；
  如果确实没有按 id 查的方法，则改为先查出 `codeId` 对应的记录再取其 inviter 用户 id），
  拿到邀请人 userId 后做同样的校验；邀请码不存在也返回 `USER_NOT_EXISTS` 之外更合适的错误就用现有错误码。
- 写法与 `AutoResetPwdUserController` 里已有的租户校验保持一致。

## 测试

新增 `ArchiveQueryControllerTest`：
| # | 场景 | 期望 |
|---|---|---|
| 1 | defaultTenantId=1，上下文租户=1，调 `tables()` | 正常返回 |
| 2 | defaultTenantId=1，上下文租户=162，调 `tables()` | 抛 ServiceException，提示"仅总平台" |
| 3 | 上下文租户=162，调 `query(...)` | 抛 ServiceException，且 ClickHouse 不被调用（mock 校验） |

新增 `InviteAdminControllerTest`：
| # | 场景 | 期望 |
|---|---|---|
| 1 | `getUser(userId)` 有值 | 正常返回列表 |
| 2 | `getUser(userId)` 返回 null | 抛 ServiceException，service 不被调用 |

Mockito mock 掉 `ClickHouseArchiveService`、`LogArchiveJob`、`InviteCodeService`、`AdminUserService`；
每个用例前后 `TenantContextHolder.clear()`。

## 不要做的事

- 不改迁移、不改 DO、不动 B 类和 C 类表（本次只做第 1 步）。
- 不改小程序、money-ui。
- 不连接线上。JDK8 写法；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-module-mini -am test -Dtest='ArchiveQueryControllerTest,InviteAdminControllerTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
git diff --stat   # 期望只有 2 个主文件 + 2 个新测试
```

## 评审退回（第 1 步 · 第 1 轮）

`InviteAdminController.getRegistrations` 三处要改：

1. **`inviterUserId` 为 null 时当前直接放行返回数据**，应改为"取不到邀请人就拒绝"（fail closed），
   统一 `throw exception(USER_NOT_EXISTS)`。
2. 代码里写了全限定类名 `cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteRegisterLogDO`，
   改成 import 后用短名；同时删掉没用上的 `InviteCodeDO` import。
3. 测试补一个用例：`listRegistrations` 返回的记录里 `inviterUserId` 为 null 时，抛 ServiceException。

`ArchiveQueryController` 评审通过，不要改。

---

# 交接：第 2 步实现（B 类六张表租户化）

## 前置事实（已核实，不用再查）

| 表 | 行数 | DO | 现有基类 | 写入位置 |
|---|---|---|---|---|
| `custom_audit_log` | 8.4 万 | `module/custom/dal/dataobject/audit/AuditLogDO` | 无基类 | `AuditLogServiceImpl.save` 带 `@Async`（框架异步执行器包了 TtlRunnable，租户上下文会传递） |
| `custom_invite_code` | 56 | `module/custom/dal/dataobject/invite/InviteCodeDO` | `BaseDO` | 注册/生成邀请码，请求线程内 |
| `custom_invite_register_log` | 1 | `module/custom/dal/dataobject/invite/InviteRegisterLogDO` | 无基类 | 注册流程，请求线程内 |
| `system_feedback` | 10 | `module/custom/dal/dataobject/feedback/FeedbackDO` | `BaseDO` | 反馈提交，请求线程内 |
| `custom_user_ip_history` | 2655 | `module/system/dal/dataobject/monitor/UserIpHistoryDO` | 无基类 | `LoginLogServiceImpl`（登录流程）、`SecurityMonitorController` |
| `custom_password_history` | 2596 | `module/system/dal/dataobject/user/PasswordHistoryDO` | 无基类 | `AdminUserServiceImpl` 改密流程；`AutoResetPwdJob` 带 `@TenantIgnore` |

四张"无基类"的表只有 `id + 业务字段 + create_time`，没有 creator/updater/deleted，
**不能**改成继承 `TenantBaseDO`（会多出表里不存在的字段导致插入失败）。

## 改动

### 1. `TenantProperties` 新增配置项

```java
/**
 * 额外的租户表（表名）。用于没有继承 TenantBaseDO、但需要按租户过滤的表
 */
private Set<String> tenantTables = Collections.emptySet();
```

### 2. `TenantDatabaseInterceptor.computeIgnoreTable` 增加一条判断

顺序：`@TenantIgnore` → 忽略；继承 `TenantBaseDO` → 不忽略；**表名在 `tenantTables` 里 → 不忽略**；其余忽略。
表名比较**忽略大小写**（构造方法里把配置的表名转小写存一份）。

### 3. `application.yaml` 的 `yudao.tenant` 下新增

```yaml
    tenant-tables: # 没继承 TenantBaseDO、但需要按租户过滤的表
      - custom_audit_log
      - custom_invite_register_log
      - custom_user_ip_history
      - custom_password_history
```

### 4. 两个 DO 改基类

- `InviteCodeDO`、`FeedbackDO`：`extends BaseDO` → `extends TenantBaseDO`（import 同步改）。

### 5. 新增迁移 `yudao-server/src/main/resources/db/migration/62_tenant_phase2_columns.sql`

幂等、不用 `SET @var`/`PREPARE`；列已存在时 MySQL 报 1060，执行器会跳过：

```sql
ALTER TABLE `custom_audit_log`           ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_invite_code`         ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_invite_register_log` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `system_feedback`            ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_user_ip_history`     ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_password_history`    ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
```

文件头写明：存量数据归默认租户；这些表在请求线程内写入，开启租户后由框架自动过滤。

## 测试

### `TenantDatabaseInterceptorTest` 追加用例（沿用现有写法）

| # | 场景 | 期望 |
|---|------|------|
| 9 | `tenantTables` 含 `t_plain`，`ignoreTable("t_plain")` | false（不忽略） |
| 10 | `tenantTables` 含 `t_plain`，传大写 `T_PLAIN` | false |
| 11 | `tenantTables` 含 `t_ignore`，但该实体有 `@TenantIgnore` | true（注解优先） |
| 12 | `tenantTables` 为空时 `t_plain` | true（保持现有行为） |

### `TenantBackfillMigrationTest` 追加方法校验 62 号

拆分后 6 条语句；每条都是 `ADD COLUMN \`tenant_id\` bigint NOT NULL DEFAULT 1`；
全文不含 `DELETE`、`SET @`、`PREPARE`、`UPDATE`。

## 不要做的事

- 不动 A 类表（接口日志、安全告警、IP 黑名单、登录/操作日志、job 日志）。
- 不改 C 类配置表。
- 不改 `TenantSecurityWebFilter`、小程序、money-ui。
- 不连接线上。JDK8 写法；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-framework/yudao-spring-boot-starter-biz-tenant -am test -Dtest='TenantDatabaseInterceptorTest,TenantSecurityWebFilterTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-server -am test -Dtest='TenantBackfillMigrationTest,SqlMigrationRunnerSplitTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
mvn -q -pl yudao-module-system -am compile -DskipTests
```

## 评审退回（第 2 步 · 第 1 轮）

`TenantDatabaseInterceptorTest.testCase10`（大写表名）失败，暴露实现缺陷：
`computeIgnoreTable` 里 `TableInfoHelper.getTableInfo(tableName) == null` 时直接 `return true`，
在它之后才判断 `tenantTables`，所以：
1. 大小写不一致时（MP 按注册名查不到）配置会失效；
2. 更重要的是，**只用 XML 手写 SQL、没有注册 MyBatis-Plus 实体的表，即使配进 tenantTables 也会被忽略**。

改法：把 `tenantTables` 的判断**提到方法最前面**——

```java
private boolean computeIgnoreTable(String tableName) {
    // 配置为租户表的，优先生效（哪怕没有注册实体）
    if (tenantTables.contains(tableName.toLowerCase())) {
        return false;
    }
    TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
    if (tableInfo == null) {
        return true;
    }
    if (tableInfo.getEntityType().getAnnotation(TenantIgnore.class) != null) {
        return true;
    }
    return !TenantBaseDO.class.isAssignableFrom(tableInfo.getEntityType());
}
```

注意：这样一来用例 11（实体带 `@TenantIgnore` 且表名在 tenantTables 里）的期望要改成 **false（不忽略）**，
因为显式配置优先级更高。请一并把该用例的期望和注释改掉，说明"显式配进 tenantTables 的表优先于 @TenantIgnore"。
其余用例不动。
