package cn.iocoder.yudao.module.custom.service.stats;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.custom.dal.mysql.stats.ContractDailyStatsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * 合同每日金额快照 Job。
 *
 * 每晚重算最近 windowDays 天的快照。已归档的日期靠唯一键 + INSERT IGNORE 原样跳过，
 * 所以重算是安全的，还能自动补齐停机期间漏掉的天——不需要维护水位线
 * （从 MAX(stat_date) 推水位线有个坑：某天一份合同都没有就没有快照行，水位线会永远卡住）。
 *
 * 为什么必须归档而不是实时算：合同会被软删、会被回收 Job 物理删除、会被归档 Job
 * 搬去 archive 库、还会被租户条件过滤掉，任何一种都会让"历史上那一天"的金额缩水。
 */
@Component
@Slf4j
public class ContractDailyStatsJob {

    @Resource
    private ContractDailyStatsMapper contractDailyStatsMapper;

    @Value("${yudao.contract-daily-stats.enabled:true}")
    private boolean enabled;

    /** 每晚重算的窗口天数，与大盘展示的 30 天窗口对齐 */
    @Value("${yudao.contract-daily-stats.window-days:30}")
    private int windowDays;

    @Scheduled(cron = "${yudao.contract-daily-stats.cron:0 30 0 * * ?}")
    public void run() {
        if (!enabled) {
            log.info("[ContractDailyStats] 跳过（enabled=false）");
            return;
        }
        try {
            // 必须用 TenantUtils.executeIgnore 而不是给方法加 @TenantIgnore：
            // 那个注解靠 @Around 代理生效，同类内部调用会绕过代理，注解等于没写，
            // 定时任务线程又没有租户上下文，第一次查询就会在 getRequiredTenantId() 抛 NPE。
            TenantUtils.executeIgnore(this::snapshotRecentDays);
        } catch (Exception e) {
            log.error("[ContractDailyStats] 快照失败", e);
        }
    }

    /**
     * 快照窗口 [今天-windowDays, 今天)，即截止到昨天为止。
     * 绝不快照今天：当天数据还在变，提前归档会把没过完的一天冻住。
     */
    public int snapshotRecentDays() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(Math.max(windowDays, 1));
        int rows = contractDailyStatsMapper.insertSnapshotOfRange(from, today);
        log.info("[ContractDailyStats] 快照完成，窗口 [{}, {})，新增 {} 行", from, today, rows);
        return rows;
    }

}
