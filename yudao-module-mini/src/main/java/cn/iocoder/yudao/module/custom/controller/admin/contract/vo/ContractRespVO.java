package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 合同 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ContractRespVO {

    private Long creator;
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8477")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("欠款人姓名")
    private String indebtedName;

    @Schema(description = "欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "28340")
    @ExcelProperty("欠款人身份证")
    private String indebtedId;

    @Schema(description = "被欠款人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("被欠款人姓名")
    private String creditorName;

    @Schema(description = "被欠款人身份证", requiredMode = Schema.RequiredMode.REQUIRED, example = "23897")
    @ExcelProperty("被欠款人身份证")
    private String creditorId;

    @Schema(description = "应用描述", example = "你猜")
    @ExcelProperty("应用描述")
    private String description;

    @Schema(description = "合同状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "合同状态", converter = DictConvert.class)
    @DictFormat("contract_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
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

    @Schema(description = "还款方式", example = "1")
    @ExcelProperty(value = "还款方式", converter = DictConvert.class)
    @DictFormat("return_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String returnType;

    @Schema(description = "理由", example = "2")
    @ExcelProperty(value = "理由", converter = DictConvert.class)
    @DictFormat("reason_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String reasonType;

    @Schema(description = "详细理由", example = "不对")
    @ExcelProperty("详细理由")
    private String detailReason;

    @Schema(description = "金额")
    @ExcelProperty("金额")
    private BigDecimal salary;

    @Schema(description = "费率")
    @ExcelProperty("费率")
    private BigDecimal tariff;

    @Schema(description = "借条附件")
    @ExcelProperty("借条附件")
    private String file;

    @Schema(description = "利息")
    @ExcelProperty("利息")
    private BigDecimal interest;

    @Schema(description = "已还金额")
    @ExcelProperty("已还金额")
    private BigDecimal refund;

}