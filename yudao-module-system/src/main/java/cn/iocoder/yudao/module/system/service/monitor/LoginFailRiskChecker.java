package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 登录失败风控（登录密码错误时调用，覆盖后台 admin 登录与小程序密码登录）。
 *
 * 策略：按【来源真实IP】在窗口内累计登录失败次数，超阈值 → 封该 IP + 告警。
 * 绝不锁定账号——否则攻击者可故意用某真实账号反复失败来把该账号锁死（拒绝服务/挤兑真实用户）。
 * 阈值/窗口/封禁时长读 LOGIN_ABUSE 告警规则；白名单 IP 完全跳过。
 */
@Component
@Slf4j
public class LoginFailRiskChecker {

    private static final String TYPE = "LOGIN_ABUSE";
    private static final int DEFAULT_THRESHOLD = 10;
    private static final long DEFAULT_WINDOW_SECONDS = 600;
    private static final long DEFAULT_BAN_SECONDS = 3600;
    private static final int DEFAULT_SEVERITY = 3;
    private static final String KEY = "security:login:fail:%s";

    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private IpBlacklistService ipBlacklistService;
    @Resource
    private IpWhitelistService ipWhitelistService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 登录失败时调用；内部自行取当前请求的可信真实IP。绝不抛出，避免影响登录主流程。 */
    public void onLoginFailure(String username) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            if (request == null) {
                return;
            }
            String ip = ServletUtils.getClientRealIp(request);
            if (ip == null || ip.isEmpty() || ipWhitelistService.isWhitelisted(ip)) {
                return; // 无IP / 白名单 直接放过
            }
            AlertRuleDO rule = alertRuleService.getCachedRule(TYPE);
            if (rule != null && rule.getEnabled() != null && rule.getEnabled() == 0) {
                return; // 规则禁用
            }
            int threshold = rule != null && rule.getThreshold() != null ? rule.getThreshold() : DEFAULT_THRESHOLD;
            long window = rule != null && rule.getWindowSeconds() != null ? rule.getWindowSeconds() : DEFAULT_WINDOW_SECONDS;

            String key = String.format(KEY, ip);
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, window, TimeUnit.SECONDS);
            }
            if (count == null || count < threshold) {
                return;
            }

            int severity = rule != null && rule.getSeverity() != null ? rule.getSeverity() : DEFAULT_SEVERITY;
            long banSeconds = rule != null && rule.getBanDurationSeconds() != null
                    ? rule.getBanDurationSeconds() : DEFAULT_BAN_SECONDS;
            String message = window + "秒内登录失败 " + count + " 次，超过阈值 " + threshold + "，封禁来源IP（不锁账号）";
            securityAlertService.saveAsync(TYPE, severity, ip, null, "/login", "LOGIN",
                    "尝试账号=" + username, message);
            boolean autoBan = rule == null || rule.getAutoBan() == null || rule.getAutoBan() == 1;
            if (autoBan) {
                ipBlacklistService.addToBlacklist(ip, "自动封禁：" + message, true,
                        LocalDateTime.now().plusSeconds(banSeconds));
            }
            log.warn("[LoginFailRisk] IP={} 登录失败第 {} 次，超阈值 {} → 封IP {}s（账号未锁）",
                    ip, count, threshold, banSeconds);
        } catch (Exception e) {
            log.debug("[LoginFailRisk] 检查异常，忽略：{}", e.getMessage());
        }
    }
}
