package cn.iocoder.yudao.module.fee.service.strategy;

import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.FeeCalculationResult;

import java.math.BigDecimal;
import java.util.List;

public interface FeeCalculationService {

    public FeeCalculationResult calculateFee(BigDecimal amount) ;

    /**
     * 计算费用 - 基于数据库配置
     */
    public FeeCalculationResult calculateFeeFromDB(BigDecimal amount) ;

    /**
     * 批量计算费用
     */
    public List<FeeCalculationResult> batchCalculate(List<BigDecimal> amounts) ;
}
