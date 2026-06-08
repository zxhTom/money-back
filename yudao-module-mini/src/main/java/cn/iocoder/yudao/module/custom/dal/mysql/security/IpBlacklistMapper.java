package cn.iocoder.yudao.module.custom.dal.mysql.security;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.IpBlacklistDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IpBlacklistMapper extends BaseMapperX<IpBlacklistDO> {

    @Select("SELECT ip FROM custom_ip_blacklist WHERE expire_time IS NULL OR expire_time > NOW()")
    List<String> selectAllActiveIps();

    @Select("SELECT COUNT(*) FROM custom_ip_blacklist WHERE ip = #{ip} AND (expire_time IS NULL OR expire_time > NOW())")
    int countActiveByIp(String ip);
}
