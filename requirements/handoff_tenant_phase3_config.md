# 交接：第 3 步 · 配置表按租户隔离（批次 A：schema + 隔离）

产品决策（用户 2026-09-25 确认）：**配置每个租户一套**。

本批次只做"隔离"，不做"新租户初始化种子数据"（批次 B 再做）。
做完后新租户的配置是空的——这没关系：租户 162 的套餐里没有任何配置类菜单，
小程序也写死走默认租户 1，所以线上行为不变。

## 前置事实（已核实，不用再查）

| 表 | 行数 | DO（均 `extends BaseDO`） | 现有唯一索引 |
|---|---|---|---|
| `custom_skin_profile` | 11 | `dal/dataobject/skin/SkinProfileDO` | `uk_code(code)` |
| `custom_text_profile` | 3 | `dal/dataobject/text/TextProfileDO` | `uk_code(code)` |
| `custom_text_item` | 2406 | `dal/dataobject/text/TextItemDO` | `uk_profile_item(profile_id,item_key)` |
| `custom_icon_set_profile` | 29 | `dal/dataobject/iconset/IconSetProfileDO` | `uk_code(code)` |
| `custom_miniprogram_config` | 1 | `dal/dataobject/miniconfig/MiniProgramConfigDO` | 无 |
| `custom_version_changelog` | 0 | `dal/dataobject/changelog/VersionChangelogDO` | `uk_version(version)` |
| `custom_speed_control_config` | 1 | `dal/dataobject/speedcontrol/SpeedControlConfigDO` | 无 |
| `fee_strategy` | 7 | `dal/dataobject/strategy/StrategyDO` | `uk_min_max(min_amount,max_amount)` |
| `contract_model` | 25 | `dal/dataobject/contractmodel/ContractModelDO` | `contract_model_unique(app_version)` |
| `time_window` | 6 | `dal/dataobject/timewindow/TimeWindowDO` | 无（**已有 tenant_id 列，值全是 0**） |

DO 的实际路径以仓库为准（上表是 `yudao-module-mini/src/main/java/cn/iocoder/yudao/module/custom/` 下的相对路径）。

迁移执行器对 1060（列重复）、1061（索引名重复）、1091（待删对象不存在）都会跳过，所以下面的 DDL 天然幂等。

## 改动

### 1. 新增 `yudao-server/src/main/resources/db/migration/63_tenant_config_tables.sql`

文件头注释写明：配置表按租户隔离；唯一索引必须带上 tenant_id，否则两个租户不能有同名 code。

```sql
-- 1. 加列（time_window 已有列，靠 1060 跳过；再把它的存量 0 修正为 1）
ALTER TABLE `custom_skin_profile`         ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_text_profile`         ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_text_item`            ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_icon_set_profile`     ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_miniprogram_config`   ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_version_changelog`    ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `custom_speed_control_config` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `fee_strategy`                ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `contract_model`              ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
ALTER TABLE `time_window`                 ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号';
UPDATE `time_window` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

-- 2. 唯一索引改为带租户（先删后建，各自独立成句）
ALTER TABLE `custom_skin_profile`      DROP INDEX `uk_code`;
ALTER TABLE `custom_skin_profile`      ADD UNIQUE KEY `uk_tenant_code` (`tenant_id`,`code`);
ALTER TABLE `custom_text_profile`      DROP INDEX `uk_code`;
ALTER TABLE `custom_text_profile`      ADD UNIQUE KEY `uk_tenant_code` (`tenant_id`,`code`);
ALTER TABLE `custom_icon_set_profile`  DROP INDEX `uk_code`;
ALTER TABLE `custom_icon_set_profile`  ADD UNIQUE KEY `uk_tenant_code` (`tenant_id`,`code`);
ALTER TABLE `custom_version_changelog` DROP INDEX `uk_version`;
ALTER TABLE `custom_version_changelog` ADD UNIQUE KEY `uk_tenant_version` (`tenant_id`,`version`);
ALTER TABLE `fee_strategy`             DROP INDEX `uk_min_max`;
ALTER TABLE `fee_strategy`             ADD UNIQUE KEY `uk_tenant_min_max` (`tenant_id`,`min_amount`,`max_amount`);
ALTER TABLE `contract_model`           DROP INDEX `contract_model_unique`;
ALTER TABLE `contract_model`           ADD UNIQUE KEY `uk_tenant_app_version` (`tenant_id`,`app_version`);
```

`custom_text_item` 的 `uk_profile_item(profile_id,item_key)` **不用改**：profile 本身已按租户隔离，
不同租户的 profile_id 不同，不会冲突。

### 2. 十个 DO 改基类

上表所有 DO：`extends BaseDO` → `extends TenantBaseDO`，同步改 import。不要动其它字段。

### 3. 不要改 `application.yaml` 的 `tenant-tables`

这十张表的 DO 都继承了 `TenantBaseDO`，框架会自动识别，不需要再配进清单。

## 测试

### 新增 `TenantConfigDOTest`（放 `yudao-module-mini/src/test/.../dal/`）

反射校验这十个 DO 都是 `TenantBaseDO` 的子类，且都带 `@TableName`。一个用例用参数化或循环断言即可，
断言失败信息里要带上类名，方便定位。

### `TenantBackfillMigrationTest` 追加 63 号校验

- 拆分后共 **17** 条语句；
- 含 10 条 `ADD COLUMN \`tenant_id\``、1 条 `UPDATE \`time_window\``、3 对 DROP/ADD UNIQUE（共 6 条）；
- 每个新建的唯一索引名里都包含 `tenant`；
- 全文不含 `SET @`、`PREPARE`、`DELETE`。

## 不要做的事

- 不做新租户配置种子数据（批次 B）。
- 不动 A 类平台日志表、不动 B 类已完成的六张表。
- 不改小程序、money-ui。
- 不连接线上。JDK8 写法；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-server -am test -Dtest='TenantBackfillMigrationTest,SqlMigrationRunnerSplitTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am test -Dtest='TenantConfigDOTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
grep -c "extends TenantBaseDO" $(grep -rl "custom_skin_profile\|custom_text_profile\|custom_text_item\|custom_icon_set_profile\|custom_miniprogram_config\|custom_version_changelog\|custom_speed_control_config\|fee_strategy\|contract_model\|time_window" --include=*DO.java yudao-module-mini/src/main)   # 期望每个文件都为 1
```

---

# 交接：第 3 步 · 批次 B（新租户配置初始化）

批次 A 完成后，新建租户的配置是空的。本批次提供"把默认租户的配置复制一份给目标租户"的能力。

## 设计

新增服务 `TenantConfigSeedService`（放 `yudao-module-mini/.../service/tenantconfig/`）+ 一个后台接口。

### 复制范围与顺序

用现有 Mapper（都在 `yudao-module-mini`，已确认存在）：
`SkinProfileMapper`、`TextProfileMapper`、`TextItemMapper`、`IconSetProfileMapper`、
`MiniProgramConfigMapper`、`VersionChangelogMapper`、`SpeedControlConfigMapper`、
`StrategyMapper`、`ContractModelMapper`、`TimeWindowMapper`。

顺序要求：**先复制 `custom_text_profile`，再复制 `custom_text_item`**，因为 item 依赖 profile 的新主键。

### 实现要点

1. 读源租户数据：`TenantUtils.execute(sourceTenantId, () -> mapper.selectList())`。
2. 写目标租户：`TenantUtils.execute(targetTenantId, () -> { ... })`，插入前把 `id` 置为 null、
   `tenantId` 置为 null（让框架按上下文注入），`createTime/updateTime/creator/updater` 交给框架填充。
3. `custom_text_item`：复制 profile 时记录 `旧 profileId -> 新 profileId` 的映射，插入 item 时用新 id。
4. **幂等**：每张表插入前先查目标租户是否已有数据，有就跳过该表，并在返回结果里标明"已跳过"。
5. 返回结构：`Map<String, Integer>`，key 为表名，value 为本次复制的行数（跳过的记 -1），便于接口直接展示。
6. 源租户默认取 `TenantProperties.getDefaultTenantId()`（为空取 1L）。
7. **前置校验**：`TenantProperties` 为 null（租户功能关闭）时，直接抛异常提示"请先开启多租户后再初始化配置"，
   因为关闭时框架不会注入 tenant_id，复制出来的数据是错的。注入方式用 `@Autowired(required = false)`。
8. 目标租户 id 等于源租户 id 时，抛异常拒绝。

### 接口

`yudao-module-mini/.../controller/admin/tenantconfig/TenantConfigController.java`

```
POST /admin-api/system/tenant-config/init?tenantId={id}
@PreAuthorize("@ss.hasPermission('system:tenant:update')")
```

返回上面的 Map。该权限只有总平台的超管有，租户套餐里不包含。

## 测试

新增 `TenantConfigSeedServiceTest`，Mockito mock 全部 Mapper：

| # | 场景 | 期望 |
|---|------|------|
| 1 | 目标租户各表都为空 | 每个 Mapper 的 insert 都按源数据行数被调用；返回值行数正确 |
| 2 | 目标租户的 skin 已有数据 | skin 不再 insert，返回值里 skin 为 -1；其余表照常 |
| 3 | text_item 复制 | 插入的 item 的 profileId 是**新** profile 的 id，不是旧的 |
| 4 | `tenantProperties` 为 null | 抛 ServiceException，任何 Mapper 都不被调用 |
| 5 | targetTenantId 等于源租户 id | 抛 ServiceException，任何 Mapper 都不被调用 |

## 不要做的事

- 不改批次 A 已完成的迁移与 DO。
- 不改小程序、money-ui。
- 不连接线上；不要自动在启动时执行复制（必须由人调接口触发）。
- JDK8 写法；注释从简。

## 验收命令

```
cd money-back
mvn -q -pl yudao-module-mini -am test -Dtest='TenantConfigSeedServiceTest' -Dsurefire.failIfNoSpecifiedTests=false
mvn -q -pl yudao-module-mini -am compile -DskipTests
```

## 评审退回（批次 B · 第 1 轮）

1. **`resetTenantDOFields` 里 id 重置失败被静默吞掉**（`catch (Exception ignored)`）。
   一旦某个 DO 的 `id` 字段定义在父类、或名字不同，就会带着源租户的 id 去插入，造成主键冲突或覆盖。
   改为：沿着类继承链向上查找名为 `id` 的字段；**找不到或赋值失败就抛异常**（`exception0(500, "...")`，
   信息里带上类名），不要继续插入。
2. 代码里多处全限定类名（`cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX`、
   `TenantBaseDO`、`java.lang.reflect.Field`），改为 import 后用短名。
3. 测试追加一个用例：复制后传给 `mapper.insert` 的对象，其 `id` 必须为 null、`tenantId` 必须为 null
   （用 ArgumentCaptor 捕获），确保重置真的生效。
