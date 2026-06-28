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
    private String notifyChannels;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Integer deleted;
}
