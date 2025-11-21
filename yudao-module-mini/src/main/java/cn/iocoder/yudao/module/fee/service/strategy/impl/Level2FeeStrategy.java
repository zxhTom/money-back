package cn.iocoder.yudao.module.fee.service.strategy.impl;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Level2FeeStrategy extends AbstractFeeStrategy {
    public Level2FeeStrategy() {
        super(BigDecimal.ZERO, new BigDecimal("5000"), new BigDecimal("9.8"));
    }
}
