package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "安全告警分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityAlertPageReqVO extends PageParam {

    @Schema(description = "告警类型")
    private String alertType;

    @Schema(description = "严重级别(1=INFO,2=WARNING,3=CRITICAL)")
    private Integer severity;

    @Schema(description = "来源IP")
    private String sourceIp;

    @Schema(description = "处理状态(0=未处理,1=已处理,2=误报)")
    private Integer handled;

    @Schema(description = "告警时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
