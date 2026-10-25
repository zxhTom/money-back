package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpUserRefDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpStatDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface DataAccessLogMapper extends BaseMapperX<DataAccessLogDO> {

    @Select("SELECT DISTINCT client_ip FROM custom_data_access_log " +
            "WHERE user_id = #{userId} AND deleted = 0 AND client_ip IS NOT NULL AND client_ip != ''")
    List<String> selectDistinctIpsByUserId(Long userId);

    /** 某用户的 IP 使用统计（用于 IP 记录，与诊断同源） */
    @Select("SELECT client_ip AS ip, MIN(access_time) AS firstSeen, MAX(access_time) AS lastSeen, COUNT(*) AS count " +
            "FROM custom_data_access_log " +
            "WHERE user_id = #{userId} AND client_ip IS NOT NULL AND client_ip != '' AND deleted = 0 " +
            "GROUP BY client_ip")
    List<UserIpStatDO> selectIpStatsByUserId(@Param("userId") Long userId);

    /** 近 N 天内：IP → 用户关联（用于全局诊断，显式限定租户） */
    @Select("SELECT d.client_ip AS ip, d.user_id AS userId, d.username, " +
            "  MAX(d.access_time) AS lastSeen, MIN(d.access_time) AS firstSeen " +
            "FROM custom_data_access_log d " +
            "INNER JOIN system_users u ON u.id = d.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId} " +
            "WHERE d.access_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "  AND d.client_ip IS NOT NULL AND d.client_ip != '' AND d.deleted = 0 " +
            "GROUP BY d.client_ip, d.user_id, d.username")
    List<IpUserRefDO> selectDiagnoseIpUserRefs(@Param("tenantId") Long tenantId, @Param("days") int days);

    @Select("SELECT DISTINCT d.client_ip FROM custom_data_access_log d " +
            "INNER JOIN system_users u ON u.id = d.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId} " +
            "WHERE d.user_id = #{userId} AND d.client_ip IS NOT NULL AND d.client_ip != '' AND d.deleted = 0")
    List<String> selectDiagnoseDistinctIpsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Select("SELECT client_ip AS ip, user_id AS userId, username, " +
            "  MAX(access_time) AS lastSeen, MIN(access_time) AS firstSeen " +
            "FROM custom_data_access_log " +
            "WHERE access_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "  AND client_ip IS NOT NULL AND client_ip != '' AND deleted = 0 " +
            "GROUP BY client_ip, user_id, username")
    List<IpUserRefDO> selectIpUserRefs(@Param("days") int days);

    @Delete("DELETE FROM custom_data_access_log WHERE access_time < #{createTime} LIMIT #{limit}")
    Integer deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit") Integer limit);

    @Select("SELECT * FROM custom_data_access_log WHERE module=#{module} AND entity_type=#{entityType} " +
            "AND JSON_CONTAINS(result_ids, CAST(#{recordId} AS JSON))=1 AND deleted=0 " +
            "ORDER BY access_time DESC LIMIT 200")
    List<DataAccessLogDO> selectViewersByRecordId(@Param("module") String module,
                                                  @Param("entityType") String entityType,
                                                  @Param("recordId") Long recordId);

    default PageResult<DataAccessLogDO> selectPage(DataAccessLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DataAccessLogDO>()
                .eqIfPresent(DataAccessLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(DataAccessLogDO::getModule, reqVO.getModule())
                .eqIfPresent(DataAccessLogDO::getEntityType, reqVO.getEntityType())
                .betweenIfPresent(DataAccessLogDO::getAccessTime, reqVO.getAccessTime())
                .eq(DataAccessLogDO::getDeleted, 0)
                .orderByDesc(DataAccessLogDO::getId));
    }
}
