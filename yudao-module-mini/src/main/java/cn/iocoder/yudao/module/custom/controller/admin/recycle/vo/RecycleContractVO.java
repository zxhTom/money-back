package cn.iocoder.yudao.module.custom.controller.admin.recycle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "合同回收站 VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecycleContractVO {

    @Schema(description = "合同 ID")
    private Long id;

    @Schema(description = "欠债方名称")
    private String indebtedName;

    @Schema(description = "欠债方 ID")
    private String indebtedId;

    @Schema(description = "债权方名称")
    private String creditorName;

    @Schema(description = "债权方 ID")
    private String creditorId;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "开始日期")
    private LocalDateTime startDate;

    @Schema(description = "结束日期")
    private LocalDateTime endDate;

    @Schema(description = "回款类型")
    private String returnType;

    @Schema(description = "原因类型")
    private String reasonType;

    @Schema(description = "归档时间")
    private LocalDateTime archiveTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
