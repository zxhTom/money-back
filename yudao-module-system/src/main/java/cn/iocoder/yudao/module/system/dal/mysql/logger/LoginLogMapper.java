package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.LoginLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpUserRefDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.LoginUserStatsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpStatDO;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface LoginLogMapper extends BaseMapperX<LoginLogDO> {

    @Select("SELECT DISTINCT user_ip FROM system_login_log " +
            "WHERE user_id = #{userId} AND user_ip IS NOT NULL AND user_ip != '' AND deleted = 0")
    List<String> selectDistinctIpsByUserId(Long userId);

    /** 近 N 天内：IP → 用户关联（用于全局诊断，通过 system_users 归属限定租户与 ADMIN 用户类型） */
    @Select("SELECT l.user_ip AS ip, l.user_id AS userId, l.username, " +
            "  MAX(l.create_time) AS lastSeen, MIN(l.create_time) AS firstSeen " +
            "FROM system_login_log l " +
            "INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId} " +
            "WHERE l.user_type = 2 " +
            "  AND l.create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "  AND l.user_ip IS NOT NULL AND l.user_ip != '' AND l.deleted = 0 " +
            "GROUP BY l.user_ip, l.user_id, l.username")
    List<IpUserRefDO> selectDiagnoseIpUserRefs(@Param("tenantId") Long tenantId, @Param("days") int days);

    /** 近 30 天用户登录统计（用于全局诊断，通过 system_users 归属限定租户与 ADMIN 用户类型） */
    @Select("SELECT l.user_id AS userId, l.username, " +
            "  COUNT(*) AS loginCount, " +
            "  SUM(CASE WHEN l.result != 0 THEN 1 ELSE 0 END) AS failCount, " +
            "  COUNT(DISTINCT l.user_ip) AS distinctIpCount, " +
            "  MAX(l.create_time) AS lastLoginTime " +
            "FROM system_login_log l " +
            "INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId} " +
            "WHERE l.user_type = 2 " +
            "  AND l.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) AND l.deleted = 0 " +
            "GROUP BY l.user_id, l.username")
    List<LoginUserStatsDO> selectDiagnoseUserLoginStats(@Param("tenantId") Long tenantId);

    /** 近 1 小时每用户登录次数（用于全局诊断，通过 system_users 归属限定租户与 ADMIN 用户类型） */
    @Select("SELECT l.user_id AS userId, COUNT(*) AS loginCountLastHour " +
            "FROM system_login_log l " +
            "INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId} " +
            "WHERE l.user_type = 2 " +
            "  AND l.create_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR) AND l.deleted = 0 " +
            "GROUP BY l.user_id")
    List<Map<String, Object>> selectDiagnoseUserLoginCountLastHour(@Param("tenantId") Long tenantId);

    /** 某用户最近一条登录记录的 IP（用于全局诊断，通过 system_users 显式限定租户与 ADMIN 用户类型） */
    @Select("SELECT l.user_ip FROM system_login_log l " +
            "INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId} " +
            "WHERE l.user_id = #{userId} AND l.user_type = 2 AND l.deleted = 0 " +
            "ORDER BY l.create_time DESC LIMIT 1")
    String selectDiagnoseLastLoginIp(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /** 某用户登录使用过的所有不同 IP（用于全局诊断，通过 system_users 显式限定租户与 ADMIN 用户类型） */
    @Select("SELECT DISTINCT l.user_ip FROM system_login_log l " +
            "INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId} " +
            "WHERE l.user_id = #{userId} AND l.user_type = 2 " +
            "  AND l.user_ip IS NOT NULL AND l.user_ip != '' AND l.deleted = 0")
    List<String> selectDiagnoseDistinctIpsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /** 某用户的 IP 使用统计（用于 IP 记录，与诊断同源） */
    @Select("SELECT user_ip AS ip, MIN(create_time) AS firstSeen, MAX(create_time) AS lastSeen, COUNT(*) AS count " +
            "FROM system_login_log " +
            "WHERE user_id = #{userId} AND user_ip IS NOT NULL AND user_ip != '' AND deleted = 0 " +
            "GROUP BY user_ip")
    List<UserIpStatDO> selectIpStatsByUserId(@Param("userId") Long userId);

    @Select("SELECT user_ip AS ip, user_id AS userId, username, " +
            "  MAX(create_time) AS lastSeen, MIN(create_time) AS firstSeen " +
            "FROM system_login_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "  AND user_ip IS NOT NULL AND user_ip != '' AND deleted = 0 " +
            "GROUP BY user_ip, user_id, username")
    List<IpUserRefDO> selectIpUserRefs(@Param("days") int days);

    @Delete("DELETE FROM system_login_log WHERE create_time < #{createTime} LIMIT #{limit}")
    Integer deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit") Integer limit);

    default PageResult<LoginLogDO> selectPage(LoginLogPageReqVO reqVO) {
        LambdaQueryWrapperX<LoginLogDO> query = new LambdaQueryWrapperX<LoginLogDO>()
                .likeIfPresent(LoginLogDO::getUserIp, reqVO.getUserIp())
                .likeIfPresent(LoginLogDO::getUsername, reqVO.getUsername())
                .betweenIfPresent(LoginLogDO::getCreateTime, reqVO.getCreateTime());
        if (Boolean.TRUE.equals(reqVO.getStatus())) {
            query.eq(LoginLogDO::getResult, LoginResultEnum.SUCCESS.getResult());
        } else if (Boolean.FALSE.equals(reqVO.getStatus())) {
            query.gt(LoginLogDO::getResult, LoginResultEnum.SUCCESS.getResult());
        }
        query.orderByDesc(LoginLogDO::getId); // 降序
        return selectPage(reqVO, query);
    }

}
