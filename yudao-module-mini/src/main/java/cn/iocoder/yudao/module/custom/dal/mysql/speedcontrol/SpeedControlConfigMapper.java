package cn.iocoder.yudao.module.custom.dal.mysql.speedcontrol;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpeedControlConfigMapper extends BaseMapperX<SpeedControlConfigDO> {

    /** 全局固定只有一行，约定 id=1 */
    default SpeedControlConfigDO selectTheOne() {
        return selectById(1L);
    }

}
