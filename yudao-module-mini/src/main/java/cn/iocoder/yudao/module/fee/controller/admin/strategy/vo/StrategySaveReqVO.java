package cn.iocoder.yudao.module.fee.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 费用策略新增/修改 Request VO")
@Data
public class StrategySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19024")
    private Long id;

    @Schema(description = "最小金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "最小金额不能为空")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "最大金额不能为空")
    private BigDecimal maxAmount;

    @Schema(description = "收费金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收费金额不能为空")
    private BigDecimal fee;

    @Schema(description = "策略顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "策略顺序不能为空")
    private Integer strategyOrder;

    @Schema(description = "状态：1启用，0禁用", example = "1")
    private Integer status;

}