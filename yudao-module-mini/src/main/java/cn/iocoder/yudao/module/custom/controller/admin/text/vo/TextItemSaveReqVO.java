package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 文案条目保存 Request VO")
@Data
public class TextItemSaveReqVO {

    @Schema(description = "模块标识", example = "header")
    private String moduleKey;

    @Schema(description = "完整文案 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.contractDetail.title")
    @NotEmpty(message = "文案 key 不能为空")
    private String itemKey;

    @Schema(description = "文案内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "合同详情")
    @NotEmpty(message = "文案内容不能为空")
    private String itemValue;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

}
