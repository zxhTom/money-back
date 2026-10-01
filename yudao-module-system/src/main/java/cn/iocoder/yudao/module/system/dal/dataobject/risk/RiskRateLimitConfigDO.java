package cn.iocoder.yudao.module.system.dal.dataobject.risk;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * API 访问频率规则配置 DO
 *
 * @author 芋道源码
 */
@TableName("system_risk_rate_limit_config")
@KeySequence("system_risk_rate_limit_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRateLimitConfigDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 角色ID（为空代表全局规则）
     */
    private Long roleId;

    /**
     * 接口匹配模式，支持 Ant 风格或具体 URI
     */
    private String apiPattern;

    /**
     * 时间窗口（秒）
     */
    private Integer timeWindow;

    /**
     * 窗口内最大请求次数
     */
    private Integer maxCount;

    /**
     * 阶梯锁定时间（秒），如 "300,3600,86400,-1"（-1 表示永久锁定）
     */
    private String lockDurationLadder;

    /**
     * 状态：0-启用，1-禁用
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
