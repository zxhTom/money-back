package cn.iocoder.yudao.module.fee.service.strategy.impl;

import cn.iocoder.yudao.module.fee.service.strategy.FeeStrategyInterface;

import java.math.BigDecimal;

public abstract class AbstractFeeStrategy implements FeeStrategyInterface {
    protected final BigDecimal minAmount;
    protected final BigDecimal maxAmount;
    protected final BigDecimal fee;

    protected AbstractFeeStrategy(BigDecimal minAmount, BigDecimal maxAmount, BigDecimal fee) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.fee = fee;
    }

    @Override
    public boolean supports(BigDecimal amount) {
        return amount.compareTo(minAmount) >= 0 && amount.compareTo(maxAmount) < 0;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return fee;
    }

    @Override
    public String getDescription() {
        return String.format("%s-%s元收费%s元", minAmount, maxAmount, fee);
    }
}
