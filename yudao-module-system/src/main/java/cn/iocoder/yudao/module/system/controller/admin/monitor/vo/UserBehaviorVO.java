package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserBehaviorVO {
    private Long userId;
    private String username;
    /** 近 30 天登录次数 */
    private int loginCount;
    /** 近 30 天登录失败次数 */
    private int failCount;
    /** 近 1 小时登录次数（高频预警） */
    private int loginCountLastHour;
    /** 近 30 天使用的不同 IP 数 */
    private int distinctIpCount;
    private String lastLoginTime;
    private String lastLoginIp;
    /** ACTIVE(7天内) / NORMAL(30天内) / INACTIVE(超30天) */
    private String activityLevel;
    /** 被标记为风险的 IP 列表 */
    private List<String> abnormalIps;
    /** 是否存在异常（高频 / 多 IP / 高失败率 / 风险IP） */
    private boolean suspicious;
    /** 异常原因描述 */
    private List<String> suspiciousReasons;
}
