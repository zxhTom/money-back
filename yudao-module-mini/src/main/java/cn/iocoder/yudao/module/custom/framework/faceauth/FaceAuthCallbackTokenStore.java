package cn.iocoder.yudao.module.custom.framework.faceauth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 人脸核身回调防伪：idCard <-> verify_token 的一次性映射。
 * 写入方：FaceAuthController#startFaceAuth（此时同时持有 idCard 和刚生成的 verify_token）。
 * 消费方：MiniCallbackController#handleCallback（回调时只有 idCard，反查 verify_token 后拿去问百度权威结果）。
 * get 只读不删；delete 需由调用方在拿到确定结果（通过/不通过）后显式调用，避免结果不确定时误删映射。
 */
@Component
public class FaceAuthCallbackTokenStore {

    private static final String KEY_PREFIX = "face:verify:token:by:idcard:";
    private static final long TTL_MINUTES = 30;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void store(String idCard, String verifyToken) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + idCard, verifyToken, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public String get(String idCard) {
        return stringRedisTemplate.opsForValue().get(KEY_PREFIX + idCard);
    }

    public void delete(String idCard) {
        stringRedisTemplate.delete(KEY_PREFIX + idCard);
    }
}
