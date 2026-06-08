package cn.iocoder.yudao.module.custom.controller.admin.security.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "安全告警处理 Request VO")
@Data
public class SecurityAlertHandleReqVO {

    @Schema(description = "告警ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(description = "处理状态(1=已处理,2=误报)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer handled;

    @Schema(description = "处理备注")
    private String handleRemark;
}
