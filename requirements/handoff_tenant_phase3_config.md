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
