package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "数据访问日志 分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DataAccessLogPageReqVO extends PageParam {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "模块标识")
    private String module;

    @Schema(description = "实体类型")
    private String entityType;

    @Schema(description = "访问时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] accessTime;
}
