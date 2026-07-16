package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IpBlacklistMapper extends BaseMapperX<IpBlacklistDO> {

    // 用应用侧传入的 now（LocalDateTime）比较到期时间，而不是数据库 NOW()：
    // 数据库服务器时区若与应用不一致（如 DB=UTC、应用=Asia/Shanghai），DB NOW() 会差几小时，
    // 导致封禁"到期了却还封着"。改由应用传时间，判定与写入(Redis TTL/expireTime)口径一致，跟 DB 时区无关。
    @Select("SELECT ip FROM custom_ip_blacklist WHERE status = 0 AND (expire_time IS NULL OR expire_time > #{now})")
    List<String> selectAllActiveIps(@Param("now") LocalDateTime now);

    @Select("SELECT COUNT(*) FROM custom_ip_blacklist WHERE ip = #{ip} AND status = 0 AND (expire_time IS NULL OR expire_time > #{now})")
    int countActiveByIp(@Param("ip") String ip, @Param("now") LocalDateTime now);

    /** 曾经/现在被封过的所有 IP（含已解封）——行不删除，status=1 即已解封，故全表 distinct 即"曾封禁" */
    @Select("SELECT DISTINCT ip FROM custom_ip_blacklist")
    List<String> selectEverBannedIps();
}
