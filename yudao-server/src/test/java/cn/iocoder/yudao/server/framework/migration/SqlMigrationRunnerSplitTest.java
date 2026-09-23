package cn.iocoder.yudao.server.framework.migration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 迁移文件必须能被 SqlMigrationRunner 正确切分，且不能依赖会话级状态。
 *
 * 执行器对每条语句单独调 JdbcTemplate.execute()，无事务、不保证同一物理连接，
 * 所以 SET @var / PREPARE / EXECUTE 这类跨语句传值的写法在这里是不可靠的。
 */
public class SqlMigrationRunnerSplitTest {

    private String read(String path) throws Exception {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }

    /**
     * 真正会被执行的语句里不许出现会话级状态依赖。
     * 必须检查切分后的语句而不是文件原文——文件注释里可以（也应该）提到这些关键字来说明为什么不能用。
     */
    @Test
    public void testNoMigrationUsesSessionState() throws Exception {
        String[] files = {
                "db/migration/54_contract_tenant_id.sql",
                "db/migration/55_speed_control.sql",
                "db/migration/56_pay_password_changed.sql",
                "db/migration/57_tenant_backfill.sql",
                "db/migration/58_contract_daily_stats.sql",
                "db/migration/59_tenant_enable_backfill.sql",
                "db/migration/60_tenant_token_rebackfill.sql",
                "db/migration/61_tenant_token_backfill2.sql"};
        for (String file : files) {
            for (String sql : SqlMigrationRunner.splitStatements(read(file))) {
                String upper = sql.toUpperCase();
                assertFalse(upper.contains("SET @"), file + " 有语句用了会话变量 SET @：" + sql);
                assertFalse(upper.startsWith("PREPARE ") || upper.startsWith("EXECUTE ")
                                || upper.startsWith("DEALLOCATE "),
                        file + " 有语句用了 PREPARE/EXECUTE：" + sql);
                assertFalse(upper.contains("LAST_INSERT_ID()"), file + " 有语句用了 LAST_INSERT_ID()：" + sql);
            }
        }
    }

    @Test
    public void testTenantMigrationSplitsIntoExpectedStatements() throws Exception {
        List<String> statements = SqlMigrationRunner.splitStatements(read("db/migration/54_contract_tenant_id.sql"));
        assertEquals(5, statements.size(), "应切分为 1 条 ALTER + 1 条 UPDATE + 3 条 INSERT");
        assertTrue(statements.get(0).toUpperCase().startsWith("ALTER TABLE"));
        assertTrue(statements.get(1).toUpperCase().startsWith("UPDATE"));
        for (int i = 2; i < 5; i++) {
            assertTrue(statements.get(i).toUpperCase().startsWith("INSERT INTO"),
                    "第 " + i + " 条应为 INSERT，实际：" + statements.get(i));
        }
    }

    @Test
    public void testSpeedControlMigrationSplitsCleanly() throws Exception {
        List<String> statements = SqlMigrationRunner.splitStatements(read("db/migration/55_speed_control.sql"));
        assertEquals(4, statements.size(), "1 条建表 + 1 条初始化行 + 2 条菜单");
        assertTrue(statements.get(0).toUpperCase().contains("CREATE TABLE"));
    }

    /** 注释行和空行不能被当成语句 */
    @Test
    public void testCommentsAndBlankLinesStripped() {
        List<String> statements = SqlMigrationRunner.splitStatements(
                "-- 注释一\n\n-- 注释二\nSELECT 1;\n\n-- 尾部注释\nSELECT 2;\n");
        assertEquals(2, statements.size());
        assertEquals("SELECT 1", statements.get(0).trim());
        assertEquals("SELECT 2", statements.get(1).trim());
    }

}
