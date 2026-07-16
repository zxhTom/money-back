package cn.iocoder.yudao.module.custom.service.contract;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 合同查询类接口（/custom/contract/page 等）的按用户风控节流。
 *
 * 与 {@link cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter} 硬拒绝不同，
 * 这里不拒绝请求，短时间内请求过于频繁时人为增加响应延迟（越频繁延迟越久，封顶3秒），
 * 既不影响正常业务的可用性，又让批量试探/爬取的成本显著上升。
 */
@Component
@Slf4j
public class ContractQueryRiskControlGuard {

    /** 正常窗口内允许的请求数，超过后开始计入违规、触发降速 */
    private static final int NORMAL_LIMIT = 20;
    /** 计数窗口（秒） */
    private static final long WINDOW_SECONDS = 60;
    /** 违规计数窗口（秒），比普通窗口长，用于让"最近是否有过风控记录"更持久 */
    private static final long VIOLATION_WINDOW_SECONDS = 600;
    /** 单次违规的延迟基数（毫秒） */
    private static final long DELAY_STEP_MS = 300;
    /** 延迟上限（毫秒） */
    private static final long DELAY_CAP_MS = 3000;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void beforeQuery(Long userId) {
        if (userId == null) {
            return;
        }
        long count = increment("contract:page:count:" + userId, WINDOW_SECONDS);
        if (count <= NORMAL_LIMIT) {
            return;
        }
        long violations = increment("contract:page:violation:" + userId, VIOLATION_WINDOW_SECONDS);
        long delayMs = calculateDelayMs(violations);
        log.warn("[SECURITY][THROTTLE] userId={} ip={} 窗口内请求数={} 违规次数={} 触发风控，延迟{}ms后返回",
                userId, currentClientIp(), count, violations, delayMs);
        sleep(delayMs);
    }

    static long calculateDelayMs(long violations) {
        return Math.min(violations * DELAY_STEP_MS, DELAY_CAP_MS);
    }

    private long increment(String key, long ttlSeconds) {
        Long value = stringRedisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return value == null ? 0L : value;
    }

    private void sleep(long delayMs) {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String currentClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return cn.hutool.extra.servlet.ServletUtil.getClientIP(request);
        } catch (Exception e) {
            return "unknown";
        }
    }

}
