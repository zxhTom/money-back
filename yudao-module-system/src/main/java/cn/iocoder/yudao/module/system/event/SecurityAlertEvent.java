package cn.iocoder.yudao.module.system.event;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SecurityAlertEvent extends ApplicationEvent {

    private final SecurityAlertDO alert;
    private final Long ruleId;

    public SecurityAlertEvent(Object source, SecurityAlertDO alert, Long ruleId) {
        super(source);
        this.alert = alert;
        this.ruleId = ruleId;
    }
}
