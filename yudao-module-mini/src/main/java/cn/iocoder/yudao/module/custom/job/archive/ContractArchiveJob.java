package cn.iocoder.yudao.module.custom.job.archive;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ArchiveContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.framework.mybatis.core.archive.DataArchiveService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 合同历史数据归档 Job
 * <p>
 * 将 12 个月前已完结（status=3）或已取消（status=4）的合同迁移到 archive 库。
 * 具体的 status 值以 contract_status 字典数据为准。
 */
@Component
@Slf4j
public class ContractArchiveJob implements JobHandler {

    private static final int ARCHIVE_MONTHS = 12;
    private static final int BATCH_SIZE = 200;
    // 已完结 / 已取消的 status 值，根据系统字典 contract_status 实际值调整
    private static final int STATUS_COMPLETED = 3;
    private static final int STATUS_CANCELLED = 4;

    @Resource
    private ContractMapper contractMapper;
    @Resource
    private ArchiveContractMapper archiveContractMapper;
    @Resource
    private DataArchiveService dataArchiveService;

    @Override
    @TenantIgnore
    public String execute(String param) {
        LocalDateTime threshold = DataArchiveService.archiveThreshold(ARCHIVE_MONTHS);
        LambdaQueryWrapper<ContractDO> condition = new LambdaQueryWrapper<ContractDO>()
                .in(ContractDO::getStatus, STATUS_COMPLETED, STATUS_CANCELLED)
                .lt(ContractDO::getCreateTime, threshold);

        int total = 0;
        int batch;
        do {
            batch = dataArchiveService.archiveBatch(contractMapper, archiveContractMapper, condition, BATCH_SIZE);
            total += batch;
        } while (batch >= BATCH_SIZE);

        log.info("[execute][合同归档完成，共归档 {} 条]", total);
        return String.format("合同归档完成，共归档 %s 条", total);
    }

}
