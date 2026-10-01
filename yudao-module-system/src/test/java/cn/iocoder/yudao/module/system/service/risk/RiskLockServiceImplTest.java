package cn.iocoder.yudao.module.system.service.risk;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskRateLimitConfigDO;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskRateLimitConfigMapper;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskUserLockLogMapper;
import cn.iocoder.yudao.module.system.enums.ErrorCodeConstants;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RiskLockServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private RiskLockServiceImpl riskLockService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private RiskUserLockLogMapper riskUserLockLogMapper;

    @Mock
    private RiskRateLimitConfigMapper riskRateLimitConfigMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    public void testGetAllEnableRateLimitRules() {
        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .apiPattern("/admin-api/trade/**")
                .timeWindow(60)
                .maxCount(10)
                .lockDurationLadder("300,3600,-1")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        when(riskRateLimitConfigMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.singletonList(rule));

        List<RiskRateLimitConfigDO> result = riskLockService.getAllEnableRateLimitRules();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    public void testEscalateLock_FirstTime() {
        Long userId = 100L;
        String lockType = "RATE_LIMIT";
        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .lockDurationLadder("300,3600,-1")
                .build();

        when(riskUserLockLogMapper.selectLatestByUserIdAndLockType(userId, lockType)).thenReturn(null);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        riskLockService.escalateLock(userId, lockType, rule);

        ArgumentCaptor<RiskUserLockLogDO> captor = ArgumentCaptor.forClass(RiskUserLockLogDO.class);
        verify(riskUserLockLogMapper).insert(captor.capture());
        RiskUserLockLogDO inserted = captor.getValue();
        assertEquals(userId, inserted.getUserId());
        assertEquals(lockType, inserted.getLockType());
        assertEquals(1, inserted.getEscalationLevel());
        assertNotNull(inserted.getLockEndTime());

        verify(valueOperations).set(eq("risk:lock_status:" + userId), anyString(), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testEscalateLock_SecondTime() {
        Long userId = 100L;
        String lockType = "RATE_LIMIT";
        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .lockDurationLadder("300,3600,-1")
                .build();

        RiskUserLockLogDO previousLog = RiskUserLockLogDO.builder()
                .id(10L)
                .userId(userId)
                .lockType(lockType)
                .escalationLevel(1)
                .lockStartTime(LocalDateTime.now().minusMinutes(10))
                .lockEndTime(LocalDateTime.now().minusMinutes(5))
                .build();
        previousLog.setCreateTime(LocalDateTime.now().minusMinutes(10));

        when(riskUserLockLogMapper.selectLatestByUserIdAndLockType(userId, lockType)).thenReturn(previousLog);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        riskLockService.escalateLock(userId, lockType, rule);

        ArgumentCaptor<RiskUserLockLogDO> captor = ArgumentCaptor.forClass(RiskUserLockLogDO.class);
        verify(riskUserLockLogMapper).insert(captor.capture());
        RiskUserLockLogDO inserted = captor.getValue();
        assertEquals(2, inserted.getEscalationLevel());
        assertNotNull(inserted.getLockEndTime());

        verify(valueOperations).set(eq("risk:lock_status:" + userId), anyString(), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testEscalateLock_Permanent() {
        Long userId = 100L;
        String lockType = "RATE_LIMIT";
        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .lockDurationLadder("300,3600,-1")
                .build();

        RiskUserLockLogDO previousLog = RiskUserLockLogDO.builder()
                .id(11L)
                .userId(userId)
                .lockType(lockType)
                .escalationLevel(2)
                .lockStartTime(LocalDateTime.now().minusHours(2))
                .lockEndTime(LocalDateTime.now().minusHours(1))
                .build();
        previousLog.setCreateTime(LocalDateTime.now().minusHours(2));

        when(riskUserLockLogMapper.selectLatestByUserIdAndLockType(userId, lockType)).thenReturn(previousLog);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        riskLockService.escalateLock(userId, lockType, rule);

        ArgumentCaptor<RiskUserLockLogDO> captor = ArgumentCaptor.forClass(RiskUserLockLogDO.class);
        verify(riskUserLockLogMapper).insert(captor.capture());
        RiskUserLockLogDO inserted = captor.getValue();
        assertEquals(3, inserted.getEscalationLevel());
        assertNull(inserted.getLockEndTime()); // 永久锁定 DB 结束时间设为 NULL

        verify(valueOperations).set(eq("risk:lock_status:" + userId), eq("-1"), eq(3650L), eq(TimeUnit.DAYS));
    }

    @Test
    public void testEscalateLock_LongAgoReset() {
        Long userId = 100L;
        String lockType = "RATE_LIMIT";
        RiskRateLimitConfigDO rule = RiskRateLimitConfigDO.builder()
                .id(1L)
                .lockDurationLadder("300,3600,-1")
                .build();

        RiskUserLockLogDO previousLog = RiskUserLockLogDO.builder()
                .id(12L)
                .userId(userId)
                .lockType(lockType)
                .escalationLevel(3)
                .lockStartTime(LocalDateTime.now().minusDays(3))
                .lockEndTime(LocalDateTime.now().minusDays(2))
                .build();
        previousLog.setCreateTime(LocalDateTime.now().minusDays(3));

        when(riskUserLockLogMapper.selectLatestByUserIdAndLockType(userId, lockType)).thenReturn(previousLog);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        riskLockService.escalateLock(userId, lockType, rule);

        ArgumentCaptor<RiskUserLockLogDO> captor = ArgumentCaptor.forClass(RiskUserLockLogDO.class);
        verify(riskUserLockLogMapper).insert(captor.capture());
        RiskUserLockLogDO inserted = captor.getValue();
        assertEquals(1, inserted.getEscalationLevel()); // 重置为 1
        assertNotNull(inserted.getLockEndTime());

        verify(valueOperations).set(eq("risk:lock_status:" + userId), anyString(), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testCheckLockStatus_Locked() {
        Long userId = 100L;
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        long futureTs = System.currentTimeMillis() + 100000L;
        when(valueOperations.get("risk:lock_status:" + userId)).thenReturn(String.valueOf(futureTs));

        ServiceException exception = assertThrows(ServiceException.class, () -> riskLockService.checkLockStatus(userId));
        assertEquals(ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED.getCode(), exception.getCode());
    }

    @Test
    public void testCheckLockStatus_PermanentLocked() {
        Long userId = 100L;
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("risk:lock_status:" + userId)).thenReturn("-1");

        ServiceException exception = assertThrows(ServiceException.class, () -> riskLockService.checkLockStatus(userId));
        assertEquals(ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED.getCode(), exception.getCode());
    }

    @Test
    public void testCheckLockStatus_NotLocked() {
        Long userId = 100L;
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("risk:lock_status:" + userId)).thenReturn(null);

        assertDoesNotThrow(() -> riskLockService.checkLockStatus(userId));
    }

    @Test
    public void testUnlockUser_Success() {
        Long adminUserId = 1L;
        Long targetUserId = 100L;

        RiskUserLockLogDO log1 = RiskUserLockLogDO.builder()
                .id(10L)
                .userId(targetUserId)
                .lockType("RATE_LIMIT")
                .lockStartTime(LocalDateTime.now().minusMinutes(10))
                .lockEndTime(null) // 永久锁定
                .build();
        RiskUserLockLogDO log2 = RiskUserLockLogDO.builder()
                .id(11L)
                .userId(targetUserId)
                .lockType("PASSWORD_ERR")
                .lockStartTime(LocalDateTime.now().minusMinutes(5))
                .lockEndTime(LocalDateTime.now().plusMinutes(25)) // 未来结束时间
                .build();

        when(riskUserLockLogMapper.selectList(any())).thenReturn(java.util.Arrays.asList(log1, log2));

        riskLockService.unlockUser(adminUserId, targetUserId);

        ArgumentCaptor<RiskUserLockLogDO> captor = ArgumentCaptor.forClass(RiskUserLockLogDO.class);
        verify(riskUserLockLogMapper, times(2)).updateById(captor.capture());
        List<RiskUserLockLogDO> updated = captor.getAllValues();
        assertEquals(2, updated.size());
        assertEquals(adminUserId, updated.get(0).getUnlockerId());
        assertNotNull(updated.get(0).getUnlockTime());
        assertNotNull(updated.get(0).getLockEndTime());

        assertEquals(adminUserId, updated.get(1).getUnlockerId());
        assertNotNull(updated.get(1).getUnlockTime());
        assertNotNull(updated.get(1).getLockEndTime());

        verify(stringRedisTemplate).delete("risk:lock_status:" + targetUserId);
        verify(stringRedisTemplate).delete("risk:pwd_err:" + targetUserId);
    }

    @Test
    public void testUnlockUser_NullUserId() {
        riskLockService.unlockUser(1L, null);
        verifyNoInteractions(riskUserLockLogMapper);
        verifyNoInteractions(stringRedisTemplate);
    }
}
