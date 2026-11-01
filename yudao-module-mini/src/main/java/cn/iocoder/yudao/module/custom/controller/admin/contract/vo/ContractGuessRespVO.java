package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 猜你喜欢返回结果")
@Data
public class ContractGuessRespVO {
    @Schema(description = "推荐人姓名")
    private String targetName;

    @Schema(description = "推荐人身份证")
    private String targetIdCard;

    @Schema(description = "推荐人身份证脱敏展示（前六位、后四位）")
    private String targetIdCardDisplay;

    /** Opaque, one-time handle for a suggested user without prior contracts. */
    private String candidateToken;

    /** Scoring data is internal and must not change the response read by older clients. */
    @JsonIgnore
    private Long targetUserId;
    @JsonIgnore
    private Long relationCount;
    @JsonIgnore
    private Long issuedCount;
    @JsonIgnore
    private LocalDateTime lastLoginTime;
    @JsonIgnore
    private LocalDateTime lastContractTime;
}
