package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 合同新增/修改 Request VO")
@Data
public class ContractSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11602")
    private Long id;

    @Schema(description = "欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "欠款人姓名不能为空")
    private String indebtedName;

    @Schema(description = "欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "22591")
    @NotEmpty(message = "欠款人身份证不能为空")
    private String indebtedId;

    @Schema(description = "被欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "被欠款人姓名不能为空")
    private String creditorName;

    @Schema(description = "被欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "18548")
    @NotEmpty(message = "被欠款人身份证不能为空")
    private String creditorId;

    @Schema(description = "应用描述", example = "你猜")
    private String description;

    @Schema(description = "合同状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "合同状态不能为空")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startDate;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endDate;

    @Schema(description = "还款方式", example = "2")
    private String returnType;

    @Schema(description = "理由", example = "1")
    private String reasonType;

    @Schema(description = "详细理由", example = "不喜欢")
    private String detailReason;

    @Schema(description = "金额")
    private Long salary;

    @Schema(description = "费率")
    private Long tariff;

    @Schema(description = "借条附件")
    private String file;

}