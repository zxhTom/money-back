package cn.iocoder.yudao.module.custom.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 设备模块开放配置 Request VO")
@Data
public class DeviceModuleAccessSetReqVO {

    @Schema(description = "模块名", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.create")
    @NotBlank(message = "模块名不能为空")
    private String moduleName;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "device-001")
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @Schema(description = "是否开放", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否开放不能为空")
    private Boolean enabled;
}

