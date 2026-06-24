package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserIpStatDO {
    private String ip;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private Integer count;
}
