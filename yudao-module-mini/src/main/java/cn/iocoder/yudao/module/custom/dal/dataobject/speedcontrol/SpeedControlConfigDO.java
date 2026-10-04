package cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@TableName("custom_speed_control_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class SpeedControlConfigDO extends BaseDO {

    private Long id;
    private Boolean enabled;
    private BigDecimal rate;
    private Boolean rangeEnabled;
    private Long minMs;
    private Long maxMs;
    private Long refFastMs;
    private Long refSlowMs;
    private Long maxDelayMs;
    private BigDecimal jitterPercent;
    private String exemptUserIds;
    private String exemptRoleIds;

}
