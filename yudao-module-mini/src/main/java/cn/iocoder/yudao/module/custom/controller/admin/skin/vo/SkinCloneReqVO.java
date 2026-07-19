package cn.iocoder.yudao.module.custom.controller.admin.skin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 皮肤克隆 Request VO")
@Data
public class SkinCloneReqVO {

    @Schema(description = "来源预设皮肤 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源预设皮肤 id 不能为空")
    private Long presetId;

    @Schema(description = "新皮肤名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "我的紫色主题")
    @NotEmpty(message = "皮肤名称不能为空")
    private String name;

}
