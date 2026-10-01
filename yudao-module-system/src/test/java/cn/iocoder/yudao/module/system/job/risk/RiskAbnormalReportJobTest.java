package cn.iocoder.yudao.module.system.job.risk;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskAbnormalReportDO;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskAbnormalReportMapper;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskUserLockLogMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RiskAbnormalReportJobTest extends BaseMockitoUnitTest {

    @InjectMocks
    private RiskAbnormalReportJob riskAbnormalReportJob;

    @Mock
    private RiskUserLockLogMapper riskUserLockLogMapper;

    @Mock
    private RiskAbnormalReportMapper riskAbnormalReportMapper;

    @Test
    public void testExecute_HasAbnormalUsers_NewReport() throws Exception {
        RiskUserLockLogDO log1 = RiskUserLockLogDO.builder()
                .id(1L)
                .userId(100L)
                .escalationLevel(3)
                .build();
        RiskUserLockLogDO log2 = RiskUserLockLogDO.builder()
                .id(2L)
                .userId(200L)
                .escalationLevel(4)
                .build();

        when(riskUserLockLogMapper.selectList(any())).thenReturn(Arrays.asList(log1, log2));

        // 100 没有报告，200 已经有报告
        when(riskAbnormalReportMapper.selectByUserIdAndStatus(100L, 0)).thenReturn(null);
        RiskAbnormalReportDO existingReport = RiskAbnormalReportDO.builder()
                .id(99L)
                .userId(200L)
                .status(0)
                .build();
        when(riskAbnormalReportMapper.selectByUserIdAndStatus(200L, 0)).thenReturn(existingReport);

        String result = riskAbnormalReportJob.execute("");

        assertEquals("生成风控异常报告 1 条", result);

        ArgumentCaptor<RiskAbnormalReportDO> captor = ArgumentCaptor.forClass(RiskAbnormalReportDO.class);
        verify(riskAbnormalReportMapper, times(1)).insert(captor.capture());
        RiskAbnormalReportDO created = captor.getValue();
        assertEquals(100L, created.getUserId());
        assertEquals("FREQUENT_LOCK", created.getAbnormalType());
        assertEquals(0, created.getStatus());
        assertEquals("用户频繁被风控系统锁定（达到阶梯 3 及以上），存在刷单或爆破风险，请人工核查。", created.getDescription());
    }

    @Test
    public void testExecute_EmptyLogs() throws Exception {
        when(riskUserLockLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        String result = riskAbnormalReportJob.execute("");

        assertEquals("生成风控异常报告 0 条", result);
        verify(riskAbnormalReportMapper, never()).insert(any(RiskAbnormalReportDO.class));
    }
}
