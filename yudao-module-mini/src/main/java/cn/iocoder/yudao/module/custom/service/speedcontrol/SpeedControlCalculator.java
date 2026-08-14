package cn.iocoder.yudao.module.custom.service.speedcontrol;

import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 限速时长换算（纯函数，无状态、无依赖，便于单测）。
 *
 * 两种模式，时间范围优先：
 *  1. 范围模式：把接口的历史净耗时基线按对数压缩映射进 [minMs, maxMs]。
 *     取对数而非线性，是为了让常见的百毫秒~秒级接口在区间里拉得开；线性映射会让
 *     绝大多数接口挤在下限附近，快慢差别看不出来。
 *     映射函数单调递增 ⇒ 原来快的接口限速后一定还是快，不会出现反超。
 *  2. 速率模式：target = 基线 × (100 / rate)，rate=100 时不降速。
 */
public class SpeedControlCalculator {

    private static final long MIN_BASELINE_MS = 1L;
    private static final double MIN_RATE = 1.0d;
    private static final double MAX_RATE = 100.0d;

    private SpeedControlCalculator() {
    }

    /** 目标总耗时（未减去基线、未封顶），jitterRoll ∈ [0,1) 由调用方提供以便单测 */
    public static long computeTargetMs(long baselineMs, SpeedControlConfigDO cfg, double jitterRoll) {
        long base = Math.max(baselineMs, 0L);
        double target = Boolean.TRUE.equals(cfg.getRangeEnabled())
                ? mapIntoRange(base, cfg)
                : applyRate(base, cfg);
        return Math.max(0L, Math.round(applyJitter(target, cfg, jitterRoll)));
    }

    /**
     * 实际需要注入的等待时长 = 目标总耗时 - 已知基线，封顶到 maxDelayMs。
     * 业务逻辑本身还要耗掉约 baselineMs，所以最终总耗时 ≈ 目标值。
     */
    public static long computeDelayMs(long baselineMs, SpeedControlConfigDO cfg, double jitterRoll) {
        long target = computeTargetMs(baselineMs, cfg, jitterRoll);
        long delay = target - Math.max(baselineMs, 0L);
        if (delay <= 0L) {
            return 0L;
        }
        long cap = cfg.getMaxDelayMs() == null ? Long.MAX_VALUE : cfg.getMaxDelayMs();
        return Math.min(delay, Math.max(cap, 0L));
    }

    public static long computeDelayMs(long baselineMs, SpeedControlConfigDO cfg) {
        return computeDelayMs(baselineMs, cfg, ThreadLocalRandom.current().nextDouble());
    }

    /** 对数压缩映射到 [minMs, maxMs] */
    private static double mapIntoRange(long baselineMs, SpeedControlConfigDO cfg) {
        double lo = cfg.getMinMs() == null ? 0d : cfg.getMinMs();
        double hi = cfg.getMaxMs() == null ? 0d : cfg.getMaxMs();
        if (hi < lo) {
            double tmp = lo;
            lo = hi;
            hi = tmp;
        }
        double refFast = Math.max(cfg.getRefFastMs() == null ? 100L : cfg.getRefFastMs(), MIN_BASELINE_MS);
        double refSlow = cfg.getRefSlowMs() == null ? 10000L : cfg.getRefSlowMs();
        // 参考区间必须真的是个区间，否则 ln 差为 0 会除出 NaN/Inf
        if (refSlow <= refFast) {
            refSlow = refFast * 2d;
        }
        double base = Math.max(baselineMs, MIN_BASELINE_MS);
        double x = (Math.log(base) - Math.log(refFast)) / (Math.log(refSlow) - Math.log(refFast));
        return lo + (hi - lo) * clamp(x, 0d, 1d);
    }

    private static double applyRate(long baselineMs, SpeedControlConfigDO cfg) {
        double rate = clamp(toDouble(cfg.getRate(), MAX_RATE), MIN_RATE, MAX_RATE);
        return baselineMs * (MAX_RATE / rate);
    }

    private static double applyJitter(double target, SpeedControlConfigDO cfg, double jitterRoll) {
        double percent = toDouble(cfg.getJitterPercent(), 0d);
        if (percent <= 0d || target <= 0d) {
            return target;
        }
        double factor = 1d + (clamp(jitterRoll, 0d, 1d) * 2d - 1d) * percent / 100d;
        return target * Math.max(factor, 0d);
    }

    private static double toDouble(BigDecimal value, double defaultValue) {
        return value == null ? defaultValue : value.doubleValue();
    }

    private static double clamp(double v, double lo, double hi) {
        if (Double.isNaN(v)) {
            return lo;
        }
        return v < lo ? lo : (v > hi ? hi : v);
    }

}
