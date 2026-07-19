package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文案配置 Response VO")
@Data
public class TextProfileRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;
    @Schema(description = "文案套名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认文案")
    private String name;
    @Schema(description = "内部唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "text-abcdef")
    private String code;
    @Schema(description = "种子来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "safe")
    private String seedFrom;
    @Schema(description = "是否生效：全表仅一条应为 true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
