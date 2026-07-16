package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;

class ContractRecycleJobTest {
    @Test
    void run_skips_whenClickHouseDisabled() {
        ClickHouseArchiveService ch = mock(ClickHouseArchiveService.class);
        when(ch.isEnabled()).thenReturn(false);
        ContractRecycleJob job = new ContractRecycleJob();
        ReflectionTestUtils.setField(job, "ch", ch);
        ReflectionTestUtils.setField(job, "enabled", true);
        job.run();
        verify(ch, never()).insertRows(anyString(), anyList(), anyList());
    }
}
