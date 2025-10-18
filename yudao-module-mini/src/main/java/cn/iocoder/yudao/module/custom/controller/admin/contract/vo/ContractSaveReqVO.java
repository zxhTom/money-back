package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 客户端新增/修改 Request VO")
@Data
public class ContractSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28827")
    private Long id;

    @Schema(description = "欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "欠款人姓名不能为空")
    private String indebtedName;

    @Schema(description = "欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "12399")
    @NotEmpty(message = "欠款人身份证不能为空")
    private String indebtedId;

    @Schema(description = "被欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "被欠款人姓名不能为空")
    private String creditorName;

    @Schema(description = "被欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "7287")
    @NotEmpty(message = "被欠款人身份证不能为空")
    private String creditorId;

    @Schema(description = "应用描述", example = "随便")
    private String description;

    @Schema(description = "合同状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "合同状态不能为空")
    private Integer status;

}