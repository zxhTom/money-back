package cn.iocoder.yudao.module.fee.controller.admin.strategy.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeStrategyDTO {
    private Long id;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal fee;
    private Integer strategyOrder;
    private String description;
}
