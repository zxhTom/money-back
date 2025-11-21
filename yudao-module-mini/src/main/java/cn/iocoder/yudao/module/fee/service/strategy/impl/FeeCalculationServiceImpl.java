package cn.iocoder.yudao.module.fee.service.strategy.impl;

import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.FeeCalculationResult;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import cn.iocoder.yudao.module.fee.dal.mysql.strategy.StrategyMapper;
import cn.iocoder.yudao.module.fee.service.strategy.FeeCalculationService;
import cn.iocoder.yudao.module.fee.service.strategy.FeeStrategyFactory;
import cn.iocoder.yudao.module.fee.service.strategy.FeeStrategyInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FeeCalculationServiceImpl implements FeeCalculationService {

    @Autowired
    private FeeStrategyFactory strategyFactory;

    @Autowired
    private StrategyMapper strategyMapper;

    /**
     * 计算费用 - 基于内存策略
     */
    @Override
    public FeeCalculationResult calculateFee(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额必须大于等于0");
        }

        FeeStrategyInterface strategy = strategyFactory.getStrategy(amount);
        BigDecimal fee = strategy.calculateFee(amount);

        FeeCalculationResult result = new FeeCalculationResult();
        result.setAmount(amount);
        result.setFee(fee.multiply(BigDecimal.valueOf(100)));
        result.setTotalAmount(amount.add(fee));
        result.setStrategyDescription(strategy.getDescription());

        log.info("费用计算完成: 金额={}, 费用={}, 总计={}", amount, fee, result.getTotalAmount());
        return result;
    }

    /**
     * 计算费用 - 基于数据库配置
     */
    @Override
    public FeeCalculationResult calculateFeeFromDB(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额必须大于等于0");
        }

        // 从数据库查询适用的策略
        StrategyDO strategy = strategyMapper.findApplicableStrategy(amount);
        if (strategy == null) {
            throw new RuntimeException("没有找到适用的收费策略，金额: " + amount);
        }

        BigDecimal fee = strategy.getFee();

        FeeCalculationResult result = new FeeCalculationResult();
        result.setAmount(amount);
        result.setFee(fee.multiply(BigDecimal.valueOf(100)));
        result.setTotalAmount(amount.add(fee));
        result.setStrategyDescription(String.format("%s-%s元收费%s元",
                strategy.getMinAmount(), strategy.getMaxAmount(), fee));
        result.setStrategyId(strategy.getId());

        log.info("基于DB费用计算完成: 金额={}, 费用={}, 策略ID={}", amount, fee, strategy.getId());
        return result;
    }

    /**
     * 批量计算费用
     */
    @Override
    public List<FeeCalculationResult> batchCalculate(List<BigDecimal> amounts) {
        return amounts.stream()
                .map(this::calculateFee)
                .collect(Collectors.toList());
    }
}
