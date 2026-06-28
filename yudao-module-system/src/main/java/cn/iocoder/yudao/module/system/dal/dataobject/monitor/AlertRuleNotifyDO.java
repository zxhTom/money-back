package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_alert_rule_notify")
@Data
public class AlertRuleNotifyDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    /** USER / ROLE / DEPT */
    private String targetType;
    private Long targetId;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Integer deleted;
}
