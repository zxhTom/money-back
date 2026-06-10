package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import lombok.Data;

import java.util.List;

@Data
public class GlobalIpDiagnoseVO {
    private String ip;
    private boolean risky;
    private List<String> risks;
    private String type;
    private String country;
    private String city;
    private String carrier;
    /** 使用过该 IP 的用户列表 */
    private List<IpUserInfoVO> users;
    /** 最早出现时间 */
    private String firstSeen;
    /** 最近出现时间 */
    private String lastSeen;
    private boolean cached;
    private String error;
}
