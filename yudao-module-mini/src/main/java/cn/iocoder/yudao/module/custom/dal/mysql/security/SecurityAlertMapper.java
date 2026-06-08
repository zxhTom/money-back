package cn.iocoder.yudao.module.custom.dal.mysql.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.security.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityAlertDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SecurityAlertMapper extends BaseMapperX<SecurityAlertDO> {

    default PageResult<SecurityAlertDO> selectPage(SecurityAlertPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SecurityAlertDO>()
                .eqIfPresent(SecurityAlertDO::getAlertType, reqVO.getAlertType())
                .eqIfPresent(SecurityAlertDO::getSeverity, reqVO.getSeverity())
                .eqIfPresent(SecurityAlertDO::getSourceIp, reqVO.getSourceIp())
                .eqIfPresent(SecurityAlertDO::getHandled, reqVO.getHandled())
                .betweenIfPresent(SecurityAlertDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SecurityAlertDO::getId));
    }

    @Select("SELECT alert_type, COUNT(*) as cnt FROM custom_security_alert " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "GROUP BY alert_type ORDER BY cnt DESC")
    List<Map<String, Object>> selectTodayAlertTypeStats();

    @Select("SELECT source_ip, COUNT(*) as cnt FROM custom_security_alert " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) AND source_ip IS NOT NULL " +
            "GROUP BY source_ip ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> selectTopAttackIps();
}
