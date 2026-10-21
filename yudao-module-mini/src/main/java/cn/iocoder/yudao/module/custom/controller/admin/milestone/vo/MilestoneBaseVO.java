package cn.iocoder.yudao.module.custom.controller.admin.milestone.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Data
public class MilestoneBaseVO {

    @Schema(description = "时间点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小程序年审")
    @NotBlank
    @Size(max = 128)
    private String title;

    @Schema(description = "分类：MINI_APP/PAY/DOMAIN/SERVER/WECHAT_WORK/OTHER", requiredMode = Schema.RequiredMode.REQUIRED, example = "MINI_APP")
    @NotBlank
    @Pattern(regexp = "MINI_APP|PAY|DOMAIN|SERVER|WECHAT_WORK|OTHER")
    private String category;

    @Schema(description = "关键时间节点", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @Schema(description = "提前提醒天数", example = "30")
    @Min(0)
    @Max(3650)
    private Integer remindDays;

    @Schema(description = "状态：0待处理 1处理中 2已完成 3忽略", example = "0")
    @Min(0)
    @Max(3)
    private Integer status;

    @Schema(description = "优先级：HIGH/MEDIUM/LOW", example = "HIGH")
    @Pattern(regexp = "HIGH|MEDIUM|LOW")
    private String priority;

    @Schema(description = "负责人", example = "张主管")
    @Size(max = 64)
    private String owner;

    @Schema(description = "费用金额", example = "300.00")
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal cost;

    @Schema(description = "备注与操作路线说明", example = "需要在公众平台提交营业执照")
    @Size(max = 10000)
    private String remark;
}
