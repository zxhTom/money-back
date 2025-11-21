package cn.iocoder.yudao.module.fee.service.strategy;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.*;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 费用策略 Service 接口
 *
 * @author 芋道源码
 */
public interface StrategyService {

    /**
     * 创建费用策略
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStrategy(@Valid StrategySaveReqVO createReqVO);

    /**
     * 更新费用策略
     *
     * @param updateReqVO 更新信息
     */
    void updateStrategy(@Valid StrategySaveReqVO updateReqVO);

    /**
     * 删除费用策略
     *
     * @param id 编号
     */
    void deleteStrategy(Long id);

    /**
    * 批量删除费用策略
    *
    * @param ids 编号
    */
    void deleteStrategyListByIds(List<Long> ids);

    /**
     * 获得费用策略
     *
     * @param id 编号
     * @return 费用策略
     */
    StrategyDO getStrategy(Long id);

    /**
     * 获得费用策略分页
     *
     * @param pageReqVO 分页查询
     * @return 费用策略分页
     */
    PageResult<StrategyDO> getStrategyPage(StrategyPageReqVO pageReqVO);

}