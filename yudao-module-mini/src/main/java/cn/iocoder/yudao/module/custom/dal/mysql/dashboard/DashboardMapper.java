package cn.iocoder.yudao.module.custom.dal.mysql.dashboard;

import cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo.ContractStatsVO;
import cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo.DashboardOverviewVO;
import cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo.FaceAuthStatsVO;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DashboardMapper {

    DashboardOverviewVO selectOverview();

    List<TrendPoint> selectNewUserTrend();

    List<TrendPoint> selectDauTrend();

    List<HourCount> selectLoginHourDist();

    PaySummary selectPaySummary();

    List<PayTrendPoint> selectPayTrend();

    FaceAuthStatsVO selectFaceAuthDist();

    ContractSummary selectContractSummary();

    List<ContractStatsVO.StatusItem> selectContractStatusDist();

    List<ContractTrendPoint> selectContractAmountTrend();

    // ── 内部查询结果 DTO ───────────────────────────────────────

    @Data
    class TrendPoint {
        private String date;
        private Long   count;
    }

    @Data
    class HourCount {
        private Integer hour;
        private Long    count;
    }

    @Data
    class PaySummary {
        private Long       totalOrders;
        private Long       successOrders;
        private Long       waitingOrders;
        private Long       refundOrders;
        private Long       closedOrders;
        private BigDecimal totalSuccessAmount;
    }

    @Data
    class PayTrendPoint {
        private String     date;
        private Long       successCount;
        private BigDecimal successAmount;
    }

    @Data
    class ContractSummary {
        private Long       overdueContracts;
        private BigDecimal overdueAmount;
    }

    @Data
    class ContractTrendPoint {
        private String     date;
        private Long       count;
        private BigDecimal amount;
    }
}
