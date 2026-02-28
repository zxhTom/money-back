package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 管理后台 - 通过登录名查询邮箱 Request VO
 *
 * @author zxhtom
 */
@Schema(description = "管理后台 - 通过登录名查询邮箱 Request VO")
@Data
public class GetEmailByUsernameReqVO {

    @Schema(description = "登录名（用户名）", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotEmpty(message = "登录名不能为空")
    private String username;

}
