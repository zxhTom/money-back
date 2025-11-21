package cn.iocoder.yudao.module.fee.dal.mysql.strategy;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.*;

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

}