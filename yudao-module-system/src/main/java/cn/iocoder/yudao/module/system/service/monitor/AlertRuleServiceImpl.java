package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleNotifyDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.AlertRuleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.AlertRuleNotifyMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Slf4j
@Service
public class AlertRuleServiceImpl implements AlertRuleService {

    @Resource private AlertRuleMapper alertRuleMapper;
    @Resource private AlertRuleNotifyMapper alertRuleNotifyMapper;
    @Resource private UserRoleMapper userRoleMapper;
    @Resource private AdminUserService adminUserService;

    private final Map<String, AlertRuleDO> ruleCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadCache() {
        List<AlertRuleDO> all = alertRuleMapper.selectList(null);
        ruleCache.clear();
        all.forEach(r -> ruleCache.put(r.getAlertType(), r));
    }

    @Override
    public List<AlertRuleDO> listAll() {
        return alertRuleMapper.selectList(null);
    }

    @Override
    public AlertRuleDO getById(Long id) {
        return alertRuleMapper.selectById(id);
    }

    @Override
    public AlertRuleDO getCachedRule(String alertType) {
        return ruleCache.get(alertType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AlertRuleDO rule, List<AlertRuleNotifyDO> notifies) {
        rule.setCreator("admin");
        rule.setUpdater("admin");
        rule.setDeleted(0);
        alertRuleMapper.insert(rule);
        saveNotifies(rule.getId(), notifies);
        loadCache();
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AlertRuleDO rule, List<AlertRuleNotifyDO> notifies) {
        rule.setUpdater("admin");
        rule.setUpdateTime(LocalDateTime.now());
        alertRuleMapper.updateById(rule);
        alertRuleNotifyMapper.deleteByRuleId(rule.getId());
        saveNotifies(rule.getId(), notifies);
        loadCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        alertRuleMapper.deleteById(id);
        alertRuleNotifyMapper.deleteByRuleId(id);
        loadCache();
    }

    @Override
    public void setEnabled(Long id, boolean enabled) {
        AlertRuleDO rule = new AlertRuleDO();
        rule.setId(id);
        rule.setEnabled(enabled ? 1 : 0);
        rule.setUpdater("admin");
        rule.setUpdateTime(LocalDateTime.now());
        alertRuleMapper.updateById(rule);
        loadCache();
    }

    @Override
    public List<AlertRuleNotifyDO> listNotifies(Long ruleId) {
        return alertRuleNotifyMapper.selectByRuleId(ruleId);
    }

    @Override
    public Set<Long> resolveNotifyUserIds(Long ruleId) {
        List<AlertRuleNotifyDO> notifies = alertRuleNotifyMapper.selectByRuleId(ruleId);
        Set<Long> userIds = new HashSet<>();
        for (AlertRuleNotifyDO n : notifies) {
            switch (n.getTargetType()) {
                case "USER":
                    userIds.add(n.getTargetId());
                    break;
                case "ROLE":
                    userIds.addAll(convertSet(
                            userRoleMapper.selectListByRoleIds(Collections.singleton(n.getTargetId())),
                            ur -> ur.getUserId()));
                    break;
                case "DEPT":
                    userIds.addAll(convertSet(
                            adminUserService.getUserListByDeptIds(Collections.singleton(n.getTargetId())),
                            u -> u.getId()));
                    break;
                default:
                    break;
            }
        }
        return userIds;
    }

    private void saveNotifies(Long ruleId, List<AlertRuleNotifyDO> notifies) {
        if (notifies == null || notifies.isEmpty()) return;
        notifies.forEach(n -> {
            n.setRuleId(ruleId);
            n.setCreator("admin");
            n.setUpdater("admin");
            n.setDeleted(0);
            alertRuleNotifyMapper.insert(n);
        });
    }
}
