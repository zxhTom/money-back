package cn.iocoder.yudao.module.system.dal.mysql.risk;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskAbnormalReportDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常用户风控报告 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface RiskAbnormalReportMapper extends BaseMapperX<RiskAbnormalReportDO> {
}
