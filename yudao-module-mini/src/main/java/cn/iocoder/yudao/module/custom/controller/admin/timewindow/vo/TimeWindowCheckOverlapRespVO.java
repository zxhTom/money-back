package cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 时间窗口重合校验 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindowCheckOverlapRespVO {

    @Schema(description = "是否允许保存（重合时长 > 阈值时为 false）")
    private Boolean allowed;
    @Schema(description = "与已有激活窗口的最大重合时长（小时）")
    private Double overlapHours;
    @Schema(description = "提示文案，用于前端展示/警告")
    private String message;
    @Schema(description = "当前使用的重合阈值（小时）")
    private Double overlapThresholdHours;
}
