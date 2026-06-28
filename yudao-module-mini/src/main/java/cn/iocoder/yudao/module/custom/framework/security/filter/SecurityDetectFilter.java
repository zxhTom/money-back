package cn.iocoder.yudao.module.custom.framework.security.filter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.framework.audit.util.IpUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.IpRiskCheckService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 安全检测过滤器：检测 SQL 注入、XSS、暴力破解、IP 黑名单
 */
@Slf4j
public class SecurityDetectFilter extends OncePerRequestFilter {

    // SQL 注入特征正则
    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)(union\\s+select|union\\s+all\\s+select)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(select\\s+.*\\s+from\\s+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(insert\\s+into\\s+|update\\s+.*\\s+set\\s+|delete\\s+from\\s+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(drop\\s+table|drop\\s+database|truncate\\s+table)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(exec\\s*\\(|execute\\s*\\(|xp_cmdshell)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(information_schema|sys\\.tables|sysobjects)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("'\\s*(or|and)\\s*'?\\d+'?\\s*=\\s*'?\\d+'?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)(/\\*.*\\*/|--\\s|;\\s*(drop|delete|update|insert))", Pattern.CASE_INSENSITIVE)
    );

    // XSS 特征正则
    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)<script[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)javascript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)on(error|load|click|mouseover|focus|blur)\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)<iframe[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)expression\\s*\\(", Pattern.CASE_INSENSITIVE)
    );

    // 暴力破解检测的目标路径
    private static final Set<String> BRUTE_FORCE_PATHS = new HashSet<>(Arrays.asList(
            "/system/auth/login",
            "/system/auth/sms-login",
            "/wechat/password-login",
            "/custom/contract/dashboard/reset-password-by-idno",
            "/custom/contract/dashboard/update-password-by-email",
            "/custom/contract/dashboard/send-email-code"
    ));

    // 默认阈值（数据库规则未配置时使用）
    private static final int DEFAULT_BRUTE_FORCE_THRESHOLD = 10;
    private static final long DEFAULT_BRUTE_FORCE_WINDOW = 300;
    private static final long DEFAULT_AUTO_BAN_SECONDS = 180000;
    private static final int DEFAULT_SQL_INJECT_THRESHOLD = 5;

    private final IpBlacklistService ipBlacklistService;
    private final SecurityAlertService securityAlertService;
    private final StringRedisTemplate redisTemplate;
    private final IpRiskCheckService ipRiskCheckService;
    private final AlertRuleService alertRuleService;

    public SecurityDetectFilter(IpBlacklistService ipBlacklistService,
                                SecurityAlertService securityAlertService,
                                StringRedisTemplate redisTemplate,
                                IpRiskCheckService ipRiskCheckService,
                                AlertRuleService alertRuleService) {
        this.ipBlacklistService = ipBlacklistService;
        this.securityAlertService = securityAlertService;
        this.redisTemplate = redisTemplate;
        this.ipRiskCheckService = ipRiskCheckService;
        this.alertRuleService = alertRuleService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = IpUtils.getExternalIp(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. IP 黑名单检查
        if (ipBlacklistService.isBlacklisted(ip)) {
            log.warn("[SecurityDetect] 黑名单IP请求被拦截: {} {}", ip, uri);
            block(response, "Access denied");
            return;
        }

        // 2. IP 风险检测（异步，不阻塞请求）
//        ipRiskCheckService.checkAsync(ip, getUserId(request), uri);

        // 4. 暴力破解检测
        if (isBruteForcePath(uri)) {
            if (checkBruteForce(ip, uri, response)) {
                return;
            }
        }

        // 5. 收集所有请求参数用于安全检测
        String suspiciousContent = collectRequestContent(request);

        // 6. SQL 注入检测
        if (StrUtil.isNotBlank(suspiciousContent)) {
            String sqlMatch = detectSqlInjection(suspiciousContent);
            if (sqlMatch != null) {
                handleSqlInjection(ip, getUserId(request), uri, method, sqlMatch, response);
                return;
            }

            // 7. XSS 检测（仅告警，不拦截 - 因为后端有 XssFilter 处理）
            String xssMatch = detectXss(suspiciousContent);
            if (xssMatch != null) {
                securityAlertService.saveAsync("XSS", 2, ip, null, uri, method,
                        truncate(xssMatch, 500), "检测到 XSS 攻击特征");
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBruteForcePath(String uri) {
        for (String path : BRUTE_FORCE_PATHS) {
            if (uri.endsWith(path) || uri.contains(path)) return true;
        }
        return false;
    }

    private boolean checkBruteForce(String ip, String uri, HttpServletResponse response) throws IOException {
        AlertRuleDO rule = alertRuleService.getCachedRule("BRUTE_FORCE");
        if (rule != null && rule.getEnabled() == 0) return false; // 规则已禁用，跳过检测

        int threshold = rule != null && rule.getThreshold() != null ? rule.getThreshold() : DEFAULT_BRUTE_FORCE_THRESHOLD;
        long window = rule != null && rule.getWindowSeconds() != null ? rule.getWindowSeconds() : DEFAULT_BRUTE_FORCE_WINDOW;
        long banDuration = rule != null && rule.getBanDurationSeconds() != null ? rule.getBanDurationSeconds() : DEFAULT_AUTO_BAN_SECONDS;
        int severity = rule != null ? rule.getSeverity() : 3;
        boolean autoBan = rule == null || rule.getAutoBan() == 1;

        String key = "security:brute:" + ip + ":" + sanitizeForKey(uri);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window, TimeUnit.SECONDS);
        }
        if (count != null && count > threshold) {
            log.warn("[SecurityDetect] 暴力破解检测: IP={} URI={} Count={}", ip, uri, count);
            securityAlertService.saveAsync("BRUTE_FORCE", severity, ip, null, uri, "POST",
                    null, window + "秒内请求次数达 " + count + " 次，已超过阈值 " + threshold);
            if (autoBan) {
                ipBlacklistService.addToBlacklist(ip, "自动封禁：暴力破解检测", true,
                        LocalDateTime.now().plusSeconds(banDuration));
            }
            block(response, "Too many requests");
            return true;
        }
        return false;
    }

    private String collectRequestContent(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        // Query string
        String queryString = request.getQueryString();
        if (StrUtil.isNotBlank(queryString)) {
            sb.append(queryString).append(" ");
        }
        // 参数
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String[] values = request.getParameterValues(name);
            if (values != null) {
                for (String val : values) {
                    sb.append(name).append("=").append(val).append(" ");
                }
            }
        }
        return sb.toString();
    }

    private String detectSqlInjection(String content) {
        for (Pattern p : SQL_INJECTION_PATTERNS) {
            if (p.matcher(content).find()) {
                return content.length() > 200 ? content.substring(0, 200) : content;
            }
        }
        return null;
    }

    private String detectXss(String content) {
        for (Pattern p : XSS_PATTERNS) {
            if (p.matcher(content).find()) {
                return content.length() > 200 ? content.substring(0, 200) : content;
            }
        }
        return null;
    }

    private void handleSqlInjection(String ip, Long userId, String uri, String method,
                                    String content, HttpServletResponse response) throws IOException {
        log.warn("[SecurityDetect] SQL注入检测: IP={} URI={}", ip, uri);

        AlertRuleDO rule = alertRuleService.getCachedRule("SQL_INJECTION");
        int threshold = rule != null && rule.getThreshold() != null ? rule.getThreshold() : DEFAULT_SQL_INJECT_THRESHOLD;
        long window = rule != null && rule.getWindowSeconds() != null ? rule.getWindowSeconds() : 600;
        long banDuration = rule != null && rule.getBanDurationSeconds() != null ? rule.getBanDurationSeconds() : DEFAULT_AUTO_BAN_SECONDS;
        int severity = rule != null ? rule.getSeverity() : 3;
        boolean autoBan = rule == null || rule.getAutoBan() == 1;

        securityAlertService.saveAsync("SQL_INJECTION", severity, ip, userId, uri, method,
                truncate(content, 500), "检测到 SQL 注入攻击特征");

        String key = "security:sqlinject:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window, TimeUnit.SECONDS);
        }
        if (autoBan && count != null && count >= threshold) {
            ipBlacklistService.addToBlacklist(ip, "自动封禁：多次SQL注入尝试", true,
                    LocalDateTime.now().plusSeconds(banDuration));
        }
        block(response, "Forbidden");
    }

    private void block(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"code\":403,\"msg\":\"" + message + "\"}");
        }
    }

    private String sanitizeForKey(String uri) {
        return uri.replaceAll("[^a-zA-Z0-9/\\-_]", "").replace("/", "_");
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max);
    }

    // 从 Security Context 尝试获取 userId（非必须）
    private Long getUserId(HttpServletRequest request) {
        try {
            return cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
