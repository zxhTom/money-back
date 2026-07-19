package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文案条目 Response VO")
@Data
public class TextItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;
    @Schema(description = "所属文案套 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long profileId;
    @Schema(description = "页面标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.contractDetail")
    private String pageKey;
    @Schema(description = "模块标识", example = "header")
    private String moduleKey;
    @Schema(description = "完整文案 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.contractDetail.title")
    private String itemKey;
    @Schema(description = "文案内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "合同详情")
    private String itemValue;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
