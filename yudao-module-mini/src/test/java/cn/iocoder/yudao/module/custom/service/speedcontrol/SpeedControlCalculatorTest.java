package cn.iocoder.yudao.module.custom.service.speedcontrol;

import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpeedControlCalculatorTest {

    private static final long MIN_3_MIN = 3 * 60 * 1000L;
    private static final long MAX_6_MIN = 6 * 60 * 1000L;
    /** 抖动关掉时 jitterRoll 取多少都一样，取 0.5 表示"正中不偏" */
    private static final double NO_JITTER_ROLL = 0.5d;

    private SpeedControlConfigDO rangeConfig() {
        SpeedControlConfigDO cfg = new SpeedControlConfigDO();
        cfg.setEnabled(true);
        cfg.setRangeEnabled(true);
        cfg.setMinMs(MIN_3_MIN);
        cfg.setMaxMs(MAX_6_MIN);
        cfg.setRefFastMs(100L);
        cfg.setRefSlowMs(10000L);
        cfg.setMaxDelayMs(600000L);
        cfg.setJitterPercent(BigDecimal.ZERO);
        return cfg;
    }

    private SpeedControlConfigDO rateConfig(String rate) {
        SpeedControlConfigDO cfg = new SpeedControlConfigDO();
        cfg.setEnabled(true);
        cfg.setRangeEnabled(false);
        cfg.setRate(new BigDecimal(rate));
        cfg.setMaxDelayMs(600000L);
        cfg.setJitterPercent(BigDecimal.ZERO);
        return cfg;
    }

    @Test
    public void testRangeMode_mapsDocumentedAnchors() {
        SpeedControlConfigDO cfg = rangeConfig();
        // 参考区间下界 → 区间下限
        assertEquals(MIN_3_MIN, SpeedControlCalculator.computeTargetMs(100L, cfg, NO_JITTER_ROLL));
        // 1s 落在区间正中（log 尺度上 100ms..10s 的中点）
        assertEquals((MIN_3_MIN + MAX_6_MIN) / 2, SpeedControlCalculator.computeTargetMs(1000L, cfg, NO_JITTER_ROLL));
        // 参考区间上界 → 区间上限
        assertEquals(MAX_6_MIN, SpeedControlCalculator.computeTargetMs(10000L, cfg, NO_JITTER_ROLL));
    }

    @Test
    public void testRangeMode_clampsOutsideReferenceBounds() {
        SpeedControlConfigDO cfg = rangeConfig();
        assertEquals(MIN_3_MIN, SpeedControlCalculator.computeTargetMs(0L, cfg, NO_JITTER_ROLL));
        assertEquals(MIN_3_MIN, SpeedControlCalculator.computeTargetMs(50L, cfg, NO_JITTER_ROLL));
        assertEquals(MAX_6_MIN, SpeedControlCalculator.computeTargetMs(60000L, cfg, NO_JITTER_ROLL));
    }

    /** 核心保证：原来快的接口限速后必须还是快，绝不能反超 */
    @Test
    public void testRangeMode_isMonotonic() {
        SpeedControlConfigDO cfg = rangeConfig();
        Random random = new Random(42);
        long previousBaseline = 0L;
        long previousTarget = -1L;
        for (int i = 0; i < 200; i++) {
            long baseline = previousBaseline + 1 + random.nextInt(200);
            long target = SpeedControlCalculator.computeTargetMs(baseline, cfg, NO_JITTER_ROLL);
            assertTrue(target >= previousTarget,
                    "基线 " + baseline + " 的目标时长 " + target + " 小于更快接口的 " + previousTarget);
            previousBaseline = baseline;
            previousTarget = target;
        }
    }

    @Test
    public void testRangeMode_resultAlwaysWithinRange() {
        SpeedControlConfigDO cfg = rangeConfig();
        long[] baselines = {0L, 1L, 100L, 999L, 1000L, 5000L, 10000L, 100000L};
        for (long baseline : baselines) {
            long target = SpeedControlCalculator.computeTargetMs(baseline, cfg, NO_JITTER_ROLL);
            assertTrue(target >= MIN_3_MIN && target <= MAX_6_MIN, "越界: baseline=" + baseline + " target=" + target);
        }
    }

    @Test
    public void testRateMode_hundredMeansNoSlowdown() {
        assertEquals(0L, SpeedControlCalculator.computeDelayMs(1000L, rateConfig("100"), NO_JITTER_ROLL));
    }

    @Test
    public void testRateMode_multipliesDuration() {
        assertEquals(2000L, SpeedControlCalculator.computeTargetMs(1000L, rateConfig("50"), NO_JITTER_ROLL));
        assertEquals(1000L, SpeedControlCalculator.computeDelayMs(1000L, rateConfig("50"), NO_JITTER_ROLL));
        // rate=1 → 100 倍，但注入延迟被 maxDelayMs 封顶
        assertEquals(600000L, SpeedControlCalculator.computeDelayMs(10000L, rateConfig("1"), NO_JITTER_ROLL));
    }

    @Test
    public void testRateMode_supportsFloat() {
        assertEquals(4000L, SpeedControlCalculator.computeTargetMs(1000L, rateConfig("25.0"), NO_JITTER_ROLL));
        assertEquals(2500L, SpeedControlCalculator.computeTargetMs(1000L, rateConfig("40.0"), NO_JITTER_ROLL));
    }

    @Test
    public void testDelayIsTargetMinusBaseline() {
        SpeedControlConfigDO cfg = rangeConfig();
        long baseline = 1000L;
        long target = SpeedControlCalculator.computeTargetMs(baseline, cfg, NO_JITTER_ROLL);
        assertEquals(target - baseline, SpeedControlCalculator.computeDelayMs(baseline, cfg, NO_JITTER_ROLL));
    }

    @Test
    public void testDelayNeverNegative() {
        SpeedControlConfigDO cfg = rangeConfig();
        // 基线已经比区间上限还慢时，不该出现负延迟
        assertEquals(0L, SpeedControlCalculator.computeDelayMs(10 * 60 * 1000L, cfg, NO_JITTER_ROLL));
    }

    @Test
    public void testJitterStaysWithinConfiguredPercent() {
        SpeedControlConfigDO cfg = rangeConfig();
        cfg.setJitterPercent(new BigDecimal("2"));
        long plain = SpeedControlCalculator.computeTargetMs(1000L, rangeConfig(), NO_JITTER_ROLL);
        long low = SpeedControlCalculator.computeTargetMs(1000L, cfg, 0d);
        long high = SpeedControlCalculator.computeTargetMs(1000L, cfg, 1d);
        assertEquals(Math.round(plain * 0.98d), low);
        assertEquals(Math.round(plain * 1.02d), high);
    }

    /** 误配置（上下限写反、参考区间倒置、字段为 null）不能算出 NaN 或负数 */
    @Test
    public void testMalformedConfigDegradesSafely() {
        SpeedControlConfigDO reversed = rangeConfig();
        reversed.setMinMs(MAX_6_MIN);
        reversed.setMaxMs(MIN_3_MIN);
        long target = SpeedControlCalculator.computeTargetMs(1000L, reversed, NO_JITTER_ROLL);
        assertTrue(target >= MIN_3_MIN && target <= MAX_6_MIN);

        SpeedControlConfigDO badRef = rangeConfig();
        badRef.setRefFastMs(10000L);
        badRef.setRefSlowMs(100L);
        assertTrue(SpeedControlCalculator.computeTargetMs(1000L, badRef, NO_JITTER_ROLL) >= 0L);

        SpeedControlConfigDO empty = new SpeedControlConfigDO();
        assertTrue(SpeedControlCalculator.computeDelayMs(1000L, empty, NO_JITTER_ROLL) >= 0L);
    }

}
