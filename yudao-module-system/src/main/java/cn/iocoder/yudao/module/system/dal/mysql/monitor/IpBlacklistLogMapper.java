package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IpBlacklistLogMapper extends BaseMapperX<IpBlacklistLogDO> {

    @Select("SELECT * FROM custom_ip_blacklist_log WHERE ip = #{ip} ORDER BY id DESC")
    List<IpBlacklistLogDO> selectByIp(String ip);
}
