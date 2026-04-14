package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RecentContractVO {
    @Schema(description = "对方证件密文（与用户 idNo 同一套加密）")
    private String idNo;
    @Schema(description = "对方证件展示（前6+*+后4）")
    private String idNoDisplay;
    private String name;
    private String avatarUrl;
}
