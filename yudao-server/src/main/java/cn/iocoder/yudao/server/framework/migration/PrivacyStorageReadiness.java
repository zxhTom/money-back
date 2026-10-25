package cn.iocoder.yudao.server.framework.migration;

import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

/** Required by file access as well as face evidence: a running HTTP port is not schema readiness. */
@Component
@org.springframework.context.annotation.Lazy(false)
@DependsOn("sqlMigrationRunner")
public class PrivacyStorageReadiness {
    @Resource private DataSource dataSource;

    @PostConstruct
    public void verify() { verifySchema(new JdbcTemplate(dataSource)); }

    public static void verifySchema(JdbcOperations jdbc) {
        try {
            jdbc.execute("SELECT id, tenant_id, user_id, session_hash, media_type, path, content, size, sha256, deleted FROM custom_face_evidence WHERE 1 = 0");
            jdbc.execute("SELECT id, config_id, path_hash, canonical_path, deleted FROM infra_file_quarantine WHERE 1 = 0");
            requireIndex(jdbc, "custom_face_evidence", "uk_tenant_session_media");
            requireIndex(jdbc, "infra_file_quarantine", "uk_config_path_hash");
        } catch (RuntimeException ex) {
            throw new IllegalStateException("隐私存储未就绪：请完成73号迁移并核对表结构和唯一索引", ex);
        }
    }

    private static void requireIndex(JdbcOperations jdbc, String table, String index) {
        List<String> columns = jdbc.queryForList("SELECT COLUMN_NAME FROM information_schema.STATISTICS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ? AND NON_UNIQUE = 0 ORDER BY SEQ_IN_INDEX",
                String.class, table, index);
        List<String> expected = "custom_face_evidence".equals(table)
                ? java.util.Arrays.asList("tenant_id", "session_hash", "media_type")
                : java.util.Arrays.asList("config_id", "path_hash");
        if (!expected.equals(columns)) throw new IllegalStateException("缺少或错误的隐私存储唯一索引: " + table);
    }
}
