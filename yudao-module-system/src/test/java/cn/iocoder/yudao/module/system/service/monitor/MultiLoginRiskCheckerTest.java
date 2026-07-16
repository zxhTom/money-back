package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MultiLoginRiskCheckerTest {

    @Mock private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Mock private AlertRuleService alertRuleService;
    @Mock private SecurityAlertService securityAlertService;
    @Mock private IpBlacklistService ipBlacklistService;
    @Mock private IpWhitelistService ipWhitelistService;
    @Mock private OAuth2TokenService oauth2TokenService;

    @InjectMocks private MultiLoginRiskChecker checker;

    private AlertRuleDO rule() {
        AlertRuleDO r = new AlertRuleDO();
        r.setEnabled(1);
        r.setThreshold(3);
        r.setSeverity(3);
        r.setBanDurationSeconds(3600L);
        return r;
    }

    @Test
    public void testWhitelistedLoginIp_skipsEverything() {
        when(ipWhitelistService.isWhitelisted("1.2.3.4")).thenReturn(true);
        checker.check(9L, "1.2.3.4");
        verifyNoInteractions(oauth2AccessTokenMapper, ipBlacklistService, securityAlertService);
    }

    @Test
    public void testIpMultiUser_overThreshold_bansIpAndKicksUsers() {
        when(ipWhitelistService.isWhitelisted("1.2.3.4")).thenReturn(false);
        when(alertRuleService.getCachedRule("IP_MULTI_USER")).thenReturn(rule());
        when(alertRuleService.getCachedRule("USER_MULTI_IP")).thenReturn(rule());
        // 该IP上有4个用户 > 阈值3
        when(oauth2AccessTokenMapper.selectDistinctUserIdsByIp("1.2.3.4"))
                .thenReturn(Arrays.asList(1L, 2L, 3L, 9L));
        // 该用户只有1个IP，不触发 USER_MULTI_IP
        when(oauth2AccessTokenMapper.selectDistinctIpsByUserId(9L))
                .thenReturn(Collections.singletonList("1.2.3.4"));

        checker.check(9L, "1.2.3.4");

        verify(ipBlacklistService).addToBlacklist(eq("1.2.3.4"), anyString(), eq(true), any());
        verify(oauth2TokenService).removeAllTokensByUserId(eq(1L), anyInt());
        verify(oauth2TokenService).removeAllTokensByUserId(eq(2L), anyInt());
        verify(oauth2TokenService).removeAllTokensByUserId(eq(3L), anyInt());
        verify(oauth2TokenService).removeAllTokensByUserId(eq(9L), anyInt());
    }

    @Test
    public void testIpMultiUser_underThreshold_noAction() {
        when(ipWhitelistService.isWhitelisted("1.2.3.4")).thenReturn(false);
        when(alertRuleService.getCachedRule("IP_MULTI_USER")).thenReturn(rule());
        when(alertRuleService.getCachedRule("USER_MULTI_IP")).thenReturn(rule());
        when(oauth2AccessTokenMapper.selectDistinctUserIdsByIp("1.2.3.4"))
                .thenReturn(Arrays.asList(1L, 2L, 9L)); // 3个，不超阈值
        when(oauth2AccessTokenMapper.selectDistinctIpsByUserId(9L))
                .thenReturn(Collections.singletonList("1.2.3.4"));

        checker.check(9L, "1.2.3.4");

        verify(ipBlacklistService, never()).addToBlacklist(anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    public void testUserMultiIp_overThreshold_bansAllIps_excludingWhitelist() {
        when(ipWhitelistService.isWhitelisted("1.2.3.4")).thenReturn(false);
        when(alertRuleService.getCachedRule("IP_MULTI_USER")).thenReturn(rule());
        when(alertRuleService.getCachedRule("USER_MULTI_IP")).thenReturn(rule());
        when(oauth2AccessTokenMapper.selectDistinctUserIdsByIp("1.2.3.4"))
                .thenReturn(Collections.singletonList(9L)); // 不触发 IP_MULTI_USER
        // 用户在线5个IP，其中 9.9.9.9 是白名单 → 排除后4个 > 阈值3
        when(oauth2AccessTokenMapper.selectDistinctIpsByUserId(9L))
                .thenReturn(Arrays.asList("1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4", "9.9.9.9"));
        when(ipWhitelistService.isWhitelisted("9.9.9.9")).thenReturn(true);
        when(ipWhitelistService.isWhitelisted("1.1.1.1")).thenReturn(false);
        when(ipWhitelistService.isWhitelisted("2.2.2.2")).thenReturn(false);
        when(ipWhitelistService.isWhitelisted("3.3.3.3")).thenReturn(false);
        when(ipWhitelistService.isWhitelisted("4.4.4.4")).thenReturn(false);

        checker.check(9L, "1.2.3.4");

        verify(ipBlacklistService).addToBlacklist(eq("1.1.1.1"), anyString(), eq(true), any());
        verify(ipBlacklistService).addToBlacklist(eq("4.4.4.4"), anyString(), eq(true), any());
        verify(ipBlacklistService, never()).addToBlacklist(eq("9.9.9.9"), anyString(), anyBoolean(), any());
        verify(oauth2TokenService).removeAllTokensByUserId(eq(9L), anyInt());
    }
}
