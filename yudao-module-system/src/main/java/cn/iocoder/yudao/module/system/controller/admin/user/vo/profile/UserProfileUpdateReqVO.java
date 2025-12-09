package cn.iocoder.yudao.module.system.controller.admin.user.vo.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Date;


@Schema(description = "管理后台 - 用户个人信息更新 Request VO")
@Data
public class UserProfileUpdateReqVO {

    @Schema(description = "用户昵称", example = "芋艿")
    @Size(max = 300, message = "用户昵称长度不能超过 300 个字符")
    private String nickname;
    private String username;

    @Schema(description = "用户邮箱", example = "yudao@iocoder.cn")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @Schema(description = "手机号码", example = "15601691300")
    @Length(min = 11, max = 11, message = "手机号长度必须 11 位")
    private String mobile;

    @Schema(description = "用户性别，参见 SexEnum 枚举类", example = "1")
    private Integer sex;

    @Schema(description = "角色头像", example = "https://www.iocoder.cn/1.png")
    @URL(message = "头像地址格式不正确")
    private String avatar;

    @Schema(description = "生日", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private Date birthDate;
    @Schema(description = "居住地址", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private String address;
    @Schema(description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private Integer occupation;
    @Schema(description = "学历", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private Integer education;
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private String realname;
    @Schema(description = "身份证", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "yudao")
    private String idNo;

}
