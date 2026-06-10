package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.LoginLogDO;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface LoginLogMapper extends BaseMapperX<LoginLogDO> {

    @Select("SELECT DISTINCT user_ip FROM system_login_log " +
            "WHERE user_id = #{userId} AND user_ip IS NOT NULL AND user_ip != '' AND deleted = 0")
    List<String> selectDistinctIpsByUserId(Long userId);

    /** 近 N 天内：IP → 用户关联（用于全局诊断） */
    @Select("SELECT user_ip AS ip, user_id AS userId, username, " +
            "  MAX(create_time) AS lastSeen, MIN(create_time) AS firstSeen " +
            "FROM system_login_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "  AND user_ip IS NOT NULL AND user_ip != '' AND deleted = 0 " +
            "GROUP BY user_ip, user_id, username")
    List<Map<String, Object>> selectIpUserRefs(@Param("days") int days);

    /** 近 30 天用户登录统计 */
    @Select("SELECT user_id AS userId, username, " +
            "  COUNT(*) AS loginCount, " +
            "  SUM(CASE WHEN result != 0 THEN 1 ELSE 0 END) AS failCount, " +
            "  COUNT(DISTINCT user_ip) AS distinctIpCount, " +
            "  MAX(create_time) AS lastLoginTime " +
            "FROM system_login_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) AND deleted = 0 " +
            "GROUP BY user_id, username")
    List<Map<String, Object>> selectUserLoginStats();

    /** 近 1 小时每用户登录次数 */
    @Select("SELECT user_id AS userId, COUNT(*) AS loginCountLastHour " +
            "FROM system_login_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR) AND deleted = 0 " +
            "GROUP BY user_id")
    List<Map<String, Object>> selectUserLoginCountLastHour();

    /** 某用户最近一条登录记录的 IP */
    @Select("SELECT user_ip FROM system_login_log " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY create_time DESC LIMIT 1")
    String selectLastLoginIp(@Param("userId") Long userId);

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
