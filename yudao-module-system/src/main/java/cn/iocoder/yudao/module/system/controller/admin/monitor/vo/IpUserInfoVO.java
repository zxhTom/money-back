package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import lombok.Data;

import java.util.List;

@Data
public class IpUserInfoVO {
    private Long userId;
    private String username;
    /** 来源日志：登录日志 / 操作日志 / 数据访问日志 */
    private List<String> sources;
    private String lastSeen;
}
