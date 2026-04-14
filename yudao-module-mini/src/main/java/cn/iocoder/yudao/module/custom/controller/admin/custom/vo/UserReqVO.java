package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * TODO
 *
 * @author zxhtom
 * 11/16/25
 */
@Data
public class UserReqVO {
    private String realname;
    @Schema(description = "身份证：手输明文或回传密文；服务端自动识别")
    private String idNo;
}
