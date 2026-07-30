package cn.iocoder.yudao.module.custom.dal.mysql.miniconfig;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MiniProgramConfigMapper extends BaseMapperX<MiniProgramConfigDO> {

    /** 全局固定只有一行，约定 id=1 */
    default MiniProgramConfigDO selectTheOne() {
        return selectById(1L);
    }

}
