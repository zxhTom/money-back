package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContractPageRateLimiterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private AlertRuleService alertRuleService;

    @InjectMocks
    private ContractPageRateLimiter limiter;

    @BeforeEach
    public void setUp() {
        // 只让 3 分钟窗口生效，其余窗口关掉，避免测试里要 mock 6 个 key
        ReflectionTestUtils.setField(limiter, "m3", 20);
        ReflectionTestUtils.setField(limiter, "m5", 0);
        ReflectionTestUtils.setField(limiter, "m10", 0);
        ReflectionTestUtils.setField(limiter, "m15", 0);
        ReflectionTestUtils.setField(limiter, "m30", 0);
        ReflectionTestUtils.setField(limiter, "m60", 0);
        limiter.init();
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private AlertRuleDO rule(boolean exposeReason) {
        AlertRuleDO r = new AlertRuleDO();
        r.setExposeReason(exposeReason ? 1 : 0);
        return r;
    }

    @Test
    public void testExposeReasonOff_genericMessage() {
        when(alertRuleService.getCachedRule("CONTRACT_QUERY_RATE_LIMIT")).thenReturn(rule(false));
        when(valueOperations.increment("contract:page:rl:9:180")).thenReturn(21L);

        ServiceException ex = assertThrows(ServiceException.class, () -> limiter.check(9L));
        assertEquals(10017, ex.getCode());
        assertEquals("请勿频繁访问", ex.getMessage());
    }

    @Test
    public void testExposeReasonOn_detailedMessage() {
        when(alertRuleService.getCachedRule("CONTRACT_QUERY_RATE_LIMIT")).thenReturn(rule(true));
        when(valueOperations.increment("contract:page:rl:9:180")).thenReturn(21L);

        ServiceException ex = assertThrows(ServiceException.class, () -> limiter.check(9L));
        assertEquals(10017, ex.getCode());
        assertEquals("查询过于频繁，180秒内已达21次，超过上限20次，请稍后再试", ex.getMessage());
    }

    @Test
    public void testRuleNull_fallsBackToGenericMessage() {
        when(alertRuleService.getCachedRule("CONTRACT_QUERY_RATE_LIMIT")).thenReturn(null);
        when(valueOperations.increment("contract:page:rl:9:180")).thenReturn(21L);

        ServiceException ex = assertThrows(ServiceException.class, () -> limiter.check(9L));
        assertEquals("请勿频繁访问", ex.getMessage());
    }
}
