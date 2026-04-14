package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "身份证密文解密响应（仅本接口返回明文）")
@Data
public class IdCardDecryptRespVO {

    @Schema(description = "明文身份证号")
    private String plain;
}
