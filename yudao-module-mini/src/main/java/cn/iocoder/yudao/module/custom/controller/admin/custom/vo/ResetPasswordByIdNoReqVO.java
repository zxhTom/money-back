package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * 管理后台 - 通过身份证+姓名重置密码 Request VO（B 方案：无邮箱时可用此方式找回）
 */
@Schema(description = "管理后台 - 通过身份证+姓名重置密码 Request VO")
@Data
public class ResetPasswordByIdNoReqVO {

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "真实姓名不能为空")
    private String realname;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110101199001011234")
    @NotEmpty(message = "身份证号不能为空")
    @Length(min = 15, max = 20, message = "身份证号长度 15-20 位")
    private String idNo;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;
}
