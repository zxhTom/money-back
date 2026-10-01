package cn.iocoder.yudao.module.system.service.risk;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskRateLimitConfigDO;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskRateLimitConfigMapper;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskUserLockLogMapper;
import cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.system.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
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

    @Resource
    private RiskRateLimitConfigMapper riskRateLimitConfigMapper;

    @Override
    public void processPasswordError(Long userId) {
        String errKey = REDIS_KEY_PWD_ERR + userId;
        Long errCount = stringRedisTemplate.opsForValue().increment(errKey);
        if (errCount != null && errCount == 1) {
            stringRedisTemplate.expire(errKey, 24, TimeUnit.HOURS);
        }
        if (errCount != null && errCount >= MAX_PWD_ERR_COUNT) {
            lockUser(userId, "PASSWORD_ERR", LOCK_MINUTES * 60L, 1);
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

    @Override
    @Cacheable(cacheNames = RedisKeyConstants.RISK_RATE_LIMIT_RULES, key = "'all'", unless = "#result == null")
    public List<RiskRateLimitConfigDO> getAllEnableRateLimitRules() {
        return riskRateLimitConfigMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public void escalateLock(Long userId, String lockType, RiskRateLimitConfigDO rule) {
        // 1. 从 system_risk_user_lock_log 查询该用户最近一次被该类规则锁定的记录，获取其 escalation_level
        RiskUserLockLogDO latestLog = riskUserLockLogMapper.selectLatestByUserIdAndLockType(userId, lockType);
        int escalationLevel = 1;
        if (latestLog != null && latestLog.getEscalationLevel() != null) {
            // 如果上一次锁定结束时间（或创建时间）距今超过 24 小时，视为很久以前，重新从 1 开始计
            LocalDateTime referenceTime = latestLog.getLockEndTime() != null ? latestLog.getLockEndTime() : latestLog.getCreateTime();
            if (referenceTime == null || referenceTime.isBefore(LocalDateTime.now().minusHours(24))) {
                escalationLevel = 1;
            } else {
                escalationLevel = latestLog.getEscalationLevel() + 1;
            }
        }

        // 2. 解析 rule.lockDurationLadder (如 "300,3600,-1")。根据 escalation_level 决定本次锁定的时间
        String ladderStr = (rule != null) ? rule.getLockDurationLadder() : null;
        String[] ladder = StrUtil.isNotBlank(ladderStr) ? ladderStr.split(",") : new String[]{"300"};
        int index = escalationLevel - 1;
        if (index < 0) {
            index = 0;
        } else if (index >= ladder.length) {
            index = ladder.length - 1;
        }

        long lockSeconds;
        try {
            lockSeconds = Long.parseLong(ladder[index].trim());
        } catch (NumberFormatException e) {
            log.error("[escalateLock][解析阶梯锁定时间失败: {}]", ladder[index], e);
            lockSeconds = 300L;
        }

        // 3. 使用 lockUser 私有方法，向 DB 插入新的锁定日志，并更新 Redis 锁
        lockUser(userId, lockType, lockSeconds, escalationLevel);
    }

    private void lockUser(Long userId, String lockType, long lockSeconds, int escalationLevel) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = null;
        if (lockSeconds > 0) {
            endTime = startTime.plusSeconds(lockSeconds);
        }

        RiskUserLockLogDO logDO = RiskUserLockLogDO.builder()
                .userId(userId)
                .lockType(lockType)
                .lockStartTime(startTime)
                .lockEndTime(endTime)
                .escalationLevel(escalationLevel)
                .build();
        riskUserLockLogMapper.insert(logDO);

        String lockKey = REDIS_KEY_LOCK_STATUS + userId;
        if (lockSeconds <= -1) {
            // 永久锁定：Redis 设极大过期时间，值为 -1，DB 结束时间设为 NULL
            stringRedisTemplate.opsForValue().set(lockKey, "-1", 3650, TimeUnit.DAYS);
            log.warn("[lockUser][用户({}) 因 {} 被永久锁定，阶梯等级: {}]", userId, lockType, escalationLevel);
        } else {
            long endTs = System.currentTimeMillis() + lockSeconds * 1000L;
            stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(endTs), lockSeconds, TimeUnit.SECONDS);
            log.warn("[lockUser][用户({}) 因 {} 被锁定 {} 秒，阶梯等级: {}]", userId, lockType, lockSeconds, escalationLevel);
        }
    }
}

