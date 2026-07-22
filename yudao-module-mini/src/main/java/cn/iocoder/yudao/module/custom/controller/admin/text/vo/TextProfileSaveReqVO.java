package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Schema(description = "管理后台 - 文案配置创建/更新 Request VO")
@Data
public class TextProfileSaveReqVO {

    @Schema(description = "主键，更新时必填", example = "1")
    private Long id;

    @Schema(description = "文案套名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认文案")
    @NotEmpty(message = "文案套名称不能为空")
    private String name;

    @Schema(description = "文案套类型：safe=安全模式基准 offcial=正式模式基准，仅创建时生效、创建后不可变",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "safe")
    @NotEmpty(message = "文案套类型不能为空")
    @Pattern(regexp = "^(safe|offcial)$", message = "文案套类型只能是 safe 或 offcial")
    private String textMode;

    @Schema(description = "备注")
    private String remark;

}
