package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 合同分页 Request VO")
@Data
public class ContractPageReqVO extends PageParam {

    @Schema(description = "欠款人姓名", example = "李四")
    private String indebtedName;

    @Schema(description = "欠款人身份证筛选：明文或密文")
    private String indebtedId;

    @Schema(description = "被欠款人姓名", example = "王五")
    private String creditorName;

    @Schema(description = "被欠款人身份证筛选：明文或密文")
    private String creditorId;

    @Schema(description = "应用描述", example = "你猜")
    private String description;

    @Schema(description = "合同状态", example = "2")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startDate;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endDate;

    @Schema(description = "还款方式", example = "1")
    private String returnType;

    @Schema(description = "理由", example = "2")
    private String reasonType;

    @Schema(description = "详细理由", example = "不对")
    private String detailReason;

    @Schema(description = "金额")
    private BigDecimal salary;

    @Schema(description = "费率")
    private BigDecimal tariff;

    @Schema(description = "借条附件")
    private String file;

    @Schema(description = "利息")
    private BigDecimal interest;

    @Schema(description = "已还金额")
    private BigDecimal refund;

}