package cn.iocoder.yudao.module.fee.controller.admin.strategy.vo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class FeeCalculateRequest {
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0", message = "金额必须大于等于0")
    private BigDecimal amount;

    private Boolean useDatabase = false; // 是否使用数据库配置
}
