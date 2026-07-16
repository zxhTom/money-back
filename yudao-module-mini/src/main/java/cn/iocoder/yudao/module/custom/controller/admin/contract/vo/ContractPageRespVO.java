package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合同分页（/custom/contract/page）精简 Response VO
 *
 * 仅返回小程序信用查询详情页（credit-detail）实际渲染的字段，
 * 不包含双方姓名、证件号（密文/脱敏）、借款理由、附件路径等 {@link ContractRespVO} 中的完整信息，
 * 避免调用方拿到超出展示所需的合同明细。
 */
@Schema(description = "管理后台 - 合同分页（精简）Response VO")
@Data
public class ContractPageRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8477")
    private Long id;

    @Schema(description = "合同状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @Schema(description = "金额")
    private BigDecimal salary;

    @Schema(description = "结束时间")
    private LocalDateTime endDate;

}
