package cn.iocoder.yudao.module.custom.controller.admin.milestone.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 节点分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MilestonePageReqVO extends PageParam {

    @Schema(description = "时间点名称")
    private String title;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "关键时间节点范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] targetDate;
}
