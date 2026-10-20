package cn.iocoder.yudao.module.custom.service.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.audit.vo.AuditLogPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.audit.AuditLogMapper;
import cn.iocoder.yudao.module.custom.framework.audit.util.AuditLogPrivacy;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    @Resource
    private AuditLogMapper auditLogMapper;
    @Resource
    private SecurityFrameworkService securityFrameworkService;

    private void requireAccess(String permission) {
        if (!securityFrameworkService.hasAnyRoles("super_admin", "tenant_admin")
                || !securityFrameworkService.hasPermission(permission)) {
            throw new AccessDeniedException("无审计日志管理权限");
        }
    }

    private Long tenantScope() {
        // Historical audit rows have no tenant_id. Only the super administrator may read globally.
        return securityFrameworkService.hasAnyRoles("super_admin") ? null
                : TenantContextHolder.getRequiredTenantId();
    }

    @Override
    @Async
    public void saveAsync(AuditLogDO logDO) {
        try {
            auditLogMapper.insert(AuditLogPrivacy.safeCopy(logDO));
        } catch (Exception e) {
            log.error("[AuditLog] 审计日志写入失败", e);
        }
    }

    @Override
    public PageResult<AuditLogDO> getPage(AuditLogPageReqVO reqVO) {
        requireAccess("custom:audit-log:query");
        return safePage(reqVO);
    }

    @Override
    public PageResult<AuditLogDO> getExportPage(AuditLogPageReqVO reqVO) {
        requireAccess("custom:audit-log:export");
        return safePage(reqVO);
    }

    private PageResult<AuditLogDO> safePage(AuditLogPageReqVO reqVO) {
        PageResult<AuditLogDO> page = auditLogMapper.selectPage(reqVO, tenantScope());
        return new PageResult<>(page.getList().stream().map(AuditLogPrivacy::safeCopy)
                .collect(Collectors.toList()), page.getTotal());
    }

    @Override
    public AuditLogDO get(Long id) {
        requireAccess("custom:audit-log:query");
        return AuditLogPrivacy.safeCopy(auditLogMapper.selectByIdInTenant(id, tenantScope()));
    }

    @Override
    public Integer cleanAuditLog(Integer exceedDay, Integer deleteLimit) {
        int count = 0;
        LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = auditLogMapper.deleteByCreateTimeLt(expireDate, deleteLimit);
            count += deleteCount;
            if (deleteCount < deleteLimit) {
                break;
            }
        }
        return count;
    }
}
