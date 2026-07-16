package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.service.security.SecurityActionExecutor;
import cn.iocoder.yudao.module.custom.service.security.UaClassifyService;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 模拟/伪造请求检测（运行在 Spring Security 之后，能拿到登录用户）。
 *
 * 判定"模拟请求"三类信号：
 *   1. 【硬判定，立即处置】UA 是明确的自动化工具（python-requests / curl / urllib 等）或空 UA：
 *      直接判定模拟请求，立即封禁来源 IP（内网来源如健康检查除外，避免误伤）。
 *   2. 【软信号，累加到阈值】伪造来源 IP：X-Forwarded-For 最左值是内网IP 且与可信真实IP 不一致（人为注入）。
 *   3. 【软信号，累加到阈值，宽松】UA 不在浏览器白名单里（custom_ua_whitelist）。
 * 软信号同一账号（或无账号时按IP）窗口内（默认1小时）累计达阈值（默认2次）→ 处置。
 *
 * 处置一律"只封 IP、不删用户"（避免有人伪造把正常用户删掉）；动作/阈值/窗口读 SIMULATED_REQUEST 告警规则配置。
 */
@Component
@Slf4j
public class SimulatedRequestInterceptor implements HandlerInterceptor {

    private static final String ALERT_TYPE = "SIMULATED_REQUEST";
    private static final int DEFAULT_THRESHOLD = 2;
    private static final long DEFAULT_WINDOW_SECONDS = 3600;
    private static final int DEFAULT_SEVERITY = 3;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private SecurityActionExecutor securityActionExecutor;
    @Resource
    private UaClassifyService uaClassifyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            detect(request);
        } catch (Exception e) {
            log.debug("[SimulatedRequest] 检测异常，忽略：{}", e.getMessage());
        }
        return true; // 只做检测/处置，不阻断当前请求本身（IP 封禁在下一次请求由黑名单过滤器拦截）
    }

    private void detect(HttpServletRequest request) {
        String realIp = ServletUtils.getClientRealIp(request);
        String ua = request.getHeader("User-Agent");
        Long userId = tryGetUserId();

        AlertRuleDO rule = alertRuleService.getCachedRule(ALERT_TYPE);
        if (rule != null && rule.getEnabled() != null && rule.getEnabled() == 0) {
            return; // 规则禁用
        }
        int threshold = rule != null && rule.getThreshold() != null ? rule.getThreshold() : DEFAULT_THRESHOLD;
        long window = rule != null && rule.getWindowSeconds() != null ? rule.getWindowSeconds() : DEFAULT_WINDOW_SECONDS;
        int severity = rule != null && rule.getSeverity() != null ? rule.getSeverity() : DEFAULT_SEVERITY;

        // 1. 硬判定：明确的自动化工具 UA（python-requests 等）→ 立即处置（不计数）
        if (uaClassifyService.isHardBot(ua)) {
            // 内网来源（健康检查、容器探活等）不封禁，避免自伤
            if (ServletUtils.isInternalIp(realIp)) {
                return;
            }
            String message = "自动化工具/非法UA 请求(" + safeUa(ua) + ")，立即判定为模拟请求";
            log.warn("[SimulatedRequest][HARD] realIp={} ua={} uri={} → 立即封禁", realIp, safeUa(ua), request.getRequestURI());
            act(rule, severity, realIp, userId, request, "UA: " + safeUa(ua), message);
            return;
        }

        // 2/3. 软信号：伪造XFF 或 非白名单浏览器 → 累加到阈值
        boolean forgedXff = isForgedXff(request, realIp);
        boolean notBrowser = !uaClassifyService.isWhitelistedBrowser(ua);
        if (!forgedXff && !notBrowser) {
            return;
        }

        String counterKey = "security:simreq:" + (userId != null ? ("uid:" + userId) : ("ip:" + realIp));
        long count = increment(counterKey, window);
        String reason = forgedXff ? "伪造来源IP" : "非白名单浏览器";
        log.warn("[SimulatedRequest][SOFT] userId={} realIp={} 原因={} ua={} 窗口内第{}次 uri={}",
                userId, realIp, reason, safeUa(ua), count, request.getRequestURI());

        if (count < threshold) {
            return;
        }
        String message = window + "秒内可疑请求(" + reason + ")达 " + count + " 次，超过阈值 " + threshold;
        act(rule, severity, realIp, userId, request, "UA: " + safeUa(ua), message);
    }

    /** 统一处置：告警 + 按规则封IP，但强制不删用户 */
    private void act(AlertRuleDO rule, int severity, String ip, Long userId,
                     HttpServletRequest request, String detail, String message) {
        securityActionExecutor.execute(rule, ALERT_TYPE, severity, ip, userId,
                request.getRequestURI(), request.getMethod(), detail, message, false);
    }

    private boolean isForgedXff(HttpServletRequest request, String realIp) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(xff)) {
            return false;
        }
        String leftmost = xff.split(",")[0].trim();
        return ServletUtils.isInternalIp(leftmost) && !leftmost.equalsIgnoreCase(realIp);
    }

    private long increment(String key, long ttlSeconds) {
        Long v = stringRedisTemplate.opsForValue().increment(key);
        if (v != null && v == 1L) {
            stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return v == null ? 0L : v;
    }

    private Long tryGetUserId() {
        try {
            return SecurityFrameworkUtils.getLoginUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String safeUa(String ua) {
        if (ua == null) {
            return "<empty>";
        }
        return ua.length() > 120 ? ua.substring(0, 120) : ua;
    }

}
