package cn.iocoder.yudao.server.framework.migration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TenantBackfillMigrationTest {

    private String read(String path) throws Exception {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

    @Test
    public void test59TenantEnableBackfillMigration() throws Exception {
        String content = read("db/migration/59_tenant_enable_backfill.sql");
        List<String> statements = SqlMigrationRunner.splitStatements(content);

        // 1. 断言共 11 条语句
        assertEquals(11, statements.size(), "59 号迁移应切分为 11 条语句");

        // 2. 断言包含 ALTER TABLE `custom_contract` ALTER COLUMN `tenant_id` SET DEFAULT 1
        boolean hasAlterDefault = statements.stream().anyMatch(stmt ->
                stmt.contains("ALTER TABLE `custom_contract` ALTER COLUMN `tenant_id` SET DEFAULT 1"));
        assertTrue(hasAlterDefault, "应包含修改 custom_contract 默认租户为 1 的 ALTER 语句");

        // 3. 断言有 custom_contract 和 custom_contract_daily_stats 的 tenant_id = 1 WHERE tenant_id = 0 回填语句
        boolean hasContractBackfill = statements.stream().anyMatch(stmt -> {
            String s = stmt.replaceAll("\\s+", " ");
            return s.contains("UPDATE `custom_contract` SET `tenant_id` = 1, `update_time` = `update_time` WHERE `tenant_id` = 0");
        });
        boolean hasStatsBackfill = statements.stream().anyMatch(stmt ->
                stmt.replaceAll("\\s+", " ").contains("UPDATE `custom_contract_daily_stats` SET `tenant_id` = 1 WHERE `tenant_id` = 0"));
        assertTrue(hasContractBackfill, "应包含 custom_contract 显式赋值 update_time 的回填 UPDATE 语句");
        assertTrue(hasStatsBackfill, "应包含 custom_contract_daily_stats 租户 0 到 1 的回填 UPDATE 语句");

        // 4. 全文不含 admin2、2000、system_user_role、SET @、PREPARE，也不含 DELETE 语句/关键字
        String upperContent = content.toUpperCase();
        assertFalse(content.contains("admin2"), "全文不应包含 admin2");
        assertFalse(content.contains("2000"), "全文不应包含 2000");
        assertFalse(content.contains("system_user_role"), "全文不应包含 system_user_role");
        assertFalse(upperContent.contains("SET @"), "全文不应包含 SET @");
        assertFalse(upperContent.contains("PREPARE"), "全文不应包含 PREPARE");
        boolean hasDeleteStmt = statements.stream().anyMatch(stmt -> stmt.toUpperCase().startsWith("DELETE"));
        assertFalse(hasDeleteStmt, "不应包含 DELETE 语句");
    }

    @Test
    public void test60TenantTokenRebackfillMigration() throws Exception {
        String content = read("db/migration/60_tenant_token_rebackfill.sql");
        List<String> statements = SqlMigrationRunner.splitStatements(content);

        // 1. 拆分后共 5 条语句
        assertEquals(5, statements.size(), "60 号迁移应切分为 5 条语句");

        // 2. 合同那条包含 `update_time` = `update_time`
        boolean hasContractBackfill = statements.stream().anyMatch(stmt -> {
            String s = stmt.replaceAll("\\s+", " ");
            return s.contains("UPDATE `custom_contract` SET `tenant_id` = 1, `update_time` = `update_time` WHERE `tenant_id` = 0");
        });
        assertTrue(hasContractBackfill, "合同回填语句必须包含 `update_time` = `update_time`");

        // 3. 不含 DELETE、SET @、PREPARE、admin2
        String upperContent = content.toUpperCase();
        assertFalse(content.contains("admin2"), "全文不应包含 admin2");
        assertFalse(upperContent.contains("SET @"), "全文不应包含 SET @");
        assertFalse(upperContent.contains("PREPARE"), "全文不应包含 PREPARE");
        boolean hasDeleteStmt = statements.stream().anyMatch(stmt -> stmt.toUpperCase().startsWith("DELETE"));
        assertFalse(hasDeleteStmt, "不应包含 DELETE 语句");
    }

    @Test
    public void test61TenantTokenBackfill2Migration() throws Exception {
        String content = read("db/migration/61_tenant_token_backfill2.sql");
        List<String> statements = SqlMigrationRunner.splitStatements(content);

        // 1. 拆分后共 2 条语句
        assertEquals(2, statements.size(), "61 号迁移应切分为 2 条语句");

        // 2. 两条都是 tenant_id = 1 WHERE tenant_id = 0 的 UPDATE
        boolean hasAccessBackfill = statements.stream().anyMatch(stmt ->
                stmt.replaceAll("\\s+", " ").contains("UPDATE `system_oauth2_access_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0"));
        boolean hasRefreshBackfill = statements.stream().anyMatch(stmt ->
                stmt.replaceAll("\\s+", " ").contains("UPDATE `system_oauth2_refresh_token` SET `tenant_id` = 1 WHERE `tenant_id` = 0"));
        assertTrue(hasAccessBackfill, "应包含 system_oauth2_access_token 的回填 UPDATE 语句");
        assertTrue(hasRefreshBackfill, "应包含 system_oauth2_refresh_token 的回填 UPDATE 语句");

        // 3. 全文不含 DELETE、SET @、PREPARE
        String upperContent = content.toUpperCase();
        assertFalse(upperContent.contains("SET @"), "全文不应包含 SET @");
        assertFalse(upperContent.contains("PREPARE"), "全文不应包含 PREPARE");
        boolean hasDeleteStmt = statements.stream().anyMatch(stmt -> stmt.toUpperCase().startsWith("DELETE"));
        assertFalse(hasDeleteStmt, "不应包含 DELETE 语句");
    }

    @Test
    public void test62TenantPhase2ColumnsMigration() throws Exception {
        String content = read("db/migration/62_tenant_phase2_columns.sql");
        List<String> statements = SqlMigrationRunner.splitStatements(content);

        // 1. 拆分后 6 条语句
        assertEquals(6, statements.size(), "62 号迁移应切分为 6 条语句");

        // 2. 每条都是 ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1
        for (String stmt : statements) {
            String s = stmt.replaceAll("\\s+", " ");
            assertTrue(s.contains("ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1"),
                    "每条语句都应为 ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1: " + stmt);
        }

        // 3. 全文不含 DELETE、SET @、PREPARE、UPDATE
        String upperContent = content.toUpperCase();
        assertFalse(upperContent.contains("DELETE"), "全文不应包含 DELETE");
        assertFalse(upperContent.contains("SET @"), "全文不应包含 SET @");
        assertFalse(upperContent.contains("PREPARE"), "全文不应包含 PREPARE");
        assertFalse(upperContent.contains("UPDATE"), "全文不应包含 UPDATE");
    }

    @Test
    public void test63TenantConfigTablesMigration() throws Exception {
        String content = read("db/migration/63_tenant_config_tables.sql");
        List<String> statements = SqlMigrationRunner.splitStatements(content);

        // 1. 拆分后共 23 条语句
        assertEquals(23, statements.size(), "63 号迁移应切分为 23 条语句");

        // 2. 10 条 ADD COLUMN `tenant_id`
        long addColCount = statements.stream().filter(stmt ->
                stmt.replaceAll("\\s+", " ").contains("ADD COLUMN `tenant_id`")).count();
        assertEquals(10, addColCount, "应有 10 条 ADD COLUMN `tenant_id` 语句");

        // 3. 1 条 UPDATE `time_window`
        long updateCount = statements.stream().filter(stmt ->
                stmt.replaceAll("\\s+", " ").contains("UPDATE `time_window` SET `tenant_id` = 1 WHERE `tenant_id` = 0")).count();
        assertEquals(1, updateCount, "应有 1 条 UPDATE `time_window` 语句");

        // 4. 6 对 DROP/ADD UNIQUE（共 12 条）
        long dropIndexCount = statements.stream().filter(stmt -> stmt.contains("DROP INDEX")).count();
        long addUniqueCount = statements.stream().filter(stmt -> stmt.contains("ADD UNIQUE KEY")).count();
        assertEquals(6, dropIndexCount, "应有 6 条 DROP INDEX 语句");
        assertEquals(6, addUniqueCount, "应有 6 条 ADD UNIQUE KEY 语句");

        // 5. 每个新建的唯一索引名里都包含 tenant
        statements.stream().filter(stmt -> stmt.contains("ADD UNIQUE KEY")).forEach(stmt -> {
            assertTrue(stmt.contains("tenant"), "新建的唯一索引名里必须包含 tenant: " + stmt);
        });

        // 6. 全文不含 SET @、PREPARE、DELETE
        String upperContent = content.toUpperCase();
        assertFalse(upperContent.contains("SET @"), "全文不应包含 SET @");
        assertFalse(upperContent.contains("PREPARE"), "全文不应包含 PREPARE");
        boolean hasDeleteStmt = statements.stream().anyMatch(stmt -> stmt.toUpperCase().startsWith("DELETE"));
        assertFalse(hasDeleteStmt, "不应包含 DELETE 语句");
    }

}
