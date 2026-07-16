package cn.iocoder.yudao.module.custom.service.invite;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.REGISTER_TOO_FREQUENT;

/**
 * 注册频率风控：同一 IP 在窗口内注册次数超过阈值时，
 * 触发 REGISTER_ABUSE 告警 + 把 IP 加入黑名单 + 拒绝本次注册。
 *
 * 阈值/窗口/封禁时长全部读 custom_alert_rule 里 alert_type=REGISTER_ABUSE 的配置，
 * 规则缺失或禁用时用下面的默认值。
 */
@Component
@Slf4j
public class RegisterRiskControlGuard {

    private static final String ALERT_TYPE = "REGISTER_ABUSE";
    private static final int DEFAULT_THRESHOLD = 20;
    private static final long DEFAULT_WINDOW_SECONDS = 3600;
    private static final long DEFAULT_BAN_SECONDS = 86400;
    private static final int DEFAULT_SEVERITY = 3;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private IpBlacklistService ipBlacklistService;

    public void beforeRegister(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }

        AlertRuleDO rule = alertRuleService.getCachedRule(ALERT_TYPE);
        if (rule != null && rule.getEnabled() != null && rule.getEnabled() == 0) {
            return; // 规则被禁用，跳过风控
        }
        int threshold = rule != null && rule.getThreshold() != null ? rule.getThreshold() : DEFAULT_THRESHOLD;
        long window = rule != null && rule.getWindowSeconds() != null ? rule.getWindowSeconds() : DEFAULT_WINDOW_SECONDS;
        long banDuration = rule != null && rule.getBanDurationSeconds() != null ? rule.getBanDurationSeconds() : DEFAULT_BAN_SECONDS;
        int severity = rule != null && rule.getSeverity() != null ? rule.getSeverity() : DEFAULT_SEVERITY;
        boolean autoBan = rule == null || rule.getAutoBan() == null || rule.getAutoBan() == 1;

        String key = "security:register:" + ip;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, window, TimeUnit.SECONDS);
        }
        if (count != null && count > threshold) {
            log.warn("[RegisterRiskControl] 注册频率超限 ip={} count={} threshold={}", ip, count, threshold);
            securityAlertService.saveAsync(ALERT_TYPE, severity, ip, null, "/register", "POST",
                    null, window + "秒内注册次数达 " + count + " 次，已超过阈值 " + threshold);
            if (autoBan) {
                ipBlacklistService.addToBlacklist(ip, "自动封禁：注册频率超限", true,
                        LocalDateTime.now().plusSeconds(banDuration));
            }
            throw exception(REGISTER_TOO_FREQUENT);
        }
    }

}
