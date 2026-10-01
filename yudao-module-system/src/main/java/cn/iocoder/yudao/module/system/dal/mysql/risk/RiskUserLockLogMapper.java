package cn.iocoder.yudao.module.system.dal.mysql.risk;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户锁定记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface RiskUserLockLogMapper extends BaseMapperX<RiskUserLockLogDO> {
}
