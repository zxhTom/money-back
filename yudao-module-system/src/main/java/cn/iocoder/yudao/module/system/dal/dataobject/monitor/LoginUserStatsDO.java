package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginUserStatsDO {
    private Long          userId;
    private String        username;
    private Integer       loginCount;
    private Integer       failCount;
    private Integer       distinctIpCount;
    private LocalDateTime lastLoginTime;
}
