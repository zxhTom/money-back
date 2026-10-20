package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.ResetPasswordByIdNoReqVO;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

@Service
public class ContractPasswordRecoveryService {
    public static final String HOURS_KEY = "security.password-recovery.ip-window-hours";
    public static final String LIMIT_KEY = "security.password-recovery.ip-max-resets";
    @Resource private AdminUserMapper adminUserMapper;
    @Resource private UserRoleMapper userRoleMapper;
    @Resource private RoleService roleService;
    @Resource private IdCardCipherService idCardCipherService;
    @Resource private AdminUserService adminUserService;
    @Resource private ConfigApi configApi;
    @Resource private RecoveryIpQuota quota;
    private int config(String key, int fallback, int max) {
        String raw = configApi.getConfigValueByKey(key);
        if (raw == null || raw.trim().isEmpty()) return fallback;
        try { int value = Integer.parseInt(raw.trim()); if (value > 0 && value <= max) return value; }
        catch (NumberFormatException ignored) { }
        throw exception0(503, "找回密码配置异常，请联系管理员");
    }
    @Transactional(rollbackFor = Exception.class)
    public void reset(ResetPasswordByIdNoReqVO input) {
        reset(input, ServletUtils.getClientRealIp(ServletUtils.getRequest()));
    }
    void reset(ResetPasswordByIdNoReqVO input, String ip) {
        Long tenant = TenantContextHolder.getTenantId();
        if (tenant == null || tenant <= 0) throw exception0(400, "缺少有效租户信息");
        List<AdminUserDO> candidates = adminUserMapper.selectListByUsername(input.getUsername().trim());
        List<AdminUserDO> matches = candidates.stream().filter(u -> tenant.equals(u.getTenantId())).collect(Collectors.toList());
        if (matches.size() != 1) throw rejected();
        AdminUserDO user = matches.get(0);
        if (!Integer.valueOf(0).equals(user.getStatus())) throw rejected();
        List<Long> ids = userRoleMapper.selectListByUserId(user.getId()).stream().map(x -> x.getRoleId()).collect(Collectors.toList());
        List<RoleDO> roles = ids.isEmpty() ? Collections.emptyList() : roleService.getRoleList(ids);
        // Additional roles (including administrators) must never use this public recovery route.
        if (roles.isEmpty() || roles.size() != new HashSet<>(ids).size()
                || roles.stream().anyMatch(role -> !"contract".equals(role.getCode()) || !Integer.valueOf(0).equals(role.getStatus()))) throw rejected();
        if (!idCardCipherService.sameIdCard(input.getIdNo(), user.getIdNo())) throw rejected();
        int hours = config(HOURS_KEY, 3, 8760);
        int limit = config(LIMIT_KEY, 5, 1000);
        String reservation = quota.reserve(ip, hours, limit);
        boolean transactional = TransactionSynchronizationManager.isSynchronizationActive();
        if (transactional) TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) quota.release(reservation);
            }
        });
        try { adminUserService.updateUserPasswordAndPayPassword(user.getId(), input.getPassword()); }
        catch (RuntimeException ex) { if (!transactional) quota.release(reservation); throw ex; }
    }
    private RuntimeException rejected() { return exception0(400, "用户名或身份证不匹配，或该账号不支持此方式找回密码"); }
}
