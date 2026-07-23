package cn.iocoder.yudao.module.custom.controller.admin.iconset.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Schema(description = "管理后台 - 图标集配置创建/更新 Request VO")
@Data
public class IconSetProfileSaveReqVO {

    @Schema(description = "主键，更新时必填", example = "1")
    private Long id;

    @Schema(description = "图标集名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "圆润填充风")
    @NotEmpty(message = "图标集名称不能为空")
    private String name;

    @Schema(description = "图标key -> SVG源码，缺失的key前端回退到预设图案")
    private Map<String, String> icons;

    @Schema(description = "缩略图地址")
    private String thumbnailUrl;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

}
