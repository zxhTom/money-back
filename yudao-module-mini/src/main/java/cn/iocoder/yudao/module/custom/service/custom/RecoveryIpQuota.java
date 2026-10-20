package cn.iocoder.yudao.module.custom.service.custom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Collections;
import cn.hutool.crypto.digest.DigestUtil;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

@Component
public class RecoveryIpQuota {
    static final String RESERVE = "local n=tonumber(redis.call('get',KEYS[1]) or '0'); if n>=tonumber(ARGV[1]) then return -1 end; n=redis.call('incr',KEYS[1]); if n==1 then redis.call('expire',KEYS[1],ARGV[2]) end; return n";
    static final String RELEASE = "local n=tonumber(redis.call('get',KEYS[1]) or '0'); if n>0 then return redis.call('decr',KEYS[1]) end; return 0";
    @Resource private StringRedisTemplate stringRedisTemplate;
    public String reserve(String ip, int hours, int limit) {
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) throw exception0(400, "无法确认请求来源，请稍后再试");
        String key = "security:password-recovery:ip:" + DigestUtil.sha256Hex(ip.trim());
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(RESERVE, Long.class), Collections.singletonList(key), String.valueOf(limit), String.valueOf(hours * 3600L));
        if (result == null) throw exception0(503, "暂时无法重置密码，请稍后再试");
        if (result < 0) throw exception0(429, "同一IP在" + hours + "小时内最多修改" + limit + "次密码，请稍后再试");
        return key;
    }
    public void release(String key) {
        stringRedisTemplate.execute(new DefaultRedisScript<>(RELEASE, Long.class), Collections.singletonList(key));
    }
}
