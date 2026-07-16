package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 多登录风控（在登录/刷新签发令牌后调用）：
 *   1. 【IP_MULTI_USER 多夫多妻】同一 IP 同时登录的不同用户数超过阈值(默认3) → 封该 IP + 剔除相关用户(不改密)；
 *   3. 【USER_MULTI_IP】同一用户同时在线的不同 IP 数超过阈值(默认3) → 立即封该用户的所有 IP。
 * 白名单 IP 完全跳过（登录 IP 在白名单直接不检；封禁也不会封到白名单）。
 */
@Component
@Slf4j
public class MultiLoginRiskChecker {

    private static final String TYPE_IP_MULTI_USER = "IP_MULTI_USER";
    private static final String TYPE_USER_MULTI_IP = "USER_MULTI_IP";
    private static final int DEFAULT_THRESHOLD = 3;
    private static final long DEFAULT_BAN_SECONDS = 86400;
    private static final int DEFAULT_SEVERITY = 3;

    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private IpBlacklistService ipBlacklistService;
    @Resource
    private IpWhitelistService ipWhitelistService;
    @Resource
    @Lazy // 会回调 removeAllTokensByUserId，懒加载避免循环依赖
    private OAuth2TokenService oauth2TokenService;

    public void check(Long userId, String ip) {
        if (ip == null || ip.isEmpty() || ipWhitelistService.isWhitelisted(ip)) {
            return; // 白名单/无IP 直接放过
        }
        checkIpMultiUser(ip);
        checkUserMultiIp(userId);
    }

    /** 一IP多用户：封IP + 剔除该IP上的相关用户（不改密） */
    private void checkIpMultiUser(String ip) {
        AlertRuleDO rule = alertRuleService.getCachedRule(TYPE_IP_MULTI_USER);
        if (isDisabled(rule)) {
            return;
        }
        int threshold = threshold(rule);
        List<Long> userIds = oauth2AccessTokenMapper.selectDistinctUserIdsByIp(ip);
        if (userIds.size() <= threshold) {
            return;
        }
        String message = "同一IP[" + ip + "]同时登录 " + userIds.size() + " 个用户，超过阈值 " + threshold;
        log.warn("[MultiLogin][IP_MULTI_USER] {} → 封IP并剔除相关用户 users={}", message, userIds);
        securityAlertService.saveAsync(TYPE_IP_MULTI_USER, severity(rule), ip, null, "/login", "LOGIN", userIds.toString(), message);
        ipBlacklistService.addToBlacklist(ip, "自动封禁：" + message, true, LocalDateTime.now().plusSeconds(banSeconds(rule)));
        for (Long uid : userIds) {
            try {
                oauth2TokenService.removeAllTokensByUserId(uid, UserTypeEnum.ADMIN.getValue()); // 踢下线，不改密
            } catch (Exception e) {
                log.warn("[MultiLogin][IP_MULTI_USER] 剔除用户 {} 失败: {}", uid, e.getMessage());
            }
        }
    }

    /** 一用户多IP：立即封该用户的所有（非白名单）IP */
    private void checkUserMultiIp(Long userId) {
        if (userId == null) {
            return;
        }
        AlertRuleDO rule = alertRuleService.getCachedRule(TYPE_USER_MULTI_IP);
        if (isDisabled(rule)) {
            return;
        }
        int threshold = threshold(rule);
        List<String> ips = oauth2AccessTokenMapper.selectDistinctIpsByUserId(userId);
        // 白名单 IP 不计数、不封禁
        List<String> banIps = ips.stream().filter(i -> !ipWhitelistService.isWhitelisted(i))
                .collect(java.util.stream.Collectors.toList());
        if (banIps.size() <= threshold) {
            return;
        }
        String message = "用户[" + userId + "]同时在线 " + banIps.size() + " 个IP，超过阈值 " + threshold;
        log.warn("[MultiLogin][USER_MULTI_IP] {} → 封所有IP ips={}", message, banIps);
        securityAlertService.saveAsync(TYPE_USER_MULTI_IP, severity(rule), null, userId, "/login", "LOGIN", banIps.toString(), message);
        for (String ip : banIps) {
            try {
                ipBlacklistService.addToBlacklist(ip, "自动封禁：" + message, true, LocalDateTime.now().plusSeconds(banSeconds(rule)));
            } catch (Exception e) {
                log.warn("[MultiLogin][USER_MULTI_IP] 封禁IP {} 失败: {}", ip, e.getMessage());
            }
        }
        // 一并踢下线该用户，彻底断开
        try {
            oauth2TokenService.removeAllTokensByUserId(userId, UserTypeEnum.ADMIN.getValue());
        } catch (Exception ignored) {
        }
    }

    private boolean isDisabled(AlertRuleDO rule) {
        return rule != null && rule.getEnabled() != null && rule.getEnabled() == 0;
    }

    private int threshold(AlertRuleDO rule) {
        return rule != null && rule.getThreshold() != null ? rule.getThreshold() : DEFAULT_THRESHOLD;
    }

    private int severity(AlertRuleDO rule) {
        return rule != null && rule.getSeverity() != null ? rule.getSeverity() : DEFAULT_SEVERITY;
    }

    private long banSeconds(AlertRuleDO rule) {
        return rule != null && rule.getBanDurationSeconds() != null ? rule.getBanDurationSeconds() : DEFAULT_BAN_SECONDS;
    }
}
