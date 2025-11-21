package cn.iocoder.yudao.module.fee.dal.mysql.strategy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.StrategyPageReqVO;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用策略 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface StrategyMapper extends BaseMapperX<StrategyDO> {

    default PageResult<StrategyDO> selectPage(StrategyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StrategyDO>()
                .eqIfPresent(StrategyDO::getMinAmount, reqVO.getMinAmount())
                .eqIfPresent(StrategyDO::getMaxAmount, reqVO.getMaxAmount())
                .eqIfPresent(StrategyDO::getFee, reqVO.getFee())
                .eqIfPresent(StrategyDO::getStrategyOrder, reqVO.getStrategyOrder())
                .eqIfPresent(StrategyDO::getStatus, reqVO.getStatus())
                .orderByDesc(StrategyDO::getId));
    }
 /**
     * 查找适用于指定金额的策略
     */
    @Select("SELECT * FROM fee_strategy " +
            "WHERE min_amount <= #{amount} AND max_amount > #{amount} " +
            "AND status = 1 " +
            "ORDER BY strategy_order LIMIT 1")
    StrategyDO findApplicableStrategy(@Param("amount") BigDecimal amount);

    /**
     * 获取所有启用的策略（按顺序）
     */
    @Select("SELECT * FROM fee_strategy WHERE status = 1 ORDER BY strategy_order")
    List<StrategyDO> findAllEnabledStrategies();

    /**
     * 根据金额范围查找策略
     */
    @Select("SELECT * FROM fee_strategy " +
            "WHERE min_amount = #{minAmount} AND max_amount = #{maxAmount} " +
            "AND status = 1")
    StrategyDO findByAmountRange(@Param("minAmount") BigDecimal minAmount,
                                 @Param("maxAmount") BigDecimal maxAmount);
}