package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 文案套克隆 Request VO")
@Data
public class TextProfileCloneReqVO {

    @Schema(description = "来源文案套 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源文案套 id 不能为空")
    private Long sourceId;

    @Schema(description = "新文案套名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "我的文案")
    @NotEmpty(message = "文案套名称不能为空")
    private String name;

}
