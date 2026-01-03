package cn.iocoder.yudao.module.custom.controller.admin.wechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小程序版本号查询响应 VO
 *
 * @author zxhtom
 */
@Schema(description = "管理后台 - 小程序版本号查询响应")
@Data
public class MiniProgramVersionRespVO {

    @Schema(description = "小程序线上版本号", example = "1.0.0")
    private String version;

    @Schema(description = "小程序AppID", example = "wx7641cb883b4fa670")
    private String appId;

}

