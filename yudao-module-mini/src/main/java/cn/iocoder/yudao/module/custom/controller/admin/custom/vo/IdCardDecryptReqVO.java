package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "身份证密文解密请求")
@Data
public class IdCardDecryptReqVO {

    @Schema(description = "证件密文（与 profile/合同一致）；本接口按用户限流，见 yudao.id-card.decrypt-api", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "cipher 不能为空")
    private String cipher;

    @Schema(description = "业务场景，便于审计日志。示例：system_user_list、contract_list_indebted、contract_list_creditor、view_detail")
    private String scene;
}
