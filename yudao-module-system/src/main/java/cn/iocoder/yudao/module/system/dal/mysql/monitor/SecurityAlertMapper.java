package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SecurityAlertMapper extends BaseMapperX<SecurityAlertDO> {

    @Delete("DELETE FROM custom_security_alert WHERE create_time < #{createTime} LIMIT #{limit}")
    Integer deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit") Integer limit);

    default PageResult<SecurityAlertDO> selectPage(SecurityAlertPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SecurityAlertDO>()
                .eqIfPresent(SecurityAlertDO::getAlertType, reqVO.getAlertType())
                .eqIfPresent(SecurityAlertDO::getSeverity, reqVO.getSeverity())
                .eqIfPresent(SecurityAlertDO::getSourceIp, reqVO.getSourceIp())
                .eqIfPresent(SecurityAlertDO::getHandled, reqVO.getHandled())
                .betweenIfPresent(SecurityAlertDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SecurityAlertDO::getId));
    }

}
