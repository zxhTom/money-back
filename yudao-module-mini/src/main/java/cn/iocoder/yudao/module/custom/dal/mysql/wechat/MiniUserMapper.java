package cn.iocoder.yudao.module.custom.dal.mysql.wechat;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MiniUserMapper extends BaseMapperX<MiniUserDo> {
    Integer finishMiniUser(@Param("miniUser") MiniUserDo miniUser);
}
