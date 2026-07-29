package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleNotifyDO;

import java.util.List;
import java.util.Set;

public interface AlertRuleService {

    List<AlertRuleDO> listAll();

    AlertRuleDO getById(Long id);

    /** 按 alertType 获取有效规则（走缓存） */
    AlertRuleDO getCachedRule(String alertType);

    /** 获取所有启用的 URL 监控规则（走缓存，支持配置多个不同 URL） */
    List<AlertRuleDO> getUrlMonitorRules();

    Long create(AlertRuleDO rule, List<AlertRuleNotifyDO> notifies);

    void update(AlertRuleDO rule, List<AlertRuleNotifyDO> notifies);

    void delete(Long id);

    void setEnabled(Long id, boolean enabled);

    void setExposeReason(Long id, boolean exposeReason);

    List<AlertRuleNotifyDO> listNotifies(Long ruleId);

    /** 展开通知对象，返回需要发送站内信的 userId 集合 */
    Set<Long> resolveNotifyUserIds(Long ruleId);
}
