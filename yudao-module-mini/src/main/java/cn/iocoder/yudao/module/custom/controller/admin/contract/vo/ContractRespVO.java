package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 合同 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ContractRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11602")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("欠款人姓名")
    private String indebtedName;

    @Schema(description = "欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "22591")
    @ExcelProperty("欠款人身份证")
    private String indebtedId;

    @Schema(description = "被欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("被欠款人姓名")
    private String creditorName;

    @Schema(description = "被欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "18548")
    @ExcelProperty("被欠款人身份证")
    private String creditorId;

    @Schema(description = "应用描述", example = "你猜")
    @ExcelProperty("应用描述")
    private String description;

    @Schema(description = "合同状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("合同状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startDate;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endDate;

    @Schema(description = "还款方式", example = "2")
    @ExcelProperty("还款方式")
    private String returnType;

    @Schema(description = "理由", example = "1")
    @ExcelProperty("理由")
    private String reasonType;

    @Schema(description = "详细理由", example = "不喜欢")
    @ExcelProperty("详细理由")
    private String detailReason;

    @Schema(description = "金额")
    @ExcelProperty("金额")
    private Long salary;

    @Schema(description = "费率")
    @ExcelProperty("费率")
    private Long tariff;

    @Schema(description = "借条附件")
    @ExcelProperty("借条附件")
    private String file;

}