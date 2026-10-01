package cn.iocoder.yudao.module.system.job.risk;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskAbnormalReportDO;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskAbnormalReportMapper;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskUserLockLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 风险异常用户报表生成 Job
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class RiskAbnormalReportJob implements JobHandler {

    @Resource
    private RiskUserLockLogMapper riskUserLockLogMapper;

    @Resource
    private RiskAbnormalReportMapper riskAbnormalReportMapper;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        // 1. 查询所有 escalation_level >= 3 的锁定记录
        List<RiskUserLockLogDO> highRiskLogs = riskUserLockLogMapper.selectList(
                new LambdaQueryWrapperX<RiskUserLockLogDO>()
                        .ge(RiskUserLockLogDO::getEscalationLevel, 3)
        );

        if (CollUtil.isEmpty(highRiskLogs)) {
            return "生成风控异常报告 0 条";
        }

        // 2. 提取去重后的 userId
        Set<Long> userIds = highRiskLogs.stream()
                .map(RiskUserLockLogDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int createdCount = 0;
        // 3. 对每一个 userId 检查是否已存在 status = 0 (待处理) 的报告（避免重复生成）
        for (Long userId : userIds) {
            RiskAbnormalReportDO existReport = riskAbnormalReportMapper.selectByUserIdAndStatus(userId, 0);
            if (existReport == null) {
                // 4. 不存在则生成新的待处理报告
                RiskAbnormalReportDO report = RiskAbnormalReportDO.builder()
                        .userId(userId)
                        .abnormalType("FREQUENT_LOCK")
                        .description("用户频繁被风控系统锁定（达到阶梯 3 及以上），存在刷单或爆破风险，请人工核查。")
                        .status(0)
                        .build();
                riskAbnormalReportMapper.insert(report);
                createdCount++;
            }
        }

        log.info("[execute][生成风控异常报告数量: {}]", createdCount);
        return String.format("生成风控异常报告 %s 条", createdCount);
    }

}
