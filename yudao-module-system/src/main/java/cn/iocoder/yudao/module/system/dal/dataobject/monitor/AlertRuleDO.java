package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_alert_rule")
@Data
public class AlertRuleDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String alertType;
    private String name;
    private String description;
    private Integer enabled;
    private Integer severity;
    private Integer threshold;
    private Integer windowSeconds;
    private Integer autoBan;
    private Long banDurationSeconds;
    /** 触发后是否逻辑删除用户并剔除其所有 token：0-否 1-是（仅对能定位到账号的规则生效） */
    private Integer autoDeleteUser;
    /** 触发后是否重置用户密码并剔除其所有 token：0-否 1-是（先改密码再踢 token，仅对能定位到账号的规则生效） */
    private Integer autoResetPassword;
    /** URL 监控规则专用：被监控的请求地址（可带或不带 /admin-api 前缀，服务端归一化匹配） */
    private String matchUrl;
    private String notifyChannels;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Integer deleted;
}
