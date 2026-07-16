package cn.iocoder.yudao.module.custom.framework.clickhouse;

import cn.iocoder.yudao.module.custom.framework.archive.ArchiveTableRegistry;
import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ClickHouse 建表自动迁移：应用启动时自动检测并执行（增量、幂等）。
 *
 * - 未启用 ClickHouse（yudao.clickhouse.enabled=false / 连接不可用）时整体跳过，不影响主系统启动。
 * - 日志归档表统一 (id, log_time, data) schema，从 {@link ArchiveTableRegistry} 生成，`CREATE TABLE IF NOT EXISTS` 幂等。
 * - 其它 DDL（contract_recycle 及后续增量）放 classpath:clickhouse/*.sql，逐条执行；
 *   所有语句务必写成幂等（IF NOT EXISTS），重复启动会自动跳过已存在对象。
 * - 新增归档表：加进 ArchiveTableRegistry 即可；新增其它表/列：往 clickhouse/*.sql 追加幂等语句。下次启动自动执行。
 */
@Component
@Slf4j
public class ClickHouseMigrationRunner {

    private final ClickHouseArchiveService ch;

    public ClickHouseMigrationRunner(ClickHouseArchiveService ch) {
        this.ch = ch;
    }

    @PostConstruct
    public void migrate() {
        if (!ch.isEnabled()) {
            log.info("[ClickHouseMigration] ClickHouse 未启用，跳过建表检测");
            return;
        }
        int ok = 0, fail = 0;
        // 1. 日志归档表（统一 JSON schema，从注册表生成）
        for (ArchiveTableRegistry.ArchiveTable t : ArchiveTableRegistry.tables()) {
            try {
                ch.ensureTable(logArchiveDdl(t.getChTable()));
                ok++;
            } catch (Exception e) {
                fail++;
                log.error("[ClickHouseMigration] 建表 {} 失败：{}", t.getChTable(), e.getMessage());
            }
        }
        // 2. clickhouse/*.sql 中的其它 DDL（contract_recycle 及后续增量），幂等
        for (String stmt : loadFileStatements()) {
            try {
                ch.execute(stmt);
                ok++;
            } catch (Exception e) {
                fail++;
                log.error("[ClickHouseMigration] 执行DDL失败：{} —— {}", brief(stmt), e.getMessage());
            }
        }
        log.info("[ClickHouseMigration] ClickHouse 建表检测完成：成功 {}，失败 {}", ok, fail);
    }

    /** 日志归档统一表结构：整行 JSON 存 data，无需与源表逐列对齐 */
    private static String logArchiveDdl(String table) {
        return "CREATE TABLE IF NOT EXISTS " + table + " ("
                + "id UInt64, log_time DateTime, data String"
                + ") ENGINE = MergeTree PARTITION BY toYYYYMM(log_time) ORDER BY (log_time, id)";
    }

    /** 读取 classpath:clickhouse/*.sql，去注释、按分号切分为语句 */
    private List<String> loadFileStatements() {
        List<String> out = new ArrayList<>();
        try {
            Resource[] files = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:clickhouse/*.sql");
            for (Resource f : files) {
                String content;
                try (InputStream is = f.getInputStream()) {
                    content = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
                }
                out.addAll(splitStatements(content));
            }
        } catch (Exception e) {
            log.warn("[ClickHouseMigration] 读取 clickhouse/*.sql 失败：{}", e.getMessage());
        }
        return out;
    }

    /** 去掉整行 -- 注释与空行，按分号切分 */
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

    private static String brief(String sql) {
        String s = sql.replaceAll("\\s+", " ").trim();
        return s.length() > 80 ? s.substring(0, 80) + "…" : s;
    }
}
