package cn.iocoder.yudao.module.custom.dal.mysql.speedcontrol;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Mapper
public interface SpeedControlConfigMapper extends BaseMapperX<SpeedControlConfigDO> {
    default SpeedControlConfigDO selectTheOne() {
        return selectOne(new QueryWrapper<SpeedControlConfigDO>().last("LIMIT 1"));
    }
}
