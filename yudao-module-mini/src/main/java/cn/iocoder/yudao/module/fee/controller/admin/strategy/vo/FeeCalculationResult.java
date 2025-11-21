package cn.iocoder.yudao.module.fee.controller.admin.strategy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeCalculationResult {
    private BigDecimal amount;           // 原始金额
    private BigDecimal fee;              // 费用
    private BigDecimal totalAmount;      // 总金额（原始金额 + 费用）
    private String strategyDescription;  // 策略描述
    private Long strategyId;             // 策略ID（数据库）
    private LocalDateTime calculateTime = LocalDateTime.now();
}
