package cn.iocoder.yudao.module.custom.service.dashboard;

import cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo.*;

public interface DashboardService {
    DashboardOverviewVO getOverview();
    UserTrendVO         getUserTrend();
    PaymentStatsVO      getPaymentStats();
    FaceAuthStatsVO     getFaceAuthStats();
    ContractStatsVO     getContractStats();
}
