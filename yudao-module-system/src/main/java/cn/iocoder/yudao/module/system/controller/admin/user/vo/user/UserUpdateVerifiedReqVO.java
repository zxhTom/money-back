package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - 修改用户认证状态 Request VO")
@Data
public class UserUpdateVerifiedReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @Schema(description = "原认证状态：0-未认证，1-已认证", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "原认证状态不能为空")
    @Min(0)
    @Max(1)
    private Integer expectedVerified;

    @Schema(description = "目标认证状态：0-未认证，1-已认证", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标认证状态不能为空")
    @Min(0)
    @Max(1)
    private Integer verified;

    @Schema(description = "人工修改原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请填写修改原因")
    @Size(max = 200, message = "修改原因不能超过 200 字")
    private String reason;

}
