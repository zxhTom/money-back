package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import lombok.Data;

@Data
public class IpUserRefDO {
    private String ip;
    private Long   userId;
    private String username;
    private String lastSeen;
    private String firstSeen;
}
