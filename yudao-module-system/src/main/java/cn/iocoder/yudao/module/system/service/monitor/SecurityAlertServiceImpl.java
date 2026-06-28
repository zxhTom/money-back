package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertHandleReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.AlertRuleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.SecurityAlertMapper;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import cn.iocoder.yudao.module.system.event.SecurityAlertEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Collections;

@Service
@Slf4j
public class SecurityAlertServiceImpl implements SecurityAlertService {

    @Resource
    private SecurityAlertMapper securityAlertMapper;
    @Resource
    private NotifyMessageMapper notifyMessageMapper;
    @Resource
    private AlertRuleMapper alertRuleMapper;
    @Lazy
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Async
    public void saveAsync(SecurityAlertDO alert) {
        try {
            alert.setCreateTime(LocalDateTime.now());
            securityAlertMapper.insert(alert);
        } catch (Exception e) {
            log.error("[SecurityAlert] 告警写入失败", e);
        }
    }

    @Override
    @Async
    public void saveAsync(String alertType, int severity, String sourceIp, Long userId,
                          String requestUrl, String requestMethod, String suspiciousContent, String alertMessage) {
        SecurityAlertDO alert = new SecurityAlertDO();
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setSourceIp(sourceIp);
        alert.setUserId(userId);
        alert.setRequestUrl(requestUrl);
        alert.setRequestMethod(requestMethod);
        alert.setSuspiciousContent(truncate(suspiciousContent, 2000));
        alert.setAlertMessage(alertMessage);
        alert.setHandled(0);
        alert.setCreateTime(LocalDateTime.now());
        try {
            securityAlertMapper.insert(alert);
            dispatchNotify(alert);
        } catch (Exception e) {
            log.error("[SecurityAlert] 告警写入失败", e);
        }
    }

    private void dispatchNotify(SecurityAlertDO alert) {
        try {
            AlertRuleDO rule = alertRuleMapper.selectByAlertType(alert.getAlertType());
            if (rule == null || rule.getEnabled() == 0) return;
            String channels = rule.getNotifyChannels() != null ? rule.getNotifyChannels() : "";

            Set<Long> userIds = alertRuleService.resolveNotifyUserIds(rule.getId());
            if (userIds.isEmpty()) return;

            // 站内信
            if (channels.contains("IN_APP")) {
                String content = String.format("【%s】%s | IP: %s", alert.getAlertType(),
                        alert.getAlertMessage(), alert.getSourceIp() != null ? alert.getSourceIp() : "-");
                for (Long uid : userIds) {
                    NotifyMessageDO msg = new NotifyMessageDO();
                    msg.setUserId(uid);
                    msg.setUserType(2);
                    msg.setTemplateId(0L);
                    msg.setTemplateCode("SECURITY_ALERT");
                    msg.setTemplateType(3);
                    msg.setTemplateNickname("安全告警");
                    msg.setTemplateContent(content);
                    msg.setTemplateParams(Collections.emptyMap());
                    msg.setReadStatus(false);
                    notifyMessageMapper.insert(msg);
                }
            }

            // 公众号推送：发布事件由 mini 模块监听处理
            if (channels.contains("WECHAT_MP")) {
                eventPublisher.publishEvent(new SecurityAlertEvent(this, alert, rule.getId()));
            }
        } catch (Exception e) {
            log.warn("[SecurityAlert] 通知发送失败", e);
        }
    }

    @Override
    public PageResult<SecurityAlertDO> getPage(SecurityAlertPageReqVO reqVO) {
        return securityAlertMapper.selectPage(reqVO);
    }

    @Override
    public void handle(SecurityAlertHandleReqVO reqVO, Long operatorId) {
        securityAlertMapper.update(null, new LambdaUpdateWrapper<SecurityAlertDO>()
                .eq(SecurityAlertDO::getId, reqVO.getId())
                .set(SecurityAlertDO::getHandled, reqVO.getHandled())
                .set(SecurityAlertDO::getHandleBy, operatorId)
                .set(SecurityAlertDO::getHandleTime, LocalDateTime.now())
                .set(SecurityAlertDO::getHandleRemark, reqVO.getHandleRemark()));
    }

    @Override
    public long countTodayUnhandled() {
        return securityAlertMapper.selectCount(new LambdaQueryWrapper<SecurityAlertDO>()
                .eq(SecurityAlertDO::getHandled, 0)
                .ge(SecurityAlertDO::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay()));
    }

    @Override
    public Map<String, Long> getTodayAlertTypeStats() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<SecurityAlertDO> list = securityAlertMapper.selectList(
                new LambdaQueryWrapper<SecurityAlertDO>()
                        .select(SecurityAlertDO::getAlertType)
                        .ge(SecurityAlertDO::getCreateTime, since));
        Map<String, Long> counts = new LinkedHashMap<>();
        list.forEach(a -> counts.merge(a.getAlertType(), 1L, Long::sum));
        // 按数量降序排列
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public List<Map<String, Object>> getTopAttackIps() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<SecurityAlertDO> list = securityAlertMapper.selectList(
                new LambdaQueryWrapper<SecurityAlertDO>()
                        .select(SecurityAlertDO::getSourceIp)
                        .ge(SecurityAlertDO::getCreateTime, since)
                        .isNotNull(SecurityAlertDO::getSourceIp));
        Map<String, Long> counts = new LinkedHashMap<>();
        list.forEach(a -> counts.merge(a.getSourceIp(), 1L, Long::sum));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("sourceIp", e.getKey());
                    m.put("cnt", e.getValue());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Integer cleanSecurityAlert(Integer exceedDay, Integer deleteLimit) {
        int count = 0;
        LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = securityAlertMapper.deleteByCreateTimeLt(expireDate, deleteLimit);
            count += deleteCount;
            if (deleteCount < deleteLimit) {
                break;
            }
        }
        return count;
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max);
    }
}
