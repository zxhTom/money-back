package cn.iocoder.yudao.module.custom.controller.admin.skin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "小程序 - 当前生效皮肤 Response VO")
@Data
public class SkinAppRespVO {

    @Schema(description = "内部唯一标识", example = "preset-purple")
    private String code;
    @Schema(description = "配置模式：0=基础模式(tokens生效) 1=高级模式(customCssText追加覆盖tokens中同名token)", example = "0")
    private Integer configMode;
    @Schema(description = "token 键值对")
    private Map<String, String> tokens;
    @Schema(description = "声明式 CSS 变量文本")
    private String customCssText;

}
