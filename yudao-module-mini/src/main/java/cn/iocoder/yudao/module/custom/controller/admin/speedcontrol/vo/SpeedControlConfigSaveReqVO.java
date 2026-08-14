package cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 访问速度控制配置 保存 Request VO")
@Data
public class SpeedControlConfigSaveReqVO {

    @Schema(description = "限速总开关")
    private Boolean enabled;

    @Schema(description = "速率 1-100，越大越快，100=不降速", example = "100")
    @DecimalMin(value = "1", message = "速率不能小于 1")
    @DecimalMax(value = "100", message = "速率不能大于 100")
    private BigDecimal rate;

    @Schema(description = "是否启用响应时间范围模式（优先于速率）")
    private Boolean rangeEnabled;

    @Schema(description = "时间范围下限(ms)")
    @Min(value = 0, message = "时间范围下限不能为负")
    private Long minMs;

    @Schema(description = "时间范围上限(ms)")
    @Min(value = 0, message = "时间范围上限不能为负")
    private Long maxMs;

    @Schema(description = "参考区间下界(ms)")
    @Min(value = 1, message = "参考区间下界至少为 1")
    private Long refFastMs;

    @Schema(description = "参考区间上界(ms)")
    @Min(value = 1, message = "参考区间上界至少为 1")
    private Long refSlowMs;

    @Schema(description = "单次注入延迟硬上限(ms)")
    @Min(value = 0, message = "延迟上限不能为负")
    private Long maxDelayMs;

    @Schema(description = "抖动百分比")
    @DecimalMin(value = "0", message = "抖动不能为负")
    @DecimalMax(value = "50", message = "抖动不能超过 50%")
    private BigDecimal jitterPercent;

    @Schema(description = "豁免用户ID列表")
    private List<Long> exemptUserIds;

    @Schema(description = "豁免角色ID列表")
    private List<Long> exemptRoleIds;

}
