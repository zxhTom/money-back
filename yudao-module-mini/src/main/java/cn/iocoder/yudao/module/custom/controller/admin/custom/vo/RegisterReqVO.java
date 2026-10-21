package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/** 公开注册只接收个人资料，不接收认证、权限、租户或账户控制字段。 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterReqVO {
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(max = 72, message = "密码过长")
    private String password;
    private String realname;
    @Size(max = 512)
    private String idNo;
    @Size(max = 512)
    private String idCard;
    private String nickname;
    private String mobile;
    private String email;
    private String avatar;
    private String inviteCode;
    private Date birthDate;
    private String address;
    private Integer occupation;
    private Integer education;
    private Integer sex;
}
