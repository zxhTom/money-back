package cn.iocoder.yudao.module.custom.controller.admin.iconset.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 图标集配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IconSetProfilePageReqVO extends PageParam {

    @Schema(description = "图标集名称，模糊查询", example = "圆润")
    private String name;

    @Schema(description = "类型：0=预设(不可删除) 1=自定义", example = "1")
    private Integer type;

}
