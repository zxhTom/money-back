package cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 访问速度控制配置 Response VO")
@Data
public class SpeedControlConfigRespVO {

    private Boolean enabled;
    private BigDecimal rate;
    private Boolean rangeEnabled;
    private Long minMs;
    private Long maxMs;
    private Long refFastMs;
    private Long refSlowMs;
    private Long maxDelayMs;
    private BigDecimal jitterPercent;
    private List<Long> exemptUserIds;
    private List<Long> exemptRoleIds;

}
