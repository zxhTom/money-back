package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.AlertRuleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.AlertRuleNotifyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AlertRuleServiceImplTest {

    @Mock
    private AlertRuleMapper alertRuleMapper;
    @Mock
    private AlertRuleNotifyMapper alertRuleNotifyMapper;

    @InjectMocks
    private AlertRuleServiceImpl service;

    @Test
    public void testSetExposeReason_true_updatesFieldToOne() {
        service.setExposeReason(5L, true);

        ArgumentCaptor<AlertRuleDO> captor = ArgumentCaptor.forClass(AlertRuleDO.class);
        verify(alertRuleMapper).updateById(captor.capture());
        assertEquals(5L, captor.getValue().getId());
        assertEquals(Integer.valueOf(1), captor.getValue().getExposeReason());
    }

    @Test
    public void testSetExposeReason_false_updatesFieldToZero() {
        service.setExposeReason(5L, false);

        ArgumentCaptor<AlertRuleDO> captor = ArgumentCaptor.forClass(AlertRuleDO.class);
        verify(alertRuleMapper).updateById(captor.capture());
        assertEquals(Integer.valueOf(0), captor.getValue().getExposeReason());
    }
}
