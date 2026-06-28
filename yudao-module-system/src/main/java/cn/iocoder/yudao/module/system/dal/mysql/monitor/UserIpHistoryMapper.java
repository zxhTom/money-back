package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.UserIpStatVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpHistoryDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserIpHistoryMapper extends BaseMapperX<UserIpHistoryDO> {

    @Insert("INSERT INTO custom_user_ip_history (user_id, ip, first_seen, last_seen, count) " +
            "VALUES (#{userId}, #{ip}, NOW(), NOW(), 1) " +
            "ON DUPLICATE KEY UPDATE last_seen = NOW(), count = count + 1")
    void upsert(@Param("userId") Long userId, @Param("ip") String ip);

    @Select("SELECT * FROM custom_user_ip_history WHERE user_id = #{userId} ORDER BY last_seen DESC")
    List<UserIpHistoryDO> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT h.user_id AS userId, COUNT(DISTINCT h.ip) AS ipCount, u.username " +
            "FROM custom_user_ip_history h " +
            "LEFT JOIN system_users u ON u.id = h.user_id AND u.deleted = 0 " +
            "GROUP BY h.user_id, u.username ORDER BY ipCount DESC LIMIT #{limit}")
    List<UserIpStatVO> selectTopUsersByIpCount(@Param("limit") int limit);
}
