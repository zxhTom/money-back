package cn.iocoder.yudao.module.custom.service.dashboard;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.dal.mysql.dashboard.DashboardMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.stats.ContractDailyStatsMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 大盘的租户取值行为。
 *
 * 背景：曾经把这里改成无条件 getRequiredTenantId()（fail closed），
 * 但 YudaoTenantAutoConfiguration 挂着 @ConditionalOnProperty(yudao.tenant.enable)，
 * 关闭多租户后连 TenantContextWebFilter 都不注册，线程里没有租户上下文，
 * 于是大盘直接 500。这组用例就是钉住这个回归。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DashboardTenantScopeTest {

    @Mock
    private DashboardMapper dashboardMapper;
    @Mock
    private ContractDailyStatsMapper contractDailyStatsMapper;

    @InjectMocks
    private DashboardServiceImpl service;

    private final AtomicReference<Long> liveTenantId = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "trendExcludeStatusesRaw", "7");
        when(dashboardMapper.selectContractSummary()).thenReturn(new DashboardMapper.ContractSummary());
        when(dashboardMapper.selectContractStatusDist()).thenReturn(Collections.emptyList());
        when(contractDailyStatsMapper.selectLiveTrend(any(), any(), any(), any())).thenAnswer(inv -> {
            liveTenantId.set(inv.getArgument(0));
            return Collections.emptyList();
        });
        when(contractDailyStatsMapper.selectTrendFromSnapshot(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
    }

    @AfterEach
    void clear() {
        TenantContextHolder.clear();
    }

    private void tenantEnabled(boolean enabled) {
        ReflectionTestUtils.setField(service, "tenantEnabled", enabled);
    }

    /** 关闭多租户时没有租户上下文是正常状态，不能抛异常，也不能带租户条件 */
    @Test
    public void testTenantDisabledDoesNotThrowAndSkipsFilter() {
        tenantEnabled(false);

        assertDoesNotThrow(() -> service.getContractStats());
        assertNull(liveTenantId.get(), "单租户模式不该带租户条件，否则 tenant_id = null 会查空");
    }

    /** 开启多租户但拿不到租户号，属于异常状态，必须抛错而不是静默汇总所有租户 */
    @Test
    public void testTenantEnabledWithoutContextFailsClosed() {
        tenantEnabled(true);
        assertThrows(NullPointerException.class, () -> service.getContractStats());
    }

    /** 开启多租户且上下文正常时，租户号要真的传到查询里 */
    @Test
    public void testTenantEnabledPassesTenantId() {
        tenantEnabled(true);
        TenantContextHolder.setTenantId(2L);

        service.getContractStats();

        assertEquals(2L, liveTenantId.get());
    }

    /** 走势要覆盖完整 30 天，缺快照的日期由实时值补上（补不到才是 0） */
    @Test
    public void testTrendAlwaysHas30Points() {
        tenantEnabled(false);

        assertEquals(30, service.getContractStats().getTrendDates().size());
        assertEquals(30, service.getContractStats().getTrendAmounts().size());
    }

    /** 快照优先于实时：同一天两边都有值时，图上必须取快照那份 */
    @Test
    public void testSnapshotWinsOverLiveForSameDay() {
        tenantEnabled(false);
        String yesterday = LocalDate.now().minusDays(1).toString();

        DashboardMapper.ContractTrendPoint live = new DashboardMapper.ContractTrendPoint();
        live.setDate(yesterday);
        live.setCount(1L);
        live.setAmount(new java.math.BigDecimal("100"));
        DashboardMapper.ContractTrendPoint snap = new DashboardMapper.ContractTrendPoint();
        snap.setDate(yesterday);
        snap.setCount(9L);
        snap.setAmount(new java.math.BigDecimal("900"));

        when(contractDailyStatsMapper.selectLiveTrend(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(live));
        when(contractDailyStatsMapper.selectTrendFromSnapshot(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(snap));

        int idx = service.getContractStats().getTrendDates().indexOf(yesterday);
        assertEquals(9L, service.getContractStats().getTrendCounts().get(idx),
                "已归档的日期必须以快照为准，不能被实时值盖掉");
    }

}
