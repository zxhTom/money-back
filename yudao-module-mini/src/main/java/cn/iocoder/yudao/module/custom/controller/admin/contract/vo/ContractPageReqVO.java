package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 客户端分页 Request VO")
@Data
public class ContractPageReqVO extends PageParam {

    @Schema(description = "欠款人姓名", example = "王五")
    private String indebtedName;

    @Schema(description = "欠款人身份证", example = "12399")
    private String indebtedId;

    @Schema(description = "被欠款人姓名", example = "王五")
    private String creditorName;

    @Schema(description = "被欠款人身份证", example = "7287")
    private String creditorId;

    @Schema(description = "应用描述", example = "随便")
    private String description;

    @Schema(description = "合同状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}