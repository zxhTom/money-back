package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 文案配置创建/更新 Request VO")
@Data
public class TextProfileSaveReqVO {

    @Schema(description = "主键，更新时必填", example = "1")
    private Long id;

    @Schema(description = "文案套名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认文案")
    @NotEmpty(message = "文案套名称不能为空")
    private String name;

    @Schema(description = "备注")
    private String remark;

}
