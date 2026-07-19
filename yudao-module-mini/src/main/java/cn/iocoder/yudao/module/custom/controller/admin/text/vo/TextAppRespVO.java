package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "小程序 - 当前生效文案 Response VO")
@Data
public class TextAppRespVO {

    @Schema(description = "内部唯一标识", example = "text-abcdef")
    private String profileCode;

    @Schema(description = "扁平的 itemKey -> itemValue 映射")
    private Map<String, String> texts;

}
