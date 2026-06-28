package cn.iocoder.yudao.module.custom.controller.admin.audit.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 审计日志 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AuditLogRespVO {

    @Schema(description = "日志ID")
    @ExcelProperty("日志ID")
    private Long id;

    @Schema(description = "链路追踪ID")
    private String traceId;

    @Schema(description = "用户ID")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "用户类型")
    @ExcelProperty("用户类型")
    private Integer userType;

    @Schema(description = "用户名")
    @ExcelProperty("用户名")
    private String username;

    @Schema(description = "业务模块")
    @ExcelProperty("业务模块")
    private String module;

    @Schema(description = "操作类型")
    @ExcelProperty("操作类型")
    private String operationType;

    @Schema(description = "实体类型")
    @ExcelProperty("实体类型")
    private String entityType;

    @Schema(description = "实体ID")
    @ExcelProperty("实体ID")
    private Long entityId;

    @Schema(description = "操作描述")
    @ExcelProperty("操作描述")
    private String operation;

    @Schema(description = "操作前数据快照")
    private String beforeData;

    @Schema(description = "操作后数据快照")
    private String afterData;

    @Schema(description = "接口请求参数(JSON，密码已脱敏)")
    private String requestParams;

    @Schema(description = "请求URL")
    @ExcelProperty("请求URL")
    private String requestUrl;

    @Schema(description = "请求方法")
    @ExcelProperty("请求方法")
    private String requestMethod;

    @Schema(description = "外网IP")
    @ExcelProperty("外网IP")
    private String externalIp;

    @Schema(description = "直连IP")
    @ExcelProperty("直连IP")
    private String directIp;

    @Schema(description = "所有IP来源")
    private String allIpHeaders;

    @Schema(description = "浏览器UA")
    private String userAgent;

    @Schema(description = "操作结果(0=成功,1=失败)")
    @ExcelProperty("操作结果")
    private Integer status;

    @Schema(description = "失败原因")
    @ExcelProperty("失败原因")
    private String errorMessage;

    @Schema(description = "耗时(ms)")
    @ExcelProperty("耗时ms")
    private Integer costTime;

    @Schema(description = "操作时间")
    @ExcelProperty("操作时间")
    private LocalDateTime createTime;
}
