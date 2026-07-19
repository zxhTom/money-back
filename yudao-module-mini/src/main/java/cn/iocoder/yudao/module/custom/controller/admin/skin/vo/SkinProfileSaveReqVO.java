package cn.iocoder.yudao.module.custom.controller.admin.skin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - 皮肤配置创建/更新 Request VO")
@Data
public class SkinProfileSaveReqVO {

    @Schema(description = "主键，更新时必填", example = "1")
    private Long id;

    @Schema(description = "皮肤名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "紫色主题")
    @NotEmpty(message = "皮肤名称不能为空")
    private String name;

    @Schema(description = "配置模式：0=基础模式(tokens生效) 1=高级模式(customCssText追加覆盖tokens中同名token)",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "配置模式不能为空")
    private Integer configMode;

    @Schema(description = "token 键值对", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "tokens 不能为空")
    private Map<String, String> tokens;

    @Schema(description = "声明式 CSS 变量文本")
    private String customCssText;

    @Schema(description = "缩略图地址")
    private String thumbnailUrl;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

}
