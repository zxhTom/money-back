package cn.iocoder.yudao.module.custom.service.dashboard;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo.*;
import cn.iocoder.yudao.module.custom.dal.mysql.dashboard.DashboardMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.stats.ContractDailyStatsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;
    @Resource
    private ContractDailyStatsMapper contractDailyStatsMapper;

    /**
     * 不计入金额走势的状态，默认只排除 7=撤销。
     * 状态含义：1待确认 2待收款 3已还款 4已逾期 5已失效 6拒签 7撤销。
     * 做成可配是因为"哪些算正常流程"属于业务口径，改口径不该动代码；
     * 而且快照按状态分行存，改这里只影响读取，历史数据不会丢。
     */
    @Value("${yudao.dashboard.trend-exclude-statuses:7}")
    private String trendExcludeStatusesRaw;

    /** 与 YudaoTenantAutoConfiguration 的开关保持一致；关闭时不做租户过滤 */
    @Value("${yudao.tenant.enable:true}")
    private boolean tenantEnabled;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardOverviewVO getOverview() {
        return dashboardMapper.selectOverview(currentTenantId());
    }

    @Override
    public UserTrendVO getUserTrend() {
        List<String> last30 = buildLast30Days();

        Map<String, Long> newMap = dashboardMapper.selectNewUserTrend().stream()
                .collect(Collectors.toMap(DashboardMapper.TrendPoint::getDate, DashboardMapper.TrendPoint::getCount));
        Map<String, Long> dauMap = dashboardMapper.selectDauTrend().stream()
                .collect(Collectors.toMap(DashboardMapper.TrendPoint::getDate, DashboardMapper.TrendPoint::getCount));

        List<Long> newCounts = last30.stream().map(d -> newMap.getOrDefault(d, 0L)).collect(Collectors.toList());
        List<Long> dauCounts = last30.stream().map(d -> dauMap.getOrDefault(d, 0L)).collect(Collectors.toList());

        // 24 小时登录分布，下标 0-23
        Map<Integer, Long> hourMap = dashboardMapper.selectLoginHourDist().stream()
                .collect(Collectors.toMap(DashboardMapper.HourCount::getHour, DashboardMapper.HourCount::getCount));
        List<Long> hourDist = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourDist.add(hourMap.getOrDefault(i, 0L));
        }

        UserTrendVO vo = new UserTrendVO();
        vo.setDates(last30);
        vo.setNewUserCounts(newCounts);
        vo.setDauCounts(dauCounts);
        vo.setLoginHourDist(hourDist);
        return vo;
    }

    @Override
    public PaymentStatsVO getPaymentStats() {
        DashboardMapper.PaySummary summary = dashboardMapper.selectPaySummary();
        List<String> last14 = buildLastNDays(14);

        Map<String, DashboardMapper.PayTrendPoint> trendMap = dashboardMapper.selectPayTrend().stream()
                .collect(Collectors.toMap(DashboardMapper.PayTrendPoint::getDate, p -> p));

        List<Long>       counts  = last14.stream().map(d -> trendMap.containsKey(d) ? trendMap.get(d).getSuccessCount()  : 0L).collect(Collectors.toList());
        List<BigDecimal> amounts = last14.stream().map(d -> trendMap.containsKey(d) ? trendMap.get(d).getSuccessAmount() : BigDecimal.ZERO).collect(Collectors.toList());

        PaymentStatsVO vo = new PaymentStatsVO();
        vo.setTotalOrders(summary.getTotalOrders());
        vo.setSuccessOrders(summary.getSuccessOrders());
        vo.setWaitingOrders(summary.getWaitingOrders());
        vo.setRefundOrders(summary.getRefundOrders());
        vo.setClosedOrders(summary.getClosedOrders());
        vo.setTotalSuccessAmount(summary.getTotalSuccessAmount());

        long total = summary.getTotalOrders() != null ? summary.getTotalOrders() : 0;
        long success = summary.getSuccessOrders() != null ? summary.getSuccessOrders() : 0;
        vo.setSuccessRate(total > 0
                ? BigDecimal.valueOf(success * 100.0 / total).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        vo.setTrendDates(last14);
        vo.setTrendSuccessCounts(counts);
        vo.setTrendSuccessAmounts(amounts);
        return vo;
    }

    @Override
    public FaceAuthStatsVO getFaceAuthStats() {
        return dashboardMapper.selectFaceAuthDist();
    }

    @Override
    public ContractStatsVO getContractStats() {
        DashboardMapper.ContractSummary summary = dashboardMapper.selectContractSummary();
        List<String> last30 = buildLast30Days();

        // 历史走势一律读快照，不再实时算 live 表——合同被软删/回收/归档/租户过滤掉时，
        // 实时算会让"过去那一天"的金额跟着缩水，这正是走势看着越往前越少的原因。
        Map<String, DashboardMapper.ContractTrendPoint> trendMap = loadTrendFromSnapshot(last30);

        List<Long>       counts  = last30.stream().map(d -> trendMap.containsKey(d) ? trendMap.get(d).getCount()  : 0L).collect(Collectors.toList());
        List<BigDecimal> amounts = last30.stream().map(d -> trendMap.containsKey(d) ? trendMap.get(d).getAmount() : BigDecimal.ZERO).collect(Collectors.toList());

        ContractStatsVO vo = new ContractStatsVO();
        vo.setOverdueContracts(summary.getOverdueContracts());
        vo.setOverdueAmount(summary.getOverdueAmount());
        vo.setStatusDistribution(dashboardMapper.selectContractStatusDist());
        vo.setTrendDates(last30);
        vo.setTrendCounts(counts);
        vo.setTrendAmounts(amounts);
        return vo;
    }

    /**
     * 近 30 天走势：优先读快照（不可变），缺快照的日期回落到实时表。
     *
     * 回落是必需的，不只是为了"今天"：
     *   - 每天 00:00~00:30 之间，昨天还没被 Job 归档；
     *   - 快照任务失败或被关掉的那几天。
     * 没有回落这些天会直接显示 0，比原来的 bug 更糟。
     */
    private Map<String, DashboardMapper.ContractTrendPoint> loadTrendFromSnapshot(List<String> dates) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(dates.size() - 1L);
        Long tenantId = currentTenantId();
        List<Integer> exclude = parseExcludeStatuses();

        // 先铺实时值（覆盖整个窗口，含今天），再用快照覆盖掉已归档的日期。
        // 快照优先：它才是不会随合同删除/归档而变化的那份数据。
        Map<String, DashboardMapper.ContractTrendPoint> map = new HashMap<>();
        for (DashboardMapper.ContractTrendPoint p : contractDailyStatsMapper.selectLiveTrend(
                tenantId, from, today.plusDays(1), exclude)) {
            map.put(p.getDate(), p);
        }
        for (DashboardMapper.ContractTrendPoint p : contractDailyStatsMapper.selectTrendFromSnapshot(
                tenantId, from, today.minusDays(1), exclude)) {
            map.put(p.getDate(), p);
        }
        return map;
    }

    /**
     * 多租户开启时必须拿到租户号，拿不到就抛错（fail closed）——
     * 快照表用 @TenantIgnore 退出了框架的自动过滤，这里再放过就会把所有租户的金额汇总到一张图里。
     *
     * 但关闭多租户时必须返回 null：yudao 的 YudaoTenantAutoConfiguration 挂着
     * @ConditionalOnProperty(yudao.tenant.enable)，关掉后连 TenantContextWebFilter 都不注册，
     * 线程里永远没有租户上下文。此时若仍调 getRequiredTenantId() 会直接抛 NPE 把大盘打成 500。
     * 单租户模式下本来也没有租户维度，不加这个条件才是对的（SQL 侧有判空分支）。
     */
    private Long currentTenantId() {
        if (!tenantEnabled) {
            return null;
        }
        return TenantContextHolder.getRequiredTenantId();
    }

    private List<Integer> parseExcludeStatuses() {
        List<Integer> list = new ArrayList<>();
        if (trendExcludeStatusesRaw == null || trendExcludeStatusesRaw.trim().isEmpty()) {
            return list;
        }
        for (String part : trendExcludeStatusesRaw.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                list.add(Integer.parseInt(t));
            } catch (NumberFormatException ignored) {
                // 配错的项跳过，不影响其它状态
            }
        }
        return list;
    }

    private List<String> buildLast30Days() {
        return buildLastNDays(30);
    }

    private List<String> buildLastNDays(int n) {
        List<String> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = n - 1; i >= 0; i--) {
            dates.add(today.minusDays(i).format(DATE_FMT));
        }
        return dates;
    }
}
