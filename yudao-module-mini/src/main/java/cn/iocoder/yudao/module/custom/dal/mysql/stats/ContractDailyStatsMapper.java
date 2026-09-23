package cn.iocoder.yudao.module.custom.dal.mysql.stats;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.stats.ContractDailyStatsDO;
import cn.iocoder.yudao.module.custom.dal.mysql.dashboard.DashboardMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ContractDailyStatsMapper extends BaseMapperX<ContractDailyStatsDO> {

    /**
     * 把 [fromInclusive, toExclusive) 区间内的合同按 (租户, 日期, 状态) 聚合写入快照。
     *
     * 一条语句覆盖整个窗口，而不是按天循环：既避免了"某天没有合同就没有快照行、
     * 导致水位线永远推不动"的问题，也让区间条件保持可走索引。
     * INSERT IGNORE + 唯一键 ⇒ 已有快照绝不会被覆盖，这是"归档后不再变化"的机械保证；
     * 因此每晚重算整个窗口是安全的，还能自动补齐停机期间漏掉的天。
     * 不加 deleted 过滤：按需求，合同被删除了也仍然要算进当天金额。
     */
    int insertSnapshotOfRange(@Param("fromInclusive") LocalDate fromInclusive,
                              @Param("toExclusive") LocalDate toExclusive);

    /** 读快照：按天汇总，排除不计入的状态 */
    List<DashboardMapper.ContractTrendPoint> selectTrendFromSnapshot(@Param("tenantId") Long tenantId,
                                                                    @Param("from") LocalDate from,
                                                                    @Param("to") LocalDate to,
                                                                    @Param("excludeStatuses") Collection<Integer> excludeStatuses);

    /**
     * 实时兜底：用于今天、以及任何还没落快照的日期（比如 00:00-00:30 之间的昨天，
     * 或快照任务失败的那几天）。口径与快照保持一致：不过滤 deleted、排除同样的状态。
     */
    List<DashboardMapper.ContractTrendPoint> selectLiveTrend(@Param("tenantId") Long tenantId,
                                                             @Param("fromInclusive") LocalDate fromInclusive,
                                                             @Param("toExclusive") LocalDate toExclusive,
                                                             @Param("excludeStatuses") Collection<Integer> excludeStatuses);

}
