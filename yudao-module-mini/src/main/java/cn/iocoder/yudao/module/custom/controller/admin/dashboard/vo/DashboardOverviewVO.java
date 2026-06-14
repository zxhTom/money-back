package cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardOverviewVO {
    // 用户
    private Long totalUsers;
    private Long todayNewUsers;
    private Long verifiedUsers;
    private Long activeUsers7d;
    private Long activeUsers30d;
    // 合同
    private Long totalContracts;
    private Long todayNewContracts;
    private Long overdueContracts;
    private BigDecimal totalContractAmount;
    // 支付
    private Long totalPaySuccess;
    private BigDecimal todayPayAmount;
    private BigDecimal monthPayAmount;
}
