package cn.iocoder.yudao.module.system.service.monitor;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * voidmob IP 检测统一客户端。
 *
 * 功能：
 * 1. 动态感知频率上限：第一次触发限速后，将当前窗口的安全调用量记入 Redis（TTL 1h），
 *    后续调用在触顶前主动等到下一分钟窗口，避免再次触发。
 * 2. 出错/限速自动等待 15s 重试一次，之后返回 null，调用方循环不会因单次失败而中断。
 */
@Component
@Slf4j
public class VoidmobIpCheckClient {

    private static final String URL_TPL          = "https://voidmob.com/api/tools/ip-check?ip=%s";
    /** 已发现的每分钟安全调用上限，TTL 1h（防止边界误判后频繁重新学习） */
    private static final String KEY_RATE_LIMIT   = "voidmob:rate_limit";
    /** 当前分钟窗口的调用计数，key 后缀为分钟级时间戳，TTL 2min */
    private static final String KEY_WINDOW_PFX   = "voidmob:window:";
    private static final int    TIMEOUT_MS        = 8_000;
    private static final long   RETRY_WAIT_MS     = 15_000L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询单个 IP。内置限速 + 错误重试。
     * @return 接口 JSON 响应，失败返回 null
     */
    public JSONObject check(String ip) {
        return doCheck(ip, true);
    }

    private JSONObject doCheck(String ip, boolean allowRetry) {
        throttleIfNeeded(ip);

        HttpResponse resp = null;
        try {
            resp = HttpRequest.get(String.format(URL_TPL, ip))
                    .timeout(TIMEOUT_MS)
                    .execute();

            int    status = resp.getStatus();
            String body   = resp.body();

            // 无论成功与否都计入本分钟窗口（乐观计数，保证不低估）
            incrementWindow();

            if (status == 429 || isRateLimitBody(body)) {
                recordDiscoveredLimit();
                if (allowRetry) {
                    log.warn("[VoidmobClient] 触发限速，等待 {}s 后重试 ip={}", RETRY_WAIT_MS / 1000, ip);
                    sleep(RETRY_WAIT_MS);
                    return doCheck(ip, false);
                }
                log.warn("[VoidmobClient] 重试后仍限速，放弃 ip={}", ip);
                return null;
            }

            if (status != 200) {
                log.warn("[VoidmobClient] HTTP {} ip={}", status, ip);
                if (allowRetry) {
                    sleep(RETRY_WAIT_MS);
                    return doCheck(ip, false);
                }
                return null;
            }

            return JSON.parseObject(body);

        } catch (Exception e) {
            log.warn("[VoidmobClient] 请求异常 ip={} err={}", ip, e.getMessage());
            if (allowRetry) {
                sleep(RETRY_WAIT_MS);
                return doCheck(ip, false);
            }
            return null;
        } finally {
            if (resp != null) {
                try { resp.close(); } catch (Exception ignored) {}
            }
        }
    }

    // ── 限速控制 ──────────────────────────────────────────────────

    private void throttleIfNeeded(String ip) {
        String limitStr = stringRedisTemplate.opsForValue().get(KEY_RATE_LIMIT);
        if (limitStr == null) return;

        int limit = Integer.parseInt(limitStr);
        int count = currentWindowCount();

        if (count >= limit) {
            long waitMs = millisUntilNextMinute() + 200;
            log.info("[VoidmobClient] 接近限速 ({}/{}/min)，等待 {}ms ip={}", count, limit, waitMs, ip);
            sleep(waitMs);
        }
    }

    private void incrementWindow() {
        String key = KEY_WINDOW_PFX + minuteEpoch();
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, 2, TimeUnit.MINUTES);
    }

    private int currentWindowCount() {
        String val = stringRedisTemplate.opsForValue().get(KEY_WINDOW_PFX + minuteEpoch());
        return val != null ? Integer.parseInt(val) : 0;
    }

    /**
     * 触发限速时，把「当前窗口已成功调用数 - 1」记为安全上限。
     * 只在发现的值比已记录的更严格时才覆写，避免偶发 503 误判为限速。
     */
    private void recordDiscoveredLimit() {
        int count     = currentWindowCount();
        int safeLimit = Math.max(1, count - 1);

        String existingStr = stringRedisTemplate.opsForValue().get(KEY_RATE_LIMIT);
        if (existingStr == null || Integer.parseInt(existingStr) > safeLimit) {
            log.warn("[VoidmobClient] 记录频率上限: {}次/分钟 (TTL=1h)", safeLimit);
            stringRedisTemplate.opsForValue().set(KEY_RATE_LIMIT, String.valueOf(safeLimit), 1, TimeUnit.HOURS);
        }
    }

    private boolean isRateLimitBody(String body) {
        if (body == null) return false;
        String lower = body.toLowerCase();
        return lower.contains("rate limit") || lower.contains("too many requests")
                || lower.contains("ratelimit") || lower.contains("throttle");
    }

    private long minuteEpoch() {
        return System.currentTimeMillis() / 60_000;
    }

    private long millisUntilNextMinute() {
        long now = System.currentTimeMillis();
        return (now / 60_000 + 1) * 60_000 - now;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
