package cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "小程序静态信息 Response VO（对外展示用，不含绑定用户信息）")
@Data
public class MiniProgramConfigRespVO {

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

}
