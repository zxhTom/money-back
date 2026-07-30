package cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "小程序静态信息保存 Request VO")
@Data
public class MiniProgramConfigSaveReqVO {

    @Schema(description = "小程序名称")
    private String appName;

    @Schema(description = "一句话简介")
    private String slogan;

    @Schema(description = "详细描述")
    private String appDescription;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "绑定的代表用户ID，null表示解绑")
    private Long boundUserId;

}
