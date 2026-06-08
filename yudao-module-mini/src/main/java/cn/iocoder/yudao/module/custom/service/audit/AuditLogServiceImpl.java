package cn.iocoder.yudao.module.custom.service.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.audit.vo.AuditLogPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.audit.AuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    @Resource
    private AuditLogMapper auditLogMapper;

    @Override
    @Async
    public void saveAsync(AuditLogDO logDO) {
        try {
            auditLogMapper.insert(logDO);
        } catch (Exception e) {
            log.error("[AuditLog] 审计日志写入失败", e);
        }
    }

    @Override
    public PageResult<AuditLogDO> getPage(AuditLogPageReqVO reqVO) {
        return auditLogMapper.selectPage(reqVO);
    }

    @Override
    public AuditLogDO get(Long id) {
        return auditLogMapper.selectById(id);
    }
}
