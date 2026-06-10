package cn.iocoder.yudao.module.trade.job.order;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class AfterSaleLogCleanJob implements JobHandler {

    private static final Integer JOB_CLEAN_RETAIN_DAY = 90;
    private static final Integer DELETE_LIMIT = 100;

    @Resource
    private AfterSaleLogService afterSaleLogService;

    @Override
    @TenantIgnore
    public String execute(String param) {
        Integer count = afterSaleLogService.cleanAfterSaleLog(JOB_CLEAN_RETAIN_DAY, DELETE_LIMIT);
        log.info("[execute][定时执行清理售后日志数量 ({}) 个]", count);
        return String.format("定时执行清理售后日志数量 %s 个", count);
    }

}
