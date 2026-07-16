package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.service.security.SecurityActionExecutor;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * URL 访问频率监控（告警类型 URL_MONITOR，运行在 Spring Security 之后，可拿到登录用户）。
 *
 * 管理员在告警规则里配置被监控的 URL（可带或不带 /admin-api 前缀）+ 访问频率（阈值/窗口）。
 * 某来源 IP 在窗口内访问该 URL 超过阈值 → 立即封禁 IP（规则 auto_ban=1）；
 * 若规则还开了 auto_reset_password / auto_delete_user 且能定位到登录账号，一并处置。
 */
@Component
@Slf4j
public class UrlMonitorInterceptor implements HandlerInterceptor {

    private static final String ADMIN_API_PREFIX = "/admin-api";
    private static final int DEFAULT_THRESHOLD = 60;
    private static final long DEFAULT_WINDOW_SECONDS = 60;
    private static final int DEFAULT_SEVERITY = 3;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private SecurityActionExecutor securityActionExecutor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            detect(request);
        } catch (Exception e) {
            log.debug("[UrlMonitor] 检测异常，忽略：{}", e.getMessage());
        }
        return true;
    }

    private void detect(HttpServletRequest request) {
        List<AlertRuleDO> rules = alertRuleService.getUrlMonitorRules();
        if (rules == null || rules.isEmpty()) {
            return;
        }
        String path = normalize(request.getRequestURI());
        AlertRuleDO matched = null;
        for (AlertRuleDO r : rules) {
            if (StrUtil.isNotBlank(r.getMatchUrl()) && path.equals(normalize(r.getMatchUrl()))) {
                matched = r;
                break;
            }
        }
        if (matched == null) {
            return;
        }

        String realIp = ServletUtils.getClientRealIp(request);
        int threshold = matched.getThreshold() != null ? matched.getThreshold() : DEFAULT_THRESHOLD;
        long window = matched.getWindowSeconds() != null ? matched.getWindowSeconds() : DEFAULT_WINDOW_SECONDS;
        int severity = matched.getSeverity() != null ? matched.getSeverity() : DEFAULT_SEVERITY;

        String key = "security:urlmon:" + matched.getId() + ":" + realIp;
        long count = increment(key, window);
        if (count <= threshold) {
            return;
        }

        Long userId = tryGetUserId();
        String message = window + "秒内访问被监控URL[" + matched.getMatchUrl() + "]达 " + count + " 次，超过阈值 " + threshold;
        log.warn("[UrlMonitor] realIp={} userId={} url={} 窗口内第{}次 → 触发处置", realIp, userId, matched.getMatchUrl(), count);
        securityActionExecutor.execute(matched, "URL_MONITOR", severity, realIp, userId,
                request.getRequestURI(), request.getMethod(), "URL: " + matched.getMatchUrl(), message, true);
    }

    /** 归一化：去掉 /admin-api 前缀、去掉结尾多余斜杠，保证带不带前缀都能匹配 */
    static String normalize(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        String u = url.trim();
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }
        if (u.startsWith(ADMIN_API_PREFIX)) {
            u = u.substring(ADMIN_API_PREFIX.length());
        }
        if (u.length() > 1 && u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
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

}
