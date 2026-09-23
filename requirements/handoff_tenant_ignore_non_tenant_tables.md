# 交接文档：只对 TenantBaseDO 表做租户过滤（线上事故修复 · 第 2 次）

## 事故

2026-09-22 23:13 第二次开启租户后，所有请求都返回 HTTP 500（包括免登录的皮肤、文案接口），已回滚。
线上日志中出现 771 次 `Unknown column 'tenant_id'`，涉及的表有：`custom_skin_profile`、`custom_speed_control_config`、`custom_security_alert`、`infra_job_log`、`infra_api_error_log`、`infra_api_access_log`、`pay_order`、`pay_order_extension`、`pay_transfer` 等；另有 `custom_ip_blacklist` 查询报「TenantContextHolder 不存在租户编号」。

## 根因

`TenantDatabaseInterceptor.computeIgnoreTable`：MyBatis-Plus 注册过的实体，只要**没继承 `TenantBaseDO`、也没加 `@TenantIgnore`**，就返回 false，即当作租户表处理。于是 SQL 会被追加 `tenant_id = ?`，插入时也会被注入 `tenant_id`。
原版 yudao 几乎每张表都有 `tenant_id` 字段；但本项目绝大部分表没有这个字段，SQL 直接报错。而且这些表在请求刚进来时就会被安全过滤器查询（IP 黑名单、限速、模拟请求拦截），此时还没有租户上下文。

线上已核实：所有继承 `TenantBaseDO` 且实际存在的表（`custom_contract`、`system_users`、`system_role`、`system_role_menu`、`system_dept`、令牌两张表、`system_social_client`、`mp_account`、`pay_channel`、`pay_notify_task`）都有 `tenant_id` 字段。

## 改动（只改以下文件）

### 1. `yudao-framework/yudao-spring-boot-starter-biz-tenant/src/main/java/cn/iocoder/yudao/framework/tenant/core/db/TenantDatabaseInterceptor.java`

`computeIgnoreTable` 改为：**只有实体继承了 `TenantBaseDO` 才不忽略**，其余一律忽略。

```java
private boolean computeIgnoreTable(String tableName) {
    TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
    if (tableInfo == null) {
        return true;
    }
    // 本项目只有继承 TenantBaseDO 的表带 tenant_id 列，其余表一律不做租户过滤
    return !TenantBaseDO.class.isAssignableFrom(tableInfo.getEntityType());
}
```

- 删掉原来 `@TenantIgnore` 那段判断，以及因此不再用到的 import。
- 如果某个 `TenantBaseDO` 实体加了 `@TenantIgnore`，要保持被忽略：先判断 `@TenantIgnore`，有就返回 true，再判断 `TenantBaseDO`。
- 其他方法（`ignoreTable`、`getTenantId`、构造方法里的 ignore-tables）不动。

### 2. 新增 `yudao-framework/yudao-spring-boot-starter-biz-tenant/src/test/java/cn/iocoder/yudao/framework/tenant/core/db/TenantDatabaseInterceptorTest.java`

在测试类里定义 4 个内部实体类，并用 `TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), XxxDO.class)` 注册，表名各不相同：

- `@TableName("t_tenant") TenantDO extends TenantBaseDO`
- `@TableName("t_plain") PlainDO extends BaseDO`
- `@TableName("t_ignore") @TenantIgnore IgnoreDO extends BaseDO`
- `@TableName("t_tenant_ignore") @TenantIgnore TenantIgnoreDO extends TenantBaseDO`

每个用例前后都 `TenantContextHolder.clear()`。

| # | 场景 | 期望 |
|---|------|------|
| 1 | `ignoreTable("t_tenant")` | false |
| 2 | `ignoreTable("t_plain")` | **true**（本次修复的核心） |
| 3 | `ignoreTable("t_ignore")` | true |
| 4 | `ignoreTable("t_tenant_ignore")` | true |
| 5 | `ignoreTable("not_registered_table")` | true |
| 6 | 构造时 `TenantProperties.ignoreTables` 含 `t_tenant`，`ignoreTable("t_tenant")` | true |
| 7 | `TenantContextHolder.setIgnore(true)` 后 `ignoreTable("t_tenant")` | true |
| 8 | `ignoreTable("`t_plain`")`（带反引号）和 `ignoreTable("T_PLAIN")` | 都是 true |

## 不要做的事

- 不改 `TenantSecurityWebFilter`、`TenantProperties`、`application.yaml`、任何迁移文件。
- 不给实体加 `@TenantIgnore`、不改任何 DO。
- 不改小程序、money-ui。
- 不连接、不操作任何线上环境。
- 按 JDK8 写；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-framework/yudao-spring-boot-starter-biz-tenant -am test -Dtest='TenantDatabaseInterceptorTest,TenantSecurityWebFilterTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
```
