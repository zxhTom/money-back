package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

@Component
@Slf4j
public class GlobalDiagnoseAccessGuard {

    private static final String[] DIAGNOSE_PERMISSIONS = new String[]{
            "custom:security:global-diagnose",
            "mini:admin:security:diagnose"
    };

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private PermissionService permissionService;

    public GlobalDiagnoseTaskContext requireValidContext(String taskType, Integer days, String lockToken) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null || loginUser.getId() == null) {
            throw exception0(401, "请先登录");
        }
        if (!Objects.equals(UserTypeEnum.ADMIN.getValue(), loginUser.getUserType())) {
            throw exception0(403, "仅管理员可执行诊断");
        }

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            tenantId = loginUser.getTenantId();
        }
        if (tenantId == null || !Objects.equals(tenantId, loginUser.getTenantId())) {
            throw exception0(403, "租户信息不匹配或缺失");
        }

        Long userId = loginUser.getId();
        AdminUserDO user = adminUserService.getUser(userId);
        if (user == null || !Objects.equals(CommonStatusEnum.ENABLE.getStatus(), user.getStatus())) {
            throw exception0(403, "用户不存在或已被禁用");
        }
        if (!Objects.equals(tenantId, user.getTenantId())) {
            throw exception0(403, "用户所属租户不一致");
        }

        if (!permissionService.hasAnyPermissions(userId, DIAGNOSE_PERMISSIONS)) {
            throw exception0(403, "无权访问全局诊断");
        }

        return GlobalDiagnoseTaskContext.builder()
                .tenantId(tenantId)
                .userId(userId)
                .taskType(taskType)
                .days(days)
                .lockToken(lockToken)
                .build();
    }

    public boolean canPushResult(Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return false;
        }
        AdminUserDO user = adminUserService.getUser(userId);
        if (user == null || !Objects.equals(CommonStatusEnum.ENABLE.getStatus(), user.getStatus())) {
            return false;
        }
        if (!Objects.equals(tenantId, user.getTenantId())) {
            return false;
        }
        return permissionService.hasAnyPermissions(userId, DIAGNOSE_PERMISSIONS);
    }
}
