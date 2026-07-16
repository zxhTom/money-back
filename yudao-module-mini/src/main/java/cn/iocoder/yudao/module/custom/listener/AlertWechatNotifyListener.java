package cn.iocoder.yudao.module.custom.listener;

import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import cn.iocoder.yudao.module.system.event.SecurityAlertEvent;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 安全告警公众号推送监听器
 * 监听 SecurityAlertEvent，向配置的通知人员推送微信公众号模板消息
 */
@Slf4j
@Component
public class AlertWechatNotifyListener {

    @Value("${wx.mp.template.security-alert-id:}")
    private String securityAlertTemplateId;

    @Resource
    private WxMpService wxMpService;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private CustomDefineMapper customDefineMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Async
    @EventListener
    public void onAlert(SecurityAlertEvent event) {
        if (!StringUtils.hasText(securityAlertTemplateId)) {
            log.debug("[AlertWechat] 未配置 wx.mp.template.security-alert-id，跳过公众号推送");
            return;
        }

        SecurityAlertDO alert = event.getAlert();
        Set<Long> userIds = alertRuleService.resolveNotifyUserIds(event.getRuleId());
        if (userIds.isEmpty()) return;

        String alertTypeName = resolveTypeName(alert.getAlertType());
        String timeStr = alert.getCreateTime() != null ? alert.getCreateTime().format(FMT) : "";
        String ip = alert.getSourceIp() != null ? alert.getSourceIp() : "-";

        for (Long userId : userIds) {
            try {
                String openId = customDefineMapper.selectOffcialOpenIdByUserId(userId);
                if (!StringUtils.hasText(openId)) continue;

                String alertId = alert.getId() != null ? String.valueOf(alert.getId()) : "-";
                WxMpTemplateMessage msg = WxMpTemplateMessage.builder()
                        .toUser(openId)
                        .templateId(securityAlertTemplateId)
                        .build();
                msg.addData(new WxMpTemplateData("thing2", alertTypeName));
                msg.addData(new WxMpTemplateData("thing3", alertId));
                msg.addData(new WxMpTemplateData("thing6", ip));
                msg.addData(new WxMpTemplateData("time8", timeStr));

                wxMpService.getTemplateMsgService().sendTemplateMsg(msg);
                log.info("[AlertWechat] 告警推送成功 userId={} type={}", userId, alert.getAlertType());
            } catch (Exception e) {
                log.warn("[AlertWechat] 告警推送失败 userId={} error={}", userId, e.getMessage());
            }
        }
    }

    private String resolveTypeName(String type) {
        if (type == null) return "未知";
        switch (type) {
            case "BRUTE_FORCE":   return "暴力破解";
            case "SQL_INJECTION": return "SQL 注入";
            case "XSS":           return "XSS 攻击";
            case "DATA_LEAKAGE":  return "数据泄露";
            case "IP_RISK":       return "IP 风险";
            case "REGISTER_ABUSE": return "注册频率风控";
            case "FACE_AUTH_UNCOMPLETED": return "人脸未认证";
            case "SIMULATED_REQUEST": return "模拟/伪造请求";
            case "URL_MONITOR": return "URL访问监控";
            case "IP_MULTI_USER": return "多夫多妻(一IP多用户)";
            case "USER_MULTI_IP": return "一用户多IP";
            default:              return type;
        }
    }
}
