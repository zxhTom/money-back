package cn.iocoder.yudao.module.custom.controller.admin.device.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 设备模块开放分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeviceModuleAccessPageReqVO extends PageParam {

    @Schema(description = "模块名（模糊）", example = "contract")
    private String moduleName;

    @Schema(description = "设备ID（模糊）", example = "device-001")
    private String deviceId;

    @Schema(description = "是否开放", example = "true")
    private Boolean enabled;
}

