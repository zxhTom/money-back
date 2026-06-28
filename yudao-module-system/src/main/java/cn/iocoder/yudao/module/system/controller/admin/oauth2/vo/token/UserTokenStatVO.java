package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token;

import lombok.Data;

@Data
public class UserTokenStatVO {
    private Long userId;
    private String username;
    private Long tokenCount;
}
