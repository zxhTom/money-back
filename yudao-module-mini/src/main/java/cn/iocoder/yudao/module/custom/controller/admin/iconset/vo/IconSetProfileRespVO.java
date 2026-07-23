package cn.iocoder.yudao.module.custom.controller.admin.iconset.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 图标集配置 Response VO")
@Data
public class IconSetProfileRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;
    @Schema(description = "图标集名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "圆润填充风")
    private String name;
    @Schema(description = "内部唯一标识", example = "preset-tabler-outline")
    private String code;
    @Schema(description = "类型：0=预设(不可删除) 1=自定义", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;
    @Schema(description = "若基于某预设克隆创建，记录来源图标集 id", example = "1")
    private Long sourcePresetId;
    @Schema(description = "图标key -> SVG源码")
    private Map<String, String> icons;
    @Schema(description = "缩略图地址")
    private String thumbnailUrl;
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
