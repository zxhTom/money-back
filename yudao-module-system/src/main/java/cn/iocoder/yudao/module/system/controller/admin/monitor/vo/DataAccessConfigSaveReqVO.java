package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "数据访问监控配置 保存 Request VO")
@Data
public class DataAccessConfigSaveReqVO {

    @Schema(description = "ID（更新时必填）")
    private Long id;

    @Schema(description = "模块标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模块标识不能为空")
    private String module;

    @Schema(description = "实体类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "实体类型不能为空")
    private String entityType;

    @Schema(description = "用户ID(null=全局默认)")
    private Long userId;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "时间窗口内最大访问条数(null=不限制)")
    private Integer maxRecordsPerWindow;

    @Schema(description = "时间窗口(秒)，默认86400")
    private Integer windowSeconds;

    @Schema(description = "备注")
    private String remark;
}
