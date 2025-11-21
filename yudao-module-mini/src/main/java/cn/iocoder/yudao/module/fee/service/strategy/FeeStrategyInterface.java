package cn.iocoder.yudao.module.fee.service.strategy;

import java.math.BigDecimal;

public interface FeeStrategyInterface {

    /**
     * 是否适用于该金额
     */
    boolean supports(BigDecimal amount);

    /**
     * 计算费用
     */
    BigDecimal calculateFee(BigDecimal amount);

    /**
     * 获取策略描述
     */
    String getDescription();
}
