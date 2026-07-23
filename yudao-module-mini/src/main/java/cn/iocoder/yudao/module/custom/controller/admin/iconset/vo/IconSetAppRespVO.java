package cn.iocoder.yudao.module.custom.controller.admin.iconset.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "小程序 - 当前生效图标集 Response VO")
@Data
public class IconSetAppRespVO {

    @Schema(description = "内部唯一标识", example = "preset-tabler-outline")
    private String code;
    @Schema(description = "图标key -> SVG源码")
    private Map<String, String> icons;

}
