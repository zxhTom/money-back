package cn.iocoder.yudao.server.framework.migration;

import cn.hutool.crypto.SecureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 轻量 SQL 迁移执行器（参考 Liquibase/Flyway 思路，自研精简版）。
 *
 * 启动时自动扫描 classpath:db/migration/*.sql，按文件名顺序，把"没执行过的"逐个执行，
 * 执行记录写入 custom_sql_migration 表（按文件名去重，每个文件只成功执行一次）。
 *
 * 特性：
 *  - 分布式锁（MySQL GET_LOCK）串行化，多实例同时启动不冲突；
 *  - 自愈：单条语句遇到"对象已存在"（列/表/索引/唯一键已存在）视为已应用并跳过，
 *    因此哪怕之前手工执行过部分 SQL，再次自动执行也不会报错、不会重复；
 *  - 某文件真正失败则记 FAILED 并停止后续文件（下次启动会重试 FAILED 的），但不阻断应用启动；
 *  - 关闭开关：yudao.sql-migration.enabled=false。
 *
 * 新增迁移：把 .sql 放进 yudao-server/src/main/resources/db/migration/，重新部署即自动执行。
 */
@Component
public class SqlMigrationRunner {

    private static final Logger log = LoggerFactory.getLogger(SqlMigrationRunner.class);

    private static final String LOCATION = "db/migration/";
    private static final String LOCK_NAME = "yudao_sql_migration_lock";
    private static final int LOCK_TIMEOUT_SECONDS = 60;

    @Value("${yudao.sql-migration.enabled:true}")
    private boolean enabled;

    @javax.annotation.Resource
    private DataSource dataSource; // dynamic-datasource 的主数据源（路由到 master）

    @PostConstruct
    public void migrate() {
        if (!enabled) {
            log.info("[SqlMigration] yudao.sql-migration.enabled=false，跳过 SQL 自动迁移");
            return;
        }
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        Integer locked = jdbc.queryForObject("SELECT GET_LOCK(?, ?)", Integer.class, LOCK_NAME, LOCK_TIMEOUT_SECONDS);
        if (locked == null || locked != 1) {
            log.warn("[SqlMigration] 未获取到迁移锁（可能有其它实例正在迁移），本实例跳过");
            return;
        }
        try {
            ensureTrackingTable(jdbc);
            Set<String> applied = loadApplied(jdbc);
            List<Resource> files = scanMigrationFiles();
            log.info("[SqlMigration] 发现迁移文件 {} 个，已应用 {} 个", files.size(), applied.size());
            for (Resource f : files) {
                String name = f.getFilename();
                if (name == null || applied.contains(name)) {
                    continue;
                }
                boolean cont = applyFile(jdbc, f, name);
                if (!cont) {
                    break; // 某文件失败，停止后续，等下次启动重试
                }
            }
        } catch (Exception e) {
            log.error("[SqlMigration] 迁移过程异常（应用继续启动）", e);
        } finally {
            try {
                jdbc.execute("DO RELEASE_LOCK('" + LOCK_NAME + "')");
            } catch (Exception ignored) {
            }
        }
    }

    private void ensureTrackingTable(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS `custom_sql_migration` (" +
                "`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键'," +
                "`filename` varchar(255) NOT NULL COMMENT '迁移文件名'," +
                "`checksum` varchar(64) DEFAULT NULL COMMENT '内容MD5'," +
                "`status` varchar(20) NOT NULL COMMENT 'SUCCESS/FAILED'," +
                "`statements_ok` int DEFAULT 0 COMMENT '成功语句数'," +
                "`statements_skipped` int DEFAULT 0 COMMENT '跳过(已存在)语句数'," +
                "`applied_at` datetime DEFAULT NULL COMMENT '执行时间'," +
                "`error_msg` varchar(2000) DEFAULT NULL COMMENT '失败信息'," +
                "PRIMARY KEY (`id`), UNIQUE KEY `uk_filename` (`filename`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQL迁移执行记录'");
    }

    private Set<String> loadApplied(JdbcTemplate jdbc) {
        // 只有 SUCCESS 才算已应用；FAILED 允许下次重试
        return new HashSet<>(jdbc.queryForList(
                "SELECT filename FROM custom_sql_migration WHERE status = 'SUCCESS'", String.class));
    }

    private List<Resource> scanMigrationFiles() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] arr = resolver.getResources("classpath*:" + LOCATION + "*.sql");
        List<Resource> list = new ArrayList<>(Arrays.asList(arr));
        list.sort(Comparator.comparing(r -> r.getFilename() == null ? "" : r.getFilename()));
        return list;
    }

    /** @return true 继续下一个文件；false 停止（本文件失败） */
    private boolean applyFile(JdbcTemplate jdbc, Resource f, String name) {
        String content;
        try (InputStream is = f.getInputStream()) {
            content = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[SqlMigration][{}] 读取失败", name, e);
            return false;
        }
        String checksum = SecureUtil.md5(content);
        List<String> statements = splitStatements(content);
        int ok = 0, skipped = 0;
        for (String sql : statements) {
            try {
                jdbc.execute(sql);
                ok++;
            } catch (DataAccessException ex) {
                if (isAlreadyExists(ex)) {
                    skipped++;
                    log.info("[SqlMigration][{}] 跳过(对象已存在)：{}", name, brief(sql));
                } else {
                    record(jdbc, name, checksum, "FAILED", ok, skipped, ex.getMessage());
                    log.error("[SqlMigration][{}] 语句失败，停止后续迁移：{}", name, brief(sql), ex);
                    return false;
                }
            }
        }
        record(jdbc, name, checksum, "SUCCESS", ok, skipped, null);
        log.info("[SqlMigration][{}] 应用成功 ok={} skipped={}", name, ok, skipped);
        return true;
    }

    private void record(JdbcTemplate jdbc, String name, String checksum, String status,
                        int ok, int skipped, String error) {
        try {
            jdbc.update("INSERT INTO custom_sql_migration " +
                    "(filename, checksum, status, statements_ok, statements_skipped, applied_at, error_msg) " +
                    "VALUES (?,?,?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE checksum=VALUES(checksum), status=VALUES(status), " +
                    "statements_ok=VALUES(statements_ok), statements_skipped=VALUES(statements_skipped), " +
                    "applied_at=VALUES(applied_at), error_msg=VALUES(error_msg)",
                    name, checksum, status, ok, skipped, LocalDateTime.now(),
                    error == null ? null : (error.length() > 1990 ? error.substring(0, 1990) : error));
        } catch (Exception e) {
            log.warn("[SqlMigration][{}] 写执行记录失败：{}", name, e.getMessage());
        }
    }

    // ── 可测试的纯逻辑 ────────────────────────────────────────────────

    /** 去掉整行 -- 注释与空行，按分号切分为多条语句 */
    static List<String> splitStatements(String content) {
        StringBuilder sb = new StringBuilder();
        for (String line : content.split("\n")) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("--")) {
                continue;
            }
            sb.append(line).append('\n');
        }
        List<String> out = new ArrayList<>();
        for (String s : sb.toString().split(";")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    /** 判断是否"对象已存在"类错误（可安全跳过，实现幂等/自愈） */
    static boolean isAlreadyExists(Throwable ex) {
        Throwable c = ex;
        while (c != null) {
            if (c instanceof SQLException) {
                int code = ((SQLException) c).getErrorCode();
                // 1050 表已存在, 1060 列重复, 1061 索引名重复, 1062 唯一键冲突, 1091 待删对象不存在, 1826 外键名重复
                if (code == 1050 || code == 1060 || code == 1061 || code == 1062 || code == 1091 || code == 1826) {
                    return true;
                }
            }
            String m = c.getMessage();
            if (m != null) {
                String low = m.toLowerCase();
                if (low.contains("already exists") || low.contains("duplicate column")
                        || low.contains("duplicate key name") || low.contains("duplicate entry")
                        || low.contains("duplicate foreign key")) {
                    return true;
                }
            }
            c = c.getCause();
        }
        return false;
    }

    private static String brief(String sql) {
        String s = sql.replaceAll("\\s+", " ").trim();
        return s.length() > 100 ? s.substring(0, 100) + "…" : s;
    }

}
