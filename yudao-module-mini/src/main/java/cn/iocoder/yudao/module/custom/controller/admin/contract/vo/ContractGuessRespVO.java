package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 猜你喜欢返回结果")
@Data
public class ContractGuessRespVO {
    @Schema(description = "推荐人姓名")
    private String targetName;

    @Schema(description = "推荐人身份证")
    private String targetIdCard;
}
