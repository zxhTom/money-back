package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.module.custom.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户端 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ContractRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28827")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("欠款人姓名")
    private String indebtedName;

    @Schema(description = "欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "12399")
    @ExcelProperty("欠款人身份证")
    private String indebtedId;

    @Schema(description = "被欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("被欠款人姓名")
    private String creditorName;

    @Schema(description = "被欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "7287")
    @ExcelProperty("被欠款人身份证")
    private String creditorId;

    @Schema(description = "应用描述", example = "随便")
    @ExcelProperty("应用描述")
    private String description;

    @Schema(description = "合同状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("合同状态")
    @DictFormat(DictTypeConstants.CONTRACT_STATUS)
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
