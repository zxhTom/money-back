package cn.iocoder.yudao.module.custom.service.security;

import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityPwdResetLogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.SecurityPwdResetLogMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 告警规则触发后的统一处置动作：
 *   1. 写安全告警（走通知管道，管理员可见）
 *   2. 若规则开启 auto_ban → 拉黑来源 IP
 *   3. 若规则开启 auto_reset_password 且能定位到账号 → 先改密码（复杂随机）再剔除其所有 token，并记录到改密日志
 *   4. 若规则开启 auto_delete_user 且能定位到账号 → 逻辑删除用户并剔除其所有 token
 * 动作互不影响，可单独或组合开启。
 */
@Component
@Slf4j
public class SecurityActionExecutor {

    private static final long DEFAULT_BAN_SECONDS = 86400;

    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private IpBlacklistService ipBlacklistService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private SecurityPwdResetLogMapper securityPwdResetLogMapper;

    public void execute(AlertRuleDO rule, String alertType, int severity, String ip, Long userId,
                        String requestUrl, String requestMethod, String detail, String message) {
        execute(rule, alertType, severity, ip, userId, requestUrl, requestMethod, detail, message, true);
    }

    /**
     * @param allowUserActions 是否允许执行"针对账号"的动作（改密+踢token、逻辑删除）。
     *        模拟请求等可能误伤正常用户的场景传 false：只封 IP，不动账号。
     */
    public void execute(AlertRuleDO rule, String alertType, int severity, String ip, Long userId,
                        String requestUrl, String requestMethod, String detail, String message,
                        boolean allowUserActions) {
        // 1. 告警
        securityAlertService.saveAsync(alertType, severity, ip, userId, requestUrl, requestMethod, detail, message);

        // 2. 封禁 IP
        boolean autoBan = rule != null && rule.getAutoBan() != null && rule.getAutoBan() == 1;
        if (autoBan && ip != null && !ip.isEmpty()) {
            long banSeconds = rule.getBanDurationSeconds() != null ? rule.getBanDurationSeconds() : DEFAULT_BAN_SECONDS;
            ipBlacklistService.addToBlacklist(ip, "自动封禁：" + message, true,
                    LocalDateTime.now().plusSeconds(banSeconds));
        }

        // 3. 改密码 + 踢 token（先改后踢），并记录明文新密码
        boolean autoResetPassword = allowUserActions
                && rule != null && rule.getAutoResetPassword() != null && rule.getAutoResetPassword() == 1;
        if (autoResetPassword && userId != null) {
            try {
                String newPassword = ComplexPasswordGenerator.generate();
                boolean done = adminUserService.resetPasswordAndKickForSecurity(userId, newPassword);
                if (done) {
                    recordPwdReset(userId, newPassword, message, alertType, ip);
                    log.warn("[SecurityAction][{}] userId={} 已自动改密并踢下线", alertType, userId);
                }
            } catch (Exception e) {
                log.error("[SecurityAction][{}] userId={} 自动改密失败", alertType, userId, e);
            }
        }

        // 4. 逻辑删除用户 + 剔除 token（仅当调用方允许时）
        boolean autoDeleteUser = allowUserActions
                && rule != null && rule.getAutoDeleteUser() != null && rule.getAutoDeleteUser() == 1;
        if (autoDeleteUser && userId != null) {
            try {
                boolean deleted = adminUserService.deleteUserForSecurity(userId);
                log.warn("[SecurityAction][{}] userId={} 逻辑删除结果={}", alertType, userId, deleted);
            } catch (Exception e) {
                log.error("[SecurityAction][{}] userId={} 逻辑删除失败", alertType, userId, e);
            }
        }
    }

    private void recordPwdReset(Long userId, String newPassword, String reason, String alertType, String ip) {
        try {
            SecurityPwdResetLogDO logDO = new SecurityPwdResetLogDO();
            logDO.setUserId(userId);
            logDO.setUsername(adminUserService.getUser(userId) != null ? adminUserService.getUser(userId).getUsername() : null);
            logDO.setNewPassword(newPassword);
            logDO.setReason(reason);
            logDO.setAlertType(alertType);
            logDO.setSourceIp(ip);
            logDO.setCreateTime(LocalDateTime.now());
            securityPwdResetLogMapper.insert(logDO);
        } catch (Exception e) {
            log.error("[SecurityAction] 写改密记录失败 userId={}", userId, e);
        }
    }

}
