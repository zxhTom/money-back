package cn.iocoder.yudao.module.custom.service.dashboard;

import cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo.*;
import cn.iocoder.yudao.module.custom.dal.mysql.dashboard.DashboardMapper;
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

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardOverviewVO getOverview() {
        return dashboardMapper.selectOverview();
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

        Map<String, DashboardMapper.ContractTrendPoint> trendMap = dashboardMapper.selectContractAmountTrend().stream()
                .collect(Collectors.toMap(DashboardMapper.ContractTrendPoint::getDate, p -> p));

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
