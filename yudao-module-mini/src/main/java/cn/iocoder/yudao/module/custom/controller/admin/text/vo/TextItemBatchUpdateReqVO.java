package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 文案条目批量保存 Request VO")
@Data
public class TextItemBatchUpdateReqVO {

    @Schema(description = "所属文案套 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "文案套 id 不能为空")
    private Long profileId;

    @Schema(description = "页面标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.contractDetail")
    @NotEmpty(message = "页面标识不能为空")
    private String pageKey;

    @Schema(description = "文案条目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "文案条目列表不能为空")
    @Valid
    private List<TextItemSaveReqVO> items;

}
