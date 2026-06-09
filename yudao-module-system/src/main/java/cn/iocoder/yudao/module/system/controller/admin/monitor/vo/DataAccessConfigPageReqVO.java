package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "数据访问监控配置 分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DataAccessConfigPageReqVO extends PageParam {

    @Schema(description = "模块标识")
    private String module;

    @Schema(description = "实体类型")
    private String entityType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
