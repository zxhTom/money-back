package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "IP黑名单添加 Request VO")
@Data
public class IpBlacklistAddReqVO {

    @Schema(description = "IP地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String ip;

    @Schema(description = "封禁原因")
    private String reason;

    @Schema(description = "过期时间，格式 yyyy-MM-dd HH:mm:ss，不填=永久封禁")
    private String expireTime;
}
