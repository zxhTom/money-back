package cn.iocoder.yudao.module.custom.service.stats;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.dal.mysql.stats.ContractDailyStatsMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ContractDailyStatsJobTest {

    private static final int WINDOW_DAYS = 30;

    @Mock
    private ContractDailyStatsMapper mapper;

    @InjectMocks
    private ContractDailyStatsJob job;

    private void enable(boolean enabled) {
        ReflectionTestUtils.setField(job, "enabled", enabled);
        ReflectionTestUtils.setField(job, "windowDays", WINDOW_DAYS);
    }

    @AfterEach
    void clearTenant() {
        TenantContextHolder.clear();
    }

    /**
     * 回归守卫：run() 必须在"忽略租户"上下文里执行快照。
     *
     * 之前的写法是给内部方法加 @TenantIgnore，但那个注解靠 @Around 代理生效，
     * 同类自调用会绕过代理 —— 定时任务线程没有租户上下文，第一次查询就会在
     * getRequiredTenantId() 抛 NPE，再被 run() 的 catch 吞掉，表现为"每晚静默失败"。
     * 所以这里断言的是执行期间的真实上下文状态，而不是注解写没写。
     */
    @Test
    public void testRunExecutesWithTenantIgnored() {
        enable(true);
        AtomicBoolean ignoredDuringCall = new AtomicBoolean(false);
        when(mapper.insertSnapshotOfRange(any(), any())).thenAnswer(inv -> {
            ignoredDuringCall.set(TenantContextHolder.isIgnore());
            return 3;
        });

        job.run();

        assertTrue(ignoredDuringCall.get(), "快照执行期间必须处于忽略租户状态，否则定时任务线程会抛 NPE");
    }

    /** 执行完要把忽略标记还原，不能污染复用该线程的后续逻辑 */
    @Test
    public void testTenantIgnoreRestoredAfterRun() {
        enable(true);
        when(mapper.insertSnapshotOfRange(any(), any())).thenReturn(1);

        job.run();

        assertFalse(TenantContextHolder.isIgnore(), "执行后必须还原忽略标记");
    }

    /** 窗口是 [今天-N, 今天)：绝不能把今天算进去，当天数据还在变 */
    @Test
    public void testWindowExcludesToday() {
        enable(true);
        AtomicReference<LocalDate> from = new AtomicReference<>();
        AtomicReference<LocalDate> to = new AtomicReference<>();
        when(mapper.insertSnapshotOfRange(any(), any())).thenAnswer(inv -> {
            from.set(inv.getArgument(0));
            to.set(inv.getArgument(1));
            return 0;
        });

        job.snapshotRecentDays();

        LocalDate today = LocalDate.now();
        assertEquals(today.minusDays(WINDOW_DAYS), from.get());
        assertEquals(today, to.get(), "上界必须是今天且为开区间，今天不入快照");
    }

    @Test
    public void testDisabledDoesNothing() {
        enable(false);
        job.run();
        verify(mapper, never()).insertSnapshotOfRange(any(), any());
    }

    /** 快照失败不能把定时任务线程打挂，也不能留下脏的忽略标记 */
    @Test
    public void testFailureIsContainedAndRestoresContext() {
        enable(true);
        when(mapper.insertSnapshotOfRange(any(), any())).thenThrow(new RuntimeException("db down"));

        job.run();

        assertFalse(TenantContextHolder.isIgnore());
    }

    /** windowDays 配成 0 或负数时兜底为至少 1 天，避免 from==to 查了个空区间 */
    @Test
    public void testWindowDaysFloorsAtOne() {
        ReflectionTestUtils.setField(job, "enabled", true);
        ReflectionTestUtils.setField(job, "windowDays", 0);
        AtomicReference<LocalDate> from = new AtomicReference<>();
        when(mapper.insertSnapshotOfRange(any(), any())).thenAnswer(inv -> {
            from.set(inv.getArgument(0));
            return 0;
        });

        job.snapshotRecentDays();

        assertEquals(LocalDate.now().minusDays(1), from.get());
    }

}
