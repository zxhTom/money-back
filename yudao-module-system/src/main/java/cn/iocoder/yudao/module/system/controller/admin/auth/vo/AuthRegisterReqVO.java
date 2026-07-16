package cn.iocoder.yudao.module.system.controller.admin.auth.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.util.Date;

@Schema(description = "管理后台 - Register Request VO")
@Data
public class AuthRegisterReqVO extends CaptchaVerificationReqVO {

    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "zxhtom")
    @NotBlank(message = "用户账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,300}$", message = "用户账号由 数字、字母 组成")
    @Size(min = 4, max = 300, message = "用户账号长度为 4-300 个字符")
    private String username;
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "zxhtom")
    private String realname;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 300, message = "用户昵称长度不能超过 300 个字符")
    private String nickname;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;
    @Schema(description = "身份证：用户手输多为明文，亦可传密文；服务端自动识别", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Length(max = 512, message = "证件参数过长")
    private String idNo;
    @Schema(description = "邀请码（是否必填由后端开关 system.user.invite-register-required 决定）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Length(max = 64, message = "邀请码过长")
    private String inviteCode;
    @Schema(description = "生日", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private Date birthDate;
    @Schema(description = "居住地址", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private String address;
    @Schema(description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private Integer occupation;
    @Schema(description = "学历", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private Integer education;

}