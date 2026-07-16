package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class ContractRecycleJob {

    private static final int BATCH = 500;
    private static final List<String> COLS = Arrays.asList(
            "id", "indebted_name", "indebted_id", "creditor_name", "creditor_id", "description",
            "status", "start_date", "end_date", "return_type", "reason_type",
            "create_time", "update_time", "archive_time");

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
            // 6 个月前被软删的：deleted=1 且 update_time 早于 N 个月
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT id,indebted_name,indebted_id,creditor_name,creditor_id,description,status,"
                            + "start_date,end_date,return_type,reason_type,create_time,update_time "
                            + "FROM custom_contract WHERE deleted=1 AND update_time < NOW() - INTERVAL " + months
                            + " MONTH ORDER BY id LIMIT " + BATCH);
            if (rows.isEmpty()) {
                break;
            }
            LocalDateTime now = LocalDateTime.now();
            List<Object[]> data = new ArrayList<>();
            List<Object> ids = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                data.add(new Object[]{
                        r.get("id"), r.get("indebted_name"), r.get("indebted_id"), r.get("creditor_name"),
                        r.get("creditor_id"), r.get("description"), r.get("status"), r.get("start_date"),
                        r.get("end_date"), r.get("return_type"), r.get("reason_type"),
                        r.get("create_time"), r.get("update_time"), now});
                ids.add(r.get("id"));
            }
            ch.insertRows("contract_recycle", COLS, data); // 先写 CH
            String ph = String.join(",", Collections.nCopies(ids.size(), "?"));
            // 物理删除（绕过逻辑删）：直接 DELETE
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
