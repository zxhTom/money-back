package cn.iocoder.yudao.module.custom.controller.admin.baidu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * TODO
 *
 * @author zxhtom
 * 12/3/25
 */
@Data
public class BaiduUserInfo {
    private String verifyToken;
    private String name;
    @Schema(description = "身份证：明文或密文，服务端自动识别")
    private String idCard;
}
