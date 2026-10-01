package cn.iocoder.yudao.module.system.dal.mysql.risk;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户锁定记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface RiskUserLockLogMapper extends BaseMapperX<RiskUserLockLogDO> {

    default RiskUserLockLogDO selectLatestByUserIdAndLockType(Long userId, String lockType) {
        return selectOne(new LambdaQueryWrapperX<RiskUserLockLogDO>()
                .eq(RiskUserLockLogDO::getUserId, userId)
                .eq(RiskUserLockLogDO::getLockType, lockType)
                .orderByDesc(RiskUserLockLogDO::getId)
                .last("LIMIT 1"));
    }

}
