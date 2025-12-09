package cn.iocoder.yudao.module.custom.controller.admin.baidu.vo;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

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
    private String idCard;
}
