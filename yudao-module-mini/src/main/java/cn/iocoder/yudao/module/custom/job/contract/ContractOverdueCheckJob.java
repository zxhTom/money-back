package cn.iocoder.yudao.module.custom.job.contract;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 合同逾期检查 Job：定时检查合同到期时间，将已到期的合同更新为已逾期状态
 *
 * @author zxhtom
 */
@Component
@Slf4j
public class ContractOverdueCheckJob implements JobHandler {

    /**
     * 已逾期状态值
     * TODO: 根据实际业务需求调整此值，确保与数据库字典中的"已逾期"状态值一致
     */
    private static final Integer OVERDUE_STATUS = 4;
    private static final Integer RUNNING_STATUS = 2;

    @Resource
    private ContractMapper contractMapper;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        log.info("[execute][开始检查合同逾期状态]");

        // 1. 查询所有 end_date < 当前时间 且状态不是已逾期的合同
        LocalDateTime now = LocalDateTime.now();
        List<ContractDO> overdueContracts = contractMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<ContractDO>()
                        .lt(ContractDO::getEndDate, now)  // end_date < 当前时间
                        .ne(ContractDO::getStatus, OVERDUE_STATUS)  // 状态不是已逾期
                        .eq(ContractDO::getStatus, RUNNING_STATUS)  // 状态是待还款
                        .isNotNull(ContractDO::getEndDate)  // end_date 不为空
        );

        if (CollUtil.isEmpty(overdueContracts)) {
            log.info("[execute][未发现逾期合同]");
            return "未发现逾期合同";
        }

        // 2. 批量更新为已逾期状态
        int updateCount = 0;
        for (ContractDO contract : overdueContracts) {
            try {
                ContractDO updateObj = new ContractDO();
                updateObj.setId(contract.getId());
                updateObj.setStatus(OVERDUE_STATUS);
                contractMapper.updateById(updateObj);
                updateCount++;
                log.info("[execute][更新合同为已逾期状态，合同ID: {}]", contract.getId());
            } catch (Exception e) {
                log.error("[execute][更新合同逾期状态失败，合同ID: {}]", contract.getId(), e);
            }
        }

        String result = String.format("检查完成，发现 %d 个逾期合同，成功更新 %d 个", overdueContracts.size(), updateCount);
        log.info("[execute][{}]", result);
        return result;
    }

}
