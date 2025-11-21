package cn.iocoder.yudao.module.fee.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 费用策略 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StrategyRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19024")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "最小金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("最小金额")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("最大金额")
    private BigDecimal maxAmount;

    @Schema(description = "收费金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("收费金额")
    private BigDecimal fee;

    @Schema(description = "策略顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("策略顺序")
    private Integer strategyOrder;

    @Schema(description = "状态：1启用，0禁用", example = "1")
    @ExcelProperty(value = "状态：1启用，0禁用", converter = DictConvert.class)
    @DictFormat("开关") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

}