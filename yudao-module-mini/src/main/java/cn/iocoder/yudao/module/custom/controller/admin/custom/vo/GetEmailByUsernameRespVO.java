package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 通过登录名查询邮箱 Response VO
 *
 * @author zxhtom
 */
@Schema(description = "管理后台 - 通过登录名查询邮箱 Response VO")
@Data
public class GetEmailByUsernameRespVO {

    @Schema(description = "用户邮箱", example = "user@example.com")
    private String email;

}
