package cn.iocoder.yudao.module.system.dal.dataobject.risk;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户锁定记录 DO
 *
 * @author 芋道源码
 */
@TableName("system_risk_user_lock_log")
@KeySequence("system_risk_user_lock_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskUserLockLogDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 锁定类型：RATE_LIMIT, PASSWORD_ERR, MANUAL, RISK_CONTROL
     */
    private String lockType;

    /**
     * 锁定开始时间
     */
    private LocalDateTime lockStartTime;

    /**
     * 锁定结束时间（为空代表永久禁用）
     */
    private LocalDateTime lockEndTime;

    /**
     * 阶梯层级，记录这是第几次惩罚
     */
    private Integer escalationLevel;

    /**
     * 手动解锁时间（为空代表未手动解锁）
     */
    private LocalDateTime unlockTime;

    /**
     * 解锁人ID
     */
    private Long unlockerId;

}
