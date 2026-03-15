package cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 时间窗口创建 Request VO")
@Data
public class TimeWindowCreateReqVO {

    @Schema(description = "开始时间（毫秒时间戳，与合同 startDate 一致）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @Schema(description = "结束时间（毫秒时间戳，与合同 endDate 一致）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    @Schema(description = "快捷选择：1/3/7，可选")
    private Integer quickSelect;
    @Schema(description = "按用户或按角色：user=按用户 role=按角色，不传时前端默认 user", example = "user")
    private String targetType;
    @Schema(description = "选定用户 ID 列表，targetType=user 时使用")
    private List<Long> selectedUserIds;
    @Schema(description = "排除用户 ID 列表，与选定重合时以排除为准")
    private List<Long> excludedUserIds;
    @Schema(description = "选定角色 ID 列表，targetType=role 时使用")
    private List<Long> selectedRoleIds;
    @Schema(description = "排除角色 ID 列表，与选定重合时以排除为准")
    private List<Long> excludedRoleIds;
    @Schema(description = "激活状态：0 未激活 1 激活", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "激活状态不能为空")
    private Integer status;
}
