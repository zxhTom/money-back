package cn.iocoder.yudao.module.custom.dto;

import lombok.Data;

/**
 * @author zxhtom
 * 12/25/25
 */
@Data
public class MpVO {
    private Long userId;
    private String realname;
    private Integer subscribeStatus;
    private Integer code;
    private String msg;
}
