package cn.iocoder.yudao.module.custom.service.contract;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Records only user IDs and hashed identities; no names or ID cards enter Redis. */
@Component
@Slf4j
public class ContractGuessFeedbackStore {

    private static final String PREFIX = "money:guess:v1:";
    private static final int IMPRESSION_MINUTES = 30;
    private static final int EXTERNAL_DAILY_LIMIT = 3;
    private static final DefaultRedisScript<Long> RESERVE_EXTERNAL = new DefaultRedisScript<>(
            "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then return 1 end " +
            "if redis.call('SCARD', KEYS[1]) >= tonumber(ARGV[2]) then return 0 end " +
            "redis.call('SADD', KEYS[1], ARGV[1]); redis.call('EXPIRE', KEYS[1], 172800); return 1", Long.class);

    @Resource
    private StringRedisTemplate redis;

    /** Count distinct strangers shown to a verified caller, across both directions and all pages. */
    public boolean reserveExternalExposure(Long tenantId, Long callerId, Long targetId) {
        if (targetId == null) return false;
        String day = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        try {
            Long allowed = redis.execute(RESERVE_EXTERNAL,
                    Collections.singletonList(PREFIX + "external:" + tenantId + ':' + callerId + ':' + day),
                    targetId.toString(), Integer.toString(EXTERNAL_DAILY_LIMIT));
            return Long.valueOf(1).equals(allowed);
        } catch (RuntimeException ex) {
            log.warn("Contract guess exposure guard unavailable; hiding external candidates", ex);
            return false;
        }
    }

    public String createExternalToken(Long tenantId, Long callerId, String direction, Long targetId) {
        if (targetId == null) return null;
        String token = UUID.randomUUID().toString();
        try {
            redis.opsForValue().set(externalTokenKey(tenantId, callerId, direction, token),
                    targetId.toString(), IMPRESSION_MINUTES, TimeUnit.MINUTES);
            return token;
        } catch (RuntimeException ex) {
            log.warn("Contract guess candidate token unavailable", ex);
            return null;
        }
    }

    public Long consumeExternalToken(Long tenantId, Long callerId, String direction, String token) {
        try {
            String id = UUID.fromString(token).toString();
            String value = redis.opsForValue().getAndDelete(externalTokenKey(tenantId, callerId, direction, id));
            return value == null ? null : Long.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (RuntimeException ex) {
            log.warn("Contract guess candidate token unavailable", ex);
            return null;
        }
    }

    public Map<Long, Double> selectionCounts(Long tenantId, Long callerId, String direction) {
        Map<Long, Double> result = new HashMap<>();
        try {
            Set<ZSetOperations.TypedTuple<String>> values = redis.opsForZSet()
                    .rangeWithScores(choiceKey(tenantId, callerId, direction), 0, -1);
            if (values != null) {
                for (ZSetOperations.TypedTuple<String> value : values) {
                    if (value.getValue() == null || value.getScore() == null) continue;
                    try { result.put(Long.valueOf(value.getValue()), value.getScore()); }
                    catch (NumberFormatException ignored) { /* Ignore unrelated old data. */ }
                }
            }
        } catch (RuntimeException ex) {
            log.warn("Contract guess feedback unavailable; ranking without it", ex);
        }
        return result;
    }

    public String createImpression(Long tenantId, Long callerId, String direction, Map<Long, String> offered) {
        if (offered.isEmpty()) return null;
        String id = UUID.randomUUID().toString();
        StringBuilder value = new StringBuilder();
        offered.forEach((userId, plainIdCard) -> {
            if (userId == null || plainIdCard == null || plainIdCard.isEmpty()) return;
            if (value.length() > 0) value.append(',');
            value.append(userId).append(':').append(hash(plainIdCard));
        });
        if (value.length() == 0) return null;
        try {
            redis.opsForValue().set(impressionKey(tenantId, callerId, direction, id),
                    value.toString(), IMPRESSION_MINUTES, TimeUnit.MINUTES);
            return id;
        } catch (RuntimeException ex) {
            log.warn("Contract guess impression unavailable", ex);
            return null;
        }
    }

    public boolean recordSelection(Long tenantId, Long callerId, String direction,
                                   String impressionId, String plainIdCard) {
        if (plainIdCard == null || plainIdCard.isEmpty()) return false;
        try {
            String id = UUID.fromString(impressionId).toString();
            String offered = redis.opsForValue().getAndDelete(impressionKey(tenantId, callerId, direction, id));
            if (offered == null) return false;
            String wantedHash = hash(plainIdCard);
            for (String entry : offered.split(",")) {
                String[] parts = entry.split(":", 2);
                if (parts.length != 2 || !parts[1].equals(wantedHash)) continue;
                Long targetId = Long.valueOf(parts[0]);
                incrementSelection(tenantId, callerId, direction, targetId);
                return true;
            }
        } catch (IllegalArgumentException ex) {
            return false;
        } catch (RuntimeException ex) {
            log.warn("Contract guess selection could not be recorded", ex);
        }
        return false;
    }

    public void recordResolvedSelection(Long tenantId, Long callerId, String direction, Long targetId) {
        try {
            incrementSelection(tenantId, callerId, direction, targetId);
        } catch (RuntimeException ex) {
            log.warn("Contract guess selection could not be recorded", ex);
        }
    }

    private void incrementSelection(Long tenantId, Long callerId, String direction, Long targetId) {
        String key = choiceKey(tenantId, callerId, direction);
        redis.opsForZSet().incrementScore(key, targetId.toString(), 1.0);
        redis.opsForHash().put(key + ":last", targetId.toString(), Instant.now().toString());
    }

    private String choiceKey(Long tenantId, Long callerId, String direction) {
        return PREFIX + "choices:" + tenantId + ':' + callerId + ':' + direction;
    }

    private String impressionKey(Long tenantId, Long callerId, String direction, String id) {
        return PREFIX + "impression:" + tenantId + ':' + callerId + ':' + direction + ':' + id;
    }

    private String externalTokenKey(Long tenantId, Long callerId, String direction, String id) {
        return PREFIX + "external-token:" + tenantId + ':' + callerId + ':' + direction + ':' + id;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
