package cn.iocoder.yudao.module.custom.service.contract;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
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

    @Resource
    private StringRedisTemplate redis;

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
                String key = choiceKey(tenantId, callerId, direction);
                redis.opsForZSet().incrementScore(key, targetId.toString(), 1.0);
                redis.opsForHash().put(key + ":last", targetId.toString(), Instant.now().toString());
                return true;
            }
        } catch (IllegalArgumentException ex) {
            return false;
        } catch (RuntimeException ex) {
            log.warn("Contract guess selection could not be recorded", ex);
        }
        return false;
    }

    private String choiceKey(Long tenantId, Long callerId, String direction) {
        return PREFIX + "choices:" + tenantId + ':' + callerId + ':' + direction;
    }

    private String impressionKey(Long tenantId, Long callerId, String direction, String id) {
        return PREFIX + "impression:" + tenantId + ':' + callerId + ':' + direction + ':' + id;
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
