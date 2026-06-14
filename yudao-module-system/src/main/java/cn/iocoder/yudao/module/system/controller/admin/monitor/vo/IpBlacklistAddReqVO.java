package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IP黑名单添加 Request VO")
@Data
public class IpBlacklistAddReqVO {

    @Schema(description = "IP地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String ip;

    @Schema(description = "封禁原因")
    private String reason;

    @Schema(description = "过期时间(不填=永久封禁)")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime expireTime;
}
