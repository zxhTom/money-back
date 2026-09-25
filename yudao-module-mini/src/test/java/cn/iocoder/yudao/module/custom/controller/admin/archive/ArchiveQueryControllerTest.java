package cn.iocoder.yudao.module.custom.controller.admin.archive;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.framework.archive.LogArchiveJob;
import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArchiveQueryControllerTest {

    @Mock
    private ClickHouseArchiveService ch;

    @Mock
    private LogArchiveJob logArchiveJob;

    @Mock
    private TenantProperties tenantProperties;

    @InjectMocks
    private ArchiveQueryController controller;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.clear();
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void test1_DefaultTenant_Tables_Success() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);
        TenantContextHolder.setTenantId(1L);

        CommonResult<List<String>> result = controller.tables();
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    public void test2_OtherTenant_Tables_ThrowsException() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);
        TenantContextHolder.setTenantId(162L);

        ServiceException exception = assertThrows(ServiceException.class, () -> controller.tables());
        assertTrue(exception.getMessage().contains("归档数据仅总平台可查询"));
    }

    @Test
    public void test3_OtherTenant_Query_ThrowsExceptionAndNoClickHouseCall() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);
        TenantContextHolder.setTenantId(162L);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                controller.query("arc_api_access_log", null, null, 1, 20));
        assertTrue(exception.getMessage().contains("归档数据仅总平台可查询"));
        verifyNoInteractions(ch);
    }
}
