package cn.iocoder.yudao.module.custom.service.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.audit.vo.AuditLogPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;

public interface AuditLogService {

    void saveAsync(AuditLogDO log);

    PageResult<AuditLogDO> getPage(AuditLogPageReqVO reqVO);

    AuditLogDO get(Long id);

    Integer cleanAuditLog(Integer exceedDay, Integer deleteLimit);
}
