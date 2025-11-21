package cn.iocoder.yudao.module.fee.service.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FeeStrategyFactory {

    @Autowired
    private List<FeeStrategyInterface> strategies;

    /**
     * 根据金额获取合适的策略
     */
    public FeeStrategyInterface getStrategy(BigDecimal amount) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(amount))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("没有找到合适的收费策略，金额: " + amount));
    }

    /**
     * 获取所有策略
     */
    public List<FeeStrategyInterface> getAllStrategies() {
        return strategies;
    }
}
