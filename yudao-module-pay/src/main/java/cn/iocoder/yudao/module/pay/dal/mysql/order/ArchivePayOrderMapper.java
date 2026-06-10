package cn.iocoder.yudao.module.pay.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单归档库 Mapper，所有操作路由到 archive 数据源
 */
@Mapper
@DS("archive")
public interface ArchivePayOrderMapper extends BaseMapperX<PayOrderDO> {
}
