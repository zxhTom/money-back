package cn.iocoder.yudao.module.system.service.risk;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskUserLockLogMapper;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.system.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RiskLockServiceImpl implements RiskLockService {

    private static final String REDIS_KEY_PWD_ERR = "risk:pwd_err:";
    private static final String REDIS_KEY_LOCK_STATUS = "risk:lock_status:";
    private static final int MAX_PWD_ERR_COUNT = 5;
    private static final int LOCK_MINUTES = 30;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RiskUserLockLogMapper riskUserLockLogMapper;

    @Override
    public void processPasswordError(Long userId) {
        String errKey = REDIS_KEY_PWD_ERR + userId;
        Long errCount = stringRedisTemplate.opsForValue().increment(errKey);
        if (errCount != null && errCount == 1) {
            stringRedisTemplate.expire(errKey, 24, TimeUnit.HOURS);
        }
        if (errCount != null && errCount >= MAX_PWD_ERR_COUNT) {
            lockUser(userId, "PASSWORD_ERR", LOCK_MINUTES);
            stringRedisTemplate.delete(errKey);
        }
    }

    @Override
    public void checkLockStatus(Long userId) {
        String lockKey = REDIS_KEY_LOCK_STATUS + userId;
        String lockEndTimeStr = stringRedisTemplate.opsForValue().get(lockKey);
        if (lockEndTimeStr != null) {
            long lockEndTime = Long.parseLong(lockEndTimeStr);
            if (lockEndTime == -1 || lockEndTime > System.currentTimeMillis()) {
                // throw a runtime exception with a custom message. Using a generic code here since we don't want to create new enums yet.
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED);
            } else {
                stringRedisTemplate.delete(lockKey);
            }
        }
    }

    private void lockUser(Long userId, String lockType, int lockMinutes) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now();
        java.time.LocalDateTime endTime = startTime.plusMinutes(lockMinutes);
        
        RiskUserLockLogDO logDO = RiskUserLockLogDO.builder()
                .userId(userId)
                .lockType(lockType)
                .lockStartTime(startTime)
                .lockEndTime(endTime)
                .escalationLevel(1)
                .build();
        riskUserLockLogMapper.insert(logDO);

        String lockKey = REDIS_KEY_LOCK_STATUS + userId;
        long endTimestamp = java.time.ZoneId.systemDefault().getRules().getOffset(endTime).getTotalSeconds() * 1000L + endTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        // better to use System.currentTimeMillis() + lockMinutes * 60 * 1000L for timestamp
        long endTs = System.currentTimeMillis() + lockMinutes * 60 * 1000L;
        stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(endTs), lockMinutes, TimeUnit.MINUTES);
        log.warn("[lockUser][用户({}) 因 {} 被锁定 {} 分钟]", userId, lockType, lockMinutes);
    }
}
