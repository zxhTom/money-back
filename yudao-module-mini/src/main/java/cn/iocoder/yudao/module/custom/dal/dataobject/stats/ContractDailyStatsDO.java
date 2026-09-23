package cn.iocoder.yudao.module.custom.dal.dataobject.stats;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同每日金额快照。写入后不再变更，是大盘走势的唯一数据源（缺快照的日期回落到实时表）。
 *
 * @TenantIgnore 打在 DO 上等价于把这张表加进 tenant.ignore-tables：
 * 光"不继承 TenantBaseDO"是不够的——TenantDatabaseInterceptor.computeIgnoreTable 对
 * 已注册的 MyBatis-Plus 实体，只有带 @TenantIgnore 才返回"忽略"，否则照样追加 tenant_id 条件
 * 并要求线程里有租户上下文（定时任务线程没有，会直接抛 NPE）。
 * 租户维度由本表自己的 tenant_id 列显式承载，不走框架自动过滤。
 */
@TableName("custom_contract_daily_stats")
@TenantIgnore
@Data
public class ContractDailyStatsDO {

    private Long id;
    private Long tenantId;
    private LocalDate statDate;
    private Integer status;
    private Long contractCount;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;

}
