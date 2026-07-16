package cn.iocoder.yudao.module.custom.service.contract;

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
public class ContractRecycleJob {

    private static final int BATCH = 500;

    @Resource
    private ClickHouseArchiveService ch;
    @Resource
    private DataSource dataSource;

    @Value("${yudao.contract-recycle.enabled:true}")
    private boolean enabled;
    @Value("${yudao.contract-recycle.months:6}")
    private int months;

    @Scheduled(cron = "${yudao.contract-recycle.cron:0 30 4 * * ?}")
    public void run() {
        if (!enabled || !ch.isEnabled()) {
            log.info("[ContractRecycle] 跳过（enabled={} ch.enabled={}）", enabled, ch.isEnabled());
            return;
        }
        try {
            recycleOnce();
        } catch (Exception e) {
            log.error("[ContractRecycle] 回收失败", e);
        }
    }

    public int recycleOnce() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int total = 0;
        while (true) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT * FROM custom_contract WHERE deleted=1 AND update_time < NOW() - INTERVAL " + months
                            + " MONTH ORDER BY id LIMIT " + BATCH);
            if (rows.isEmpty()) {
                break;
            }
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            List<String> cols = new java.util.ArrayList<>(rows.get(0).keySet());
            cols.add("archive_time");
            List<Object[]> data = new java.util.ArrayList<>();
            List<Object> ids = new java.util.ArrayList<>();
            for (Map<String, Object> r : rows) {
                Object[] arr = new Object[cols.size()];
                for (int i = 0; i < cols.size() - 1; i++) {
                    arr[i] = r.get(cols.get(i));
                }
                arr[cols.size() - 1] = now;
                data.add(arr);
                ids.add(r.get("id"));
            }
            ch.insertRows("contract_recycle", cols, data);
            String ph = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
            jdbc.update("DELETE FROM custom_contract WHERE id IN (" + ph + ")", ids.toArray());
            total += rows.size();
            if (rows.size() < BATCH) {
                break;
            }
        }
        if (total > 0) {
            log.warn("[ContractRecycle] 物理删除并归档合同 {} 条", total);
        }
        return total;
    }
}
