package cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 时间窗口配置 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindowConfigRespVO {

    @Schema(description = "允许的最大重合时长（小时）", example = "1")
    private Double overlapThresholdHours;
}
