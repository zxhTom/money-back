package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StaticsContractPeriodRespVO {
// 当日统计
    private Long todayCount;
    private BigDecimal todayRevenue;

    // 当月统计
    private Long monthCount;
    private BigDecimal monthRevenue;

    // 累计统计
    private Long totalCount;
    private BigDecimal totalRevenue;

    // 平均值
    private BigDecimal avgSalary;
    private BigDecimal todayAvgSalary;

    // 日期信息
    private LocalDate statDate;
    private LocalDate monthStartDate;
}
