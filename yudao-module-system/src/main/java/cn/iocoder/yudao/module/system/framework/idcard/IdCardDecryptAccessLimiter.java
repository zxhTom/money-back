package cn.iocoder.yudao.module.system.framework.idcard;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_ID_CARD_DECRYPT_DISTINCT_LIMIT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_ID_CARD_DECRYPT_RATE_LIMIT;

/**
 * 身份证解密接口：按用户限流 + 每周期内「不同密文」种数上限（Redis）。
 */
@Component
public class IdCardDecryptAccessLimiter {

    /**
     * 返回值：0=超过不同证件上限；1=该密文本周期已出现过；2=本周期内新增一种密文
     */
    private static final DefaultRedisScript<Long> RESERVE_DISTINCT = new DefaultRedisScript<>();
    static {
        RESERVE_DISTINCT.setResultType(Long.class);
        RESERVE_DISTINCT.setScriptText(
                "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then "
                        + "return 1 end "
                        + "local n = redis.call('SCARD', KEYS[1]) "
                        + "if n >= tonumber(ARGV[2]) then return 0 end "
                        + "redis.call('SADD', KEYS[1], ARGV[1]) "
                        + "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3])) "
                        + "return 2");
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IdCardCipherProperties idCardCipherProperties;

    public void checkRequestRate(Long userId) {
        IdCardCipherProperties.DecryptApi cfg = idCardCipherProperties.getDecryptApi();
        if (userId == null || !cfg.isRateLimitEnabled()) {
            return;
        }
        String key = "idcard:dec:rate:" + userId;
        Long c = stringRedisTemplate.opsForValue().increment(key);
        if (c != null && c == 1L) {
            stringRedisTemplate.expire(key, cfg.getRateWindowSeconds(), TimeUnit.SECONDS);
        }
        if (c != null && c > cfg.getRateMaxRequests()) {
            throw exception(USER_ID_CARD_DECRYPT_RATE_LIMIT, cfg.getRateWindowSeconds(), cfg.getRateMaxRequests());
        }
    }

    /**
     * @return 1=密文本周期已占用名额；2=本次新占用一个「不同密文」名额（解密失败时需 rollback）
     */
    public int reserveDistinctCipherSlot(Long userId, String cipherTrimmed) {
        IdCardCipherProperties.DecryptApi cfg = idCardCipherProperties.getDecryptApi();
        if (userId == null || !cfg.isDistinctLimitEnabled()) {
            return 1;
        }
        long now = System.currentTimeMillis() / 1000;
        long window = Math.max(1, cfg.getDistinctWindowSeconds());
        long bucketStart = (now / window) * window;
        String uniqKey = "idcard:dec:uq:" + userId + ":" + bucketStart;
        String fp = DigestUtil.sha256Hex(cipherTrimmed);
        int ttl = Math.min(Integer.MAX_VALUE, cfg.getDistinctWindowSeconds() * 2);
        Long r = stringRedisTemplate.execute(RESERVE_DISTINCT,
                Collections.singletonList(uniqKey),
                fp, String.valueOf(cfg.getDistinctMaxPerWindow()), String.valueOf(ttl));
        if (r == null || r == 0L) {
            throw exception(USER_ID_CARD_DECRYPT_DISTINCT_LIMIT, cfg.getDistinctWindowSeconds(), cfg.getDistinctMaxPerWindow());
        }
        return r.intValue();
    }

    /**
     * 解密失败时，若此前 {@link #reserveDistinctCipherSlot} 返回 2，则回滚名额
     */
    public void rollbackNewDistinctSlot(Long userId, String cipherTrimmed, int reserveResult) {
        if (userId == null || reserveResult != 2) {
            return;
        }
        IdCardCipherProperties.DecryptApi cfg = idCardCipherProperties.getDecryptApi();
        if (!cfg.isDistinctLimitEnabled()) {
            return;
        }
        long now = System.currentTimeMillis() / 1000;
        long window = Math.max(1, cfg.getDistinctWindowSeconds());
        long bucketStart = (now / window) * window;
        String uniqKey = "idcard:dec:uq:" + userId + ":" + bucketStart;
        String fp = DigestUtil.sha256Hex(cipherTrimmed);
        stringRedisTemplate.opsForSet().remove(uniqKey, fp);
    }
}
