package cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 时间窗口重合校验 Request VO")
@Data
public class TimeWindowCheckOverlapReqVO {

    @Schema(description = "开始时间（毫秒时间戳）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @Schema(description = "结束时间（毫秒时间戳）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    @Schema(description = "排除的记录 ID（修改时传当前记录 id，新增不传）")
    private Long excludeId;
}
