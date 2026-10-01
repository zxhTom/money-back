package cn.iocoder.yudao.module.system.dal.dataobject.risk;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 异常用户风控报告 DO
 *
 * @author 芋道源码
 */
@TableName("system_risk_abnormal_report")
@KeySequence("system_risk_abnormal_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAbnormalReportDO extends TenantBaseDO {

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
     * 异常类型
     */
    private String abnormalType;

    /**
     * 描述
     */
    private String description;

    /**
     * 处理状态：0-待处理，1-已处理
     */
    private Integer status;

}
