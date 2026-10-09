package cn.iocoder.yudao.module.custom.controller.admin.milestone.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 节点 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MilestoneRespVO extends MilestoneBaseVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "剩余天数（负数代表已过期）")
    private Integer daysLeft;

    @Schema(description = "预警状态：EXPIRED=已过期, URGENT=紧急预警(30天内), NORMAL=正常, DONE=已完成/忽略")
    private String alertStatus;
}
