package cn.iocoder.yudao.module.custom.controller.admin.invite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "邀请码 Response VO")
@Data
public class InviteCodeRespVO {

    @Schema(description = "邀请码ID")
    private Long id;

    @Schema(description = "邀请码")
    private String code;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态：0-有效 1-已失效")
    private Integer status;

    @Schema(description = "是否仍有效（未过期且状态为有效）")
    private Boolean valid;

    @Schema(description = "通过该码成功注册的人数")
    private Integer registerCount;

    @Schema(description = "生成时间")
    private LocalDateTime createTime;

}
