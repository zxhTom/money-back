package cn.iocoder.yudao.module.fee.dal.dataobject.strategy;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 费用策略 DO
 *
 * @author 芋道源码
 */
@TableName("fee_strategy")
@KeySequence("fee_strategy_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 最小金额
     */
    private BigDecimal minAmount;
    /**
     * 最大金额
     */
    private BigDecimal maxAmount;
    /**
     * 收费金额
     */
    private BigDecimal fee;
    /**
     * 策略顺序
     */
    private Integer strategyOrder;
    /**
     * 状态：1启用，0禁用
     *
     * 枚举 {@link TODO 开关 对应的类}
     */
    private Integer status;


}