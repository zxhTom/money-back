package cn.iocoder.yudao.module.custom.controller.admin.invite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "邀请注册记录 Response VO")
@Data
public class InviteRegisterLogRespVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "被邀请人用户ID")
    private Long inviteeUserId;

    @Schema(description = "被邀请人用户名")
    private String inviteeUsername;

    @Schema(description = "被邀请人真实姓名")
    private String inviteeRealname;

    @Schema(description = "注册来源IP")
    private String registerIp;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;

}
