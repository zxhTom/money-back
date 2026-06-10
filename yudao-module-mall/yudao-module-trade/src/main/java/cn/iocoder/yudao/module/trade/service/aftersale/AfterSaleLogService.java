package cn.iocoder.yudao.module.trade.service.aftersale;


import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import cn.iocoder.yudao.module.trade.service.aftersale.bo.AfterSaleLogCreateReqBO;

import java.util.List;

/**
 * 交易售后日志 Service 接口
 *
 * @author 陈賝
 * @since 2023/6/12 14:18
 */
public interface AfterSaleLogService {

    /**
     * 创建售后日志
     *
     * @param createReqBO 日志记录
     * @author 陈賝
     * @since 2023/6/12 14:18
     */
    void createAfterSaleLog(AfterSaleLogCreateReqBO createReqBO);

    /**
     * 获取售后日志
     *
     * @param afterSaleId 售后编号
     * @return 售后日志
     */
    List<AfterSaleLogDO> getAfterSaleLogList(Long afterSaleId);

    /**
     * 清理超过指定天数的售后日志
     *
     * @param exceedDay   超过天数
     * @param deleteLimit 单次删除条数
     * @return 删除总条数
     */
    Integer cleanAfterSaleLog(Integer exceedDay, Integer deleteLimit);

}
