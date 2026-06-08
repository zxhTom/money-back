package cn.iocoder.yudao.module.custom.controller.admin.audit.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 审计日志分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogPageReqVO extends PageParam {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "操作类型(CREATE/READ/UPDATE/DELETE/EXPORT/LOGIN)")
    private String operationType;

    @Schema(description = "业务模块")
    private String module;

    @Schema(description = "实体类型")
    private String entityType;

    @Schema(description = "实体ID")
    private Long entityId;

    @Schema(description = "外网IP")
    private String externalIp;

    @Schema(description = "操作结果(0=成功,1=失败)")
    private Integer status;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
