package cn.iocoder.yudao.module.pay.job.order;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.mybatis.core.archive.DataArchiveService;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.ArchivePayOrderMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 支付订单历史数据归档 Job
 * <p>
 * 将 12 个月前已支付（SUCCESS）、已退款（REFUND）、已关闭（CLOSED）的订单迁移到 archive 库
 */
@Component
@Slf4j
public class PayOrderArchiveJob implements JobHandler {

    private static final int ARCHIVE_MONTHS = 12;
    private static final int BATCH_SIZE = 200;

    @Resource
    private PayOrderMapper payOrderMapper;
    @Resource
    private ArchivePayOrderMapper archivePayOrderMapper;
    @Resource
    private DataArchiveService dataArchiveService;

    @Override
    @TenantIgnore
    public String execute(String param) {
        LocalDateTime threshold = DataArchiveService.archiveThreshold(ARCHIVE_MONTHS);
        LambdaQueryWrapper<PayOrderDO> condition = new LambdaQueryWrapper<PayOrderDO>()
                .in(PayOrderDO::getStatus,
                        PayOrderStatusEnum.SUCCESS.getStatus(),
                        PayOrderStatusEnum.REFUND.getStatus(),
                        PayOrderStatusEnum.CLOSED.getStatus())
                .lt(PayOrderDO::getCreateTime, threshold);

        int total = 0;
        int batch;
        do {
            batch = dataArchiveService.archiveBatch(payOrderMapper, archivePayOrderMapper, condition, BATCH_SIZE);
            total += batch;
        } while (batch >= BATCH_SIZE);

        log.info("[execute][支付订单归档完成，共归档 {} 条]", total);
        return String.format("支付订单归档完成，共归档 %s 条", total);
    }

}
