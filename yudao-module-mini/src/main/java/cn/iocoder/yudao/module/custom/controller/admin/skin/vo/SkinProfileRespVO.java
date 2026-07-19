package cn.iocoder.yudao.module.custom.controller.admin.skin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 皮肤配置 Response VO")
@Data
public class SkinProfileRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;
    @Schema(description = "皮肤名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "紫色主题")
    private String name;
    @Schema(description = "内部唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "preset-purple")
    private String code;
    @Schema(description = "类型：0=预设(不可删除) 1=自定义", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;
    @Schema(description = "若基于某预设克隆创建，记录来源皮肤 id", example = "1")
    private Long sourcePresetId;
    @Schema(description = "配置模式：0=基础模式(tokens生效) 1=高级模式(customCssText追加覆盖tokens中同名token)",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer configMode;
    @Schema(description = "token 键值对")
    private Map<String, String> tokens;
    @Schema(description = "声明式 CSS 变量文本")
    private String customCssText;
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
