package cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentStatsVO {
    private Long        totalOrders;
    private Long        successOrders;
    private Long        waitingOrders;
    private Long        refundOrders;
    private Long        closedOrders;
    private BigDecimal  successRate;
    private BigDecimal  totalSuccessAmount;
    private List<String>     trendDates;
    private List<Long>       trendSuccessCounts;
    private List<BigDecimal> trendSuccessAmounts;
}
