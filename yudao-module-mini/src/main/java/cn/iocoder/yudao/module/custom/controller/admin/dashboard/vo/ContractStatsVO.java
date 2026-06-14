package cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractStatsVO {
    private Long       overdueContracts;
    private BigDecimal overdueAmount;
    private List<StatusItem>  statusDistribution;
    private List<String>      trendDates;
    private List<Long>        trendCounts;
    private List<BigDecimal>  trendAmounts;

    @Data
    public static class StatusItem {
        private String     name;
        private Long       value;
        private BigDecimal amount;
    }
}
