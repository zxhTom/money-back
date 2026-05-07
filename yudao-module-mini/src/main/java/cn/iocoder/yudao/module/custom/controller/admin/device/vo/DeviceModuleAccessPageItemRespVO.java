package cn.iocoder.yudao.module.custom.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 设备模块开放分页项 Response VO")
@Data
public class DeviceModuleAccessPageItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模块名", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.create")
    private String moduleName;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "device-001")
    private String deviceId;

    @Schema(description = "是否开放", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean enabled;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

