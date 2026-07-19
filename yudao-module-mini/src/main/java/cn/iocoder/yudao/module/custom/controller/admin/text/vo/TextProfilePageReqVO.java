package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 文案配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TextProfilePageReqVO extends PageParam {

    @Schema(description = "文案套名称，模糊查询", example = "默认文案")
    private String name;

}
