package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessLogDO;
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

    @Select("SELECT client_ip AS ip, user_id AS userId, username, " +
            "  MAX(access_time) AS lastSeen, MIN(access_time) AS firstSeen " +
            "FROM custom_data_access_log " +
            "WHERE access_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "  AND client_ip IS NOT NULL AND client_ip != '' AND deleted = 0 " +
            "GROUP BY client_ip, user_id, username")
    List<Map<String, Object>> selectIpUserRefs(@Param("days") int days);

    @Delete("DELETE FROM custom_data_access_log WHERE access_time < #{createTime} LIMIT #{limit}")
    Integer deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit") Integer limit);

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
