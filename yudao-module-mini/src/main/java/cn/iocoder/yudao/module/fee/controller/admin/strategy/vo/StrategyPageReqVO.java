package cn.iocoder.yudao.module.fee.controller.admin.strategy.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 费用策略分页 Request VO")
@Data
public class StrategyPageReqVO extends PageParam {

    @Schema(description = "最小金额")
    private BigDecimal minAmount;

    @Schema(description = "最大金额")
    private BigDecimal maxAmount;

    @Schema(description = "收费金额")
    private BigDecimal fee;

    @Schema(description = "策略顺序")
    private Integer strategyOrder;

    @Schema(description = "状态：1启用，0禁用", example = "1")
    private Integer status;

}