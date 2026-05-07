package cn.iocoder.yudao.module.custom.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 设备模块开放查询 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceModuleAccessCheckRespVO {

    @Schema(description = "模块名", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.create")
    private String moduleName;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "device-001")
    private String deviceId;

    @Schema(description = "是否开放", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean enabled;
}

