package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpAccessLogDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
@InterceptorIgnore(tenantLine = "true") // 安全日志跨租户，忽略租户隔离
public interface IpAccessLogMapper extends BaseMapperX<IpAccessLogDO> {

    /** 按 (ip,reason,分钟) 聚合累加 hitDelta（由内存缓冲区定时批量刷库，降低数据库压力） */
    @Insert("INSERT INTO custom_ip_access_log " +
            "(ip, reason, uri, method, user_agent, user_id, hit_count, stat_day, minute_bucket, create_time, update_time) " +
            "VALUES (#{ip},#{reason},#{uri},#{method},#{userAgent},#{userId},#{hitDelta},#{statDay},#{minuteBucket},#{now},#{now}) " +
            "ON DUPLICATE KEY UPDATE hit_count = hit_count + #{hitDelta}, uri = VALUES(uri), update_time = VALUES(update_time)")
    int upsert(@Param("ip") String ip, @Param("reason") String reason, @Param("uri") String uri,
               @Param("method") String method, @Param("userAgent") String userAgent, @Param("userId") Long userId,
               @Param("statDay") LocalDate statDay, @Param("minuteBucket") LocalDateTime minuteBucket,
               @Param("now") LocalDateTime now, @Param("hitDelta") long hitDelta);

    /** 每日拦截趋势（近 N 天）：当天总命中数 + 去重IP数 */
    @Select("SELECT stat_day AS day, SUM(hit_count) AS hits, COUNT(DISTINCT ip) AS ips " +
            "FROM custom_ip_access_log WHERE stat_day >= #{fromDay} GROUP BY stat_day ORDER BY stat_day")
    List<Map<String, Object>> selectDailyTrend(@Param("fromDay") LocalDate fromDay);

    /** Top 攻击/试探 IP（近 N 天），按总命中数排序 */
    @Select("SELECT ip, SUM(hit_count) AS hits, COUNT(DISTINCT reason) AS reasons, " +
            "MIN(create_time) AS first_seen, MAX(update_time) AS last_seen " +
            "FROM custom_ip_access_log WHERE stat_day >= #{fromDay} " +
            "GROUP BY ip ORDER BY hits DESC LIMIT #{size}")
    List<Map<String, Object>> selectTopIps(@Param("fromDay") LocalDate fromDay, @Param("size") int size);

    /** 某 IP 的每日频率（近 N 天） */
    @Select("SELECT stat_day AS day, SUM(hit_count) AS hits FROM custom_ip_access_log " +
            "WHERE ip = #{ip} AND stat_day >= #{fromDay} GROUP BY stat_day ORDER BY stat_day")
    List<Map<String, Object>> selectDailyByIp(@Param("ip") String ip, @Param("fromDay") LocalDate fromDay);
}
