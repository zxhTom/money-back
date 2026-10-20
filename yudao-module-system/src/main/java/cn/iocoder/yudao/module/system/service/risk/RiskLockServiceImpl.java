package cn.iocoder.yudao.module.system.service.risk;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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
import java.util.Map;
import java.util.LinkedHashMap;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RiskLockServiceImpl implements RiskLockService {

    private static final String REDIS_KEY_PWD_ERR = "risk:pwd_err:";
    private static final String REDIS_KEY_LOCK_STATUS = "risk:lock_status:";
    private static final int MAX_PWD_ERR_COUNT = 5;
    private static final int LOCK_MINUTES = 30;

    @Resource
    private DataSource dataSource;

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
                if (lockEndTime == -1) throw ServiceExceptionUtil.exception(ErrorCodeConstants.AUTH_LOGIN_RISK_LOCKED);
                List<RiskUserLockLogDO> logs = riskUserLockLogMapper.selectList(new LambdaQueryWrapperX<RiskUserLockLogDO>()
                        .eq(RiskUserLockLogDO::getUserId, userId).orderByDesc(RiskUserLockLogDO::getId).last("LIMIT 1"));
                String type = logs.isEmpty() ? "" : logs.get(0).getLockType();
                String reason = "PASSWORD_ERR".equals(type) ? "密码错误次数达到上限" : "RATE_LIMIT".equals(type) ? "操作过于频繁" : "安全风控";
                String until = Instant.ofEpochMilli(lockEndTime).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                long minutes = Math.max(1, (long) Math.ceil((lockEndTime - System.currentTimeMillis()) / 60000.0));
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.AUTH_LOGIN_TEMPORARILY_LOCKED, reason, until, minutes);
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

    protected JdbcTemplate jdbc() { return new JdbcTemplate(dataSource); }

    private Integer requireTenantUser(Long userId) {
        if (userId == null) throw ServiceExceptionUtil.exception(ErrorCodeConstants.USER_NOT_EXISTS);
        List<Integer> statuses = jdbc().query(
                "SELECT status FROM system_users WHERE id=? AND tenant_id=? AND deleted=0",
                (rs, n) -> rs.getInt("status"), userId, TenantContextHolder.getRequiredTenantId());
        if (statuses.size() != 1) throw ServiceExceptionUtil.exception(ErrorCodeConstants.USER_NOT_EXISTS);
        return statuses.get(0);
    }

    @Override
    public Map<String, Object> getLoginSecurity(Long userId) {
        int status = requireTenantUser(userId);
        long now = System.currentTimeMillis();
        String lockValue = stringRedisTemplate.opsForValue().get(REDIS_KEY_LOCK_STATUS + userId);
        long lockUntil = lockValue == null ? 0 : Long.parseLong(lockValue);
        String errors = stringRedisTemplate.opsForValue().get(REDIS_KEY_PWD_ERR + userId);
        Long countTtl = stringRedisTemplate.getExpire(REDIS_KEY_PWD_ERR + userId, TimeUnit.SECONDS);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId); result.put("accountStatus", status); result.put("serverTime", now);
        result.put("locked", lockUntil == -1 || lockUntil > now);
        result.put("permanent", lockUntil == -1);
        result.put("lockEndTime", lockUntil > 0 ? lockUntil : null);
        result.put("passwordErrorCount", errors == null ? 0 : Long.parseLong(errors));
        result.put("passwordErrorLimit", MAX_PWD_ERR_COUNT);
        result.put("counterExpiresAt", countTtl != null && countTtl > 0 ? now + countTtl * 1000L : null);
        JdbcTemplate jdbc = jdbc();
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        result.put("lockHistory", jdbc.query("SELECT lock_type,lock_start_time,lock_end_time,unlock_time,unlocker_id FROM system_risk_user_lock_log WHERE user_id=? AND tenant_id=? AND deleted=0 ORDER BY id DESC LIMIT 10",
                (rs, n) -> {
                    Map<String,Object> item = new LinkedHashMap<>(); item.put("lockType",rs.getString("lock_type"));
                    for (String field : new String[]{"lock_start_time","lock_end_time","unlock_time"}) {
                        java.sql.Timestamp time = rs.getTimestamp(field);
                        String key = "lock_start_time".equals(field) ? "startTime" : "lock_end_time".equals(field) ? "endTime" : "unlockTime";
                        item.put(key, time == null ? null : time.getTime());
                    }
                    item.put("unlockerId",rs.getObject("unlocker_id"));return item;
                }, userId, tenantId));
        result.put("loginHistory", jdbc.query("SELECT create_time,result FROM system_login_log WHERE user_id=? AND tenant_id=? AND user_type=2 AND deleted=0 AND log_type=100 ORDER BY id DESC LIMIT 20",
                (rs,n) -> { Map<String,Object> item=new LinkedHashMap<>();item.put("time",rs.getTimestamp("create_time").getTime());item.put("result",rs.getInt("result"));return item; }, userId, tenantId));
        return result;
    }

    @Override
    public void unlockUser(Long adminUserId, Long targetUserId) {
        if (targetUserId == null) {
            return;
        }
        requireTenantUser(targetUserId);
        LocalDateTime now = LocalDateTime.now();
        // 1. 查询该 targetUserId 尚未结束的锁定记录（lock_end_time > now() 或为 null）
        List<RiskUserLockLogDO> activeLogs = riskUserLockLogMapper.selectList(new LambdaQueryWrapperX<RiskUserLockLogDO>()
                .eq(RiskUserLockLogDO::getUserId, targetUserId)
                .eq(RiskUserLockLogDO::getTenantId, TenantContextHolder.getRequiredTenantId())
                .and(w -> w.isNull(RiskUserLockLogDO::getLockEndTime).or().gt(RiskUserLockLogDO::getLockEndTime, now)));

        // 2. 将其更新：unlock_time = now(), unlocker_id = adminUserId, 如果 lock_end_time 为空或在未来，则将其改为 now()（即立刻结束）
        for (RiskUserLockLogDO logDO : activeLogs) {
            logDO.setUnlockTime(now);
            logDO.setUnlockerId(adminUserId);
            if (logDO.getLockEndTime() == null || logDO.getLockEndTime().isAfter(now)) {
                logDO.setLockEndTime(now);
            }
            riskUserLockLogMapper.updateById(logDO);
        }

        // 3. 删除 Redis 中的限制锁
        stringRedisTemplate.delete(REDIS_KEY_LOCK_STATUS + targetUserId);

        // 4. 清除密码错误次数计数器
        stringRedisTemplate.delete(REDIS_KEY_PWD_ERR + targetUserId);

        log.info("[unlockUser][管理员({}) 手动解锁用户({})，更新了 {} 条锁定记录]", adminUserId, targetUserId, activeLogs.size());
    }
}

