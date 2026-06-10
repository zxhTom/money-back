package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogPageReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OperateLogMapper extends BaseMapperX<OperateLogDO> {

    @Select("SELECT DISTINCT user_ip FROM system_operate_log " +
            "WHERE user_id = #{userId} AND user_ip IS NOT NULL AND user_ip != '' AND deleted = 0")
    List<String> selectDistinctIpsByUserId(Long userId);

    @Select("SELECT user_ip AS ip, user_id AS userId, " +
            "  MAX(create_time) AS lastSeen, MIN(create_time) AS firstSeen " +
            "FROM system_operate_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "  AND user_ip IS NOT NULL AND user_ip != '' AND deleted = 0 " +
            "GROUP BY user_ip, user_id")
    List<Map<String, Object>> selectIpUserRefs(@Param("days") int days);

    @Delete("DELETE FROM system_operate_log WHERE create_time < #{createTime} LIMIT #{limit}")
    Integer deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit") Integer limit);

    default PageResult<OperateLogDO> selectPage(OperateLogPageReqVO pageReqDTO) {
        return selectPage(pageReqDTO, new LambdaQueryWrapperX<OperateLogDO>()
                .eqIfPresent(OperateLogDO::getUserId, pageReqDTO.getUserId())
                .eqIfPresent(OperateLogDO::getBizId, pageReqDTO.getBizId())
                .likeIfPresent(OperateLogDO::getType, pageReqDTO.getType())
                .likeIfPresent(OperateLogDO::getSubType, pageReqDTO.getSubType())
                .likeIfPresent(OperateLogDO::getAction, pageReqDTO.getAction())
                .betweenIfPresent(OperateLogDO::getCreateTime, pageReqDTO.getCreateTime())
                .orderByDesc(OperateLogDO::getId));
    }

    default PageResult<OperateLogDO> selectPage(OperateLogPageReqDTO pageReqDTO) {
        return selectPage(pageReqDTO, new LambdaQueryWrapperX<OperateLogDO>()
                .eqIfPresent(OperateLogDO::getType, pageReqDTO.getType())
                .eqIfPresent(OperateLogDO::getBizId, pageReqDTO.getBizId())
                .eqIfPresent(OperateLogDO::getUserId, pageReqDTO.getUserId())
                .orderByDesc(OperateLogDO::getId));
    }

}
