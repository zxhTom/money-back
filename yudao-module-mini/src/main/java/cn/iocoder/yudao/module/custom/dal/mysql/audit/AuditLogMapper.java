package cn.iocoder.yudao.module.custom.dal.mysql.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.audit.vo.AuditLogPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapperX<AuditLogDO> {

    default PageResult<AuditLogDO> selectPage(AuditLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AuditLogDO>()
                .eqIfPresent(AuditLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AuditLogDO::getOperationType, reqVO.getOperationType())
                .eqIfPresent(AuditLogDO::getModule, reqVO.getModule())
                .eqIfPresent(AuditLogDO::getEntityType, reqVO.getEntityType())
                .eqIfPresent(AuditLogDO::getEntityId, reqVO.getEntityId())
                .eqIfPresent(AuditLogDO::getExternalIp, reqVO.getExternalIp())
                .eqIfPresent(AuditLogDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AuditLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AuditLogDO::getId));
    }
}
