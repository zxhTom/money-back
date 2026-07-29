package cn.iocoder.yudao.module.custom.service.invite;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegisterRiskControlGuardTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private AlertRuleService alertRuleService;
    @Mock
    private SecurityAlertService securityAlertService;
    @Mock
    private IpBlacklistService ipBlacklistService;

    @InjectMocks
    private RegisterRiskControlGuard guard;

    private AlertRuleDO rule(boolean enabled, int threshold, long window, boolean exposeReason) {
        AlertRuleDO r = new AlertRuleDO();
        r.setEnabled(enabled ? 1 : 0);
        r.setThreshold(threshold);
        r.setWindowSeconds((int) window);
        r.setAutoBan(0);
        r.setSeverity(2);
        r.setExposeReason(exposeReason ? 1 : 0);
        return r;
    }

    private void givenOverThreshold(long count) {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("security:register:1.2.3.4")).thenReturn(count);
    }

    @Test
    public void testExposeReasonOff_genericMessage() {
        when(alertRuleService.getCachedRule("REGISTER_ABUSE")).thenReturn(rule(true, 20, 3600, false));
        givenOverThreshold(21);

        ServiceException ex = assertThrows(ServiceException.class, () -> guard.beforeRegister("1.2.3.4"));
        assertEquals(10012, ex.getCode());
        assertEquals("注册过于频繁，请稍后再试", ex.getMessage());
    }

    @Test
    public void testExposeReasonOn_detailedMessage() {
        when(alertRuleService.getCachedRule("REGISTER_ABUSE")).thenReturn(rule(true, 20, 3600, true));
        givenOverThreshold(21);

        ServiceException ex = assertThrows(ServiceException.class, () -> guard.beforeRegister("1.2.3.4"));
        assertEquals(10012, ex.getCode());
        assertEquals("注册过于频繁，3600秒内已达21次，超过上限20次，请稍后再试", ex.getMessage());
    }
}
