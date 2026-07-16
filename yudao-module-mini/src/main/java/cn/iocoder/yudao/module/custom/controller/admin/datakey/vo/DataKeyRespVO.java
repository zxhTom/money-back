package cn.iocoder.yudao.module.custom.controller.admin.datakey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 数据密钥 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataKeyRespVO {

    @Schema(description = "密钥版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long version;

    @Schema(description = "半密钥，客户端需按约定规则派生出会话密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyPart;

}
