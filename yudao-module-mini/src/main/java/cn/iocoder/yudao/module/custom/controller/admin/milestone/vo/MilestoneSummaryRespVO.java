package cn.iocoder.yudao.module.custom.controller.admin.milestone.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 节点看板概览统计 Response VO")
@Data
@Builder
public class MilestoneSummaryRespVO {

    @Schema(description = "已被预警/即将到期总数（30天内或已过期且未完成）")
    private Integer alertCount;

    @Schema(description = "已过期数")
    private Integer expiredCount;

    @Schema(description = "紧急预警数（<=15天且未完成）")
    private Integer urgentCount;

    @Schema(description = "待处理事项数")
    private Integer pendingCount;

    @Schema(description = "已完成事项数")
    private Integer completedCount;

    @Schema(description = "近期即将到期/需准备的列表（按到期日升序）")
    private List<MilestoneRespVO> upcomingList;
}
