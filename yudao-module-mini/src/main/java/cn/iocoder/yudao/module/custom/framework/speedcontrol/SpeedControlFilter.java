package cn.iocoder.yudao.module.custom.framework.speedcontrol;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.core.filter.ApiRequestFilter;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import cn.iocoder.yudao.module.custom.framework.security.config.CustomWebMvcConfig;
import cn.iocoder.yudao.module.custom.service.speedcontrol.SpeedControlCalculator;
import cn.iocoder.yudao.module.custom.service.speedcontrol.SpeedControlConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.AntPathMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 访问速度控制过滤器：按配置给接口注入等待，人为拉长响应时间。
 *
 * 注册顺序 -97（Spring Security 的 -100 之后），所以进入时能直接拿到登录用户做豁免判断。
 *
 * 为什么是阻塞 sleep 而不是 startAsync 释放线程：外层的 {@code DataEncryptFilter} 继承
 * OncePerRequestFilter，在 chain.doFilter 之后读缓冲区改写响应体，它不是 async-aware；
 * 内层一旦 startAsync 就会让它拿到空响应体，响应加密会坏掉。代价是延迟期间占住 Tomcat 线程，
 * 需要相应调大 server.tomcat.threads.max。
 *
 * 延迟量基于该接口的历史"净耗时"移动平均（EMA，存 Redis），不是本次实测值——
 * 用历史均值才能保证同一个接口每次的限速时长稳定，且接口之间的快慢relation不被单次抖动打乱。
 */
@Slf4j
public class SpeedControlFilter extends ApiRequestFilter {

    private static final String BASELINE_KEY_PREFIX = "speedctl:base:";
    private static final long BASELINE_TTL_DAYS = 7L;
    private static final double EMA_ALPHA = 0.2d;
    private static final long MAX_SINGLE_SLEEP_MS = 30 * 60 * 1000L;

    /**
     * 永不限速的路径。全部硬编码、不可配——配错时间把自己锁在外面时，
     * 这几条是唯一还能进后台把限速关掉的通道。
     */
    private static final String[] HARD_EXCLUDES = {
            "/**/custom/speed-control/**",   // 限速配置接口自身
            "/**/system/auth/**",            // 后台登录/登出/验证码/刷新令牌/权限信息
            "/**/member/auth/**",            // 小程序端登录
            "/**/system/menu/**",            // 后台菜单路由，否则登录后进不去首页
            "/actuator/**"                   // 健康检查
    };

    private final SpeedControlConfigService speedControlConfigService;
    private final StringRedisTemplate stringRedisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public SpeedControlFilter(WebProperties webProperties,
                              SpeedControlConfigService speedControlConfigService,
                              StringRedisTemplate stringRedisTemplate) {
        super(webProperties);
        this.speedControlConfigService = speedControlConfigService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (isExcluded(uri)) {
            chain.doFilter(request, response);
            return;
        }

        SpeedControlConfigDO config = speedControlConfigService.getCachedConfig();
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            chain.doFilter(request, response);
            return;
        }
        if (speedControlConfigService.isExempt(tryGetUserId())) {
            chain.doFilter(request, response);
            return;
        }

        String baselineKey = buildBaselineKey(request);
        Long baseline = readBaseline(baselineKey);
        if (baseline != null) {
            // 冷启动（还没有基线）时本次不延迟，只测量并记录，避免拿默认值放大出离谱时长
            sleepQuietly(SpeedControlCalculator.computeDelayMs(baseline, config));
        }

        long startNanos = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long actualMs = (System.nanoTime() - startNanos) / 1_000_000L;
            writeBaseline(baselineKey, baseline, actualMs);
        }
    }

    boolean isExcluded(String uri) {
        // 限速配置接口做双保险：Ant 匹配之外再兜一次朴素包含判断
        if (uri.contains("/custom/speed-control/")) {
            return true;
        }
        for (String pattern : HARD_EXCLUDES) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        // 第三方回调（微信/支付网关）：它们不会等几分钟，限速会直接打断支付与消息回调
        for (String pattern : CustomWebMvcConfig.WEBHOOK_EXCLUDES) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    /** 把路径里的数字段归一成 {id}，避免 /contract/123 这类 RESTful 路径把 key 撑爆 */
    private String buildBaselineKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        StringBuilder sb = new StringBuilder(BASELINE_KEY_PREFIX);
        sb.append(request.getMethod()).append(':');
        for (String segment : uri.split("/")) {
            if (segment.isEmpty()) {
                continue;
            }
            sb.append('/').append(isNumeric(segment) ? "{id}" : segment);
        }
        return sb.toString();
    }

    private static boolean isNumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return !s.isEmpty();
    }

    private Long readBaseline(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            return value == null ? null : Long.valueOf(value);
        } catch (Exception e) {
            log.warn("[SpeedControl] 读取基线失败，本次不限速：{}", e.getMessage());
            return null;
        }
    }

    private void writeBaseline(String key, Long previous, long actualMs) {
        try {
            long next = previous == null
                    ? actualMs
                    : Math.round(EMA_ALPHA * actualMs + (1 - EMA_ALPHA) * previous);
            stringRedisTemplate.opsForValue().set(key, String.valueOf(Math.max(next, 0L)),
                    BASELINE_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("[SpeedControl] 写入基线失败：{}", e.getMessage());
        }
    }

    private void sleepQuietly(long delayMs) {
        if (delayMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(Math.min(delayMs, MAX_SINGLE_SLEEP_MS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Long tryGetUserId() {
        try {
            return SecurityFrameworkUtils.getLoginUserId();
        } catch (Exception e) {
            return null;
        }
    }

}
