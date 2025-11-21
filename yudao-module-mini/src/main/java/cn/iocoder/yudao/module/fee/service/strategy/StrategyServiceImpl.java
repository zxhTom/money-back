package cn.iocoder.yudao.module.fee.service.strategy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.StrategyPageReqVO;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.StrategySaveReqVO;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import cn.iocoder.yudao.module.fee.dal.mysql.strategy.StrategyMapper;
import cn.iocoder.yudao.module.fee.enums.StrategyErrorCodeConstants;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 费用策略 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class StrategyServiceImpl implements StrategyService {

    @Resource
    private StrategyMapper strategyMapper;

    @Override
    public Long createStrategy(StrategySaveReqVO createReqVO) {
        // 插入
        StrategyDO strategy = BeanUtils.toBean(createReqVO, StrategyDO.class);
        strategyMapper.insert(strategy);

        // 返回
        return strategy.getId();
    }

    @Override
    public void updateStrategy(StrategySaveReqVO updateReqVO) {
        // 校验存在
        validateStrategyExists(updateReqVO.getId());
        // 更新
        StrategyDO updateObj = BeanUtils.toBean(updateReqVO, StrategyDO.class);
        strategyMapper.updateById(updateObj);
    }

    @Override
    public void deleteStrategy(Long id) {
        // 校验存在
        validateStrategyExists(id);
        // 删除
        strategyMapper.deleteById(id);
    }

    @Override
        public void deleteStrategyListByIds(List<Long> ids) {
        // 删除
        strategyMapper.deleteByIds(ids);
        }


    private void validateStrategyExists(Long id) {
        if (strategyMapper.selectById(id) == null) {
            throw exception(StrategyErrorCodeConstants.STRATEGY_NOT_EXISTS);
        }
    }

    @Override
    public StrategyDO getStrategy(Long id) {
        return strategyMapper.selectById(id);
    }

    @Override
    public PageResult<StrategyDO> getStrategyPage(StrategyPageReqVO pageReqVO) {
        return strategyMapper.selectPage(pageReqVO);
    }

}