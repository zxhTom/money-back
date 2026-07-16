package cn.iocoder.yudao.module.custom.framework.archive;

import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;

@Component
@Slf4j
public class LogArchiveJob {

    private static final int BATCH = 2000;
    private static final int MAX_ROUNDS = 500;

    @Resource
    private ClickHouseArchiveService ch;
    @Resource
    private DataSource dataSource; // dynamic-datasource 主库

    @Value("${yudao.log-archive.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${yudao.log-archive.cron:0 0 4 * * ?}")
    public void run() {
        if (!enabled || !ch.isEnabled()) {
            log.info("[LogArchive] 跳过（enabled={} ch.enabled={}）", enabled, ch.isEnabled());
            return;
        }
        for (ArchiveTableRegistry.ArchiveTable t : ArchiveTableRegistry.tables()) {
            try {
                archiveTable(t);
            } catch (Exception e) {
                log.error("[LogArchive] 表 {} 归档失败，跳过", t.getMysqlTable(), e);
            }
        }
    }

    /** 单表归档：分批 先写CH成功→再删MySQL；返回归档总条数（供测试） */
    public int archiveTable(ArchiveTableRegistry.ArchiveTable t) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String cutoff = "NOW() - INTERVAL " + t.getRetentionDays() + " DAY";
        int total = 0;
        for (int round = 0; round < MAX_ROUNDS; round++) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT * FROM " + t.getMysqlTable()
                            + " WHERE " + t.getTimeColumn() + " < " + cutoff
                            + " ORDER BY id LIMIT " + BATCH);
            if (rows.isEmpty()) {
                break;
            }
            List<String> cols = new ArrayList<>(rows.get(0).keySet());
            List<Object[]> data = new ArrayList<>();
            List<Object> ids = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                Object[] arr = new Object[cols.size()];
                for (int i = 0; i < cols.size(); i++) {
                    arr[i] = r.get(cols.get(i));
                }
                data.add(arr);
                ids.add(r.get("id"));
            }
            ch.insertRows(t.getChTable(), cols, data); // 先写 CH，失败则抛出、不删 MySQL
            String ph = String.join(",", Collections.nCopies(ids.size(), "?"));
            jdbc.update("DELETE FROM " + t.getMysqlTable() + " WHERE id IN (" + ph + ")", ids.toArray());
            total += rows.size();
            if (rows.size() < BATCH) {
                break;
            }
        }
        if (total > 0) {
            log.info("[LogArchive] 表 {} 归档 {} 条到 {}", t.getMysqlTable(), total, t.getChTable());
        }
        return total;
    }
}
