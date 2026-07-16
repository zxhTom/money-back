package cn.iocoder.yudao.module.custom.dal.mysql.security;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.UaWhitelistDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UaWhitelistMapper extends BaseMapperX<UaWhitelistDO> {

    default List<UaWhitelistDO> selectEnabled() {
        return selectList(new LambdaQueryWrapperX<UaWhitelistDO>()
                .eq(UaWhitelistDO::getEnabled, 1));
    }

}
