package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import lombok.Data;

@Data
public class UserIpStatVO {
    private Long userId;
    private String username;
    private Long ipCount;
}
