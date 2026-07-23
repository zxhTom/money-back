package cn.iocoder.yudao.module.custom.controller.admin.iconset.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 图标集克隆 Request VO")
@Data
public class IconSetCloneReqVO {

    @Schema(description = "来源预设图标集 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源预设图标集 id 不能为空")
    private Long presetId;

    @Schema(description = "新图标集名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "我的自定义图标")
    @NotEmpty(message = "图标集名称不能为空")
    private String name;

}
