package cn.iocoder.yudao.module.custom.service.security;

import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityPwdResetLogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.SecurityPwdResetLogMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityActionExecutorTest {

    @Mock
    private SecurityAlertService securityAlertService;
    @Mock
    private IpBlacklistService ipBlacklistService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private SecurityPwdResetLogMapper securityPwdResetLogMapper;

    @InjectMocks
    private SecurityActionExecutor executor;

    private AlertRuleDO rule(Integer autoBan, Integer autoDeleteUser) {
        AlertRuleDO r = new AlertRuleDO();
        r.setAutoBan(autoBan);
        r.setAutoDeleteUser(autoDeleteUser);
        r.setBanDurationSeconds(3600L);
        return r;
    }

    private AlertRuleDO ruleReset(Integer autoResetPassword) {
        AlertRuleDO r = new AlertRuleDO();
        r.setAutoResetPassword(autoResetPassword);
        return r;
    }

    @Test
    public void testAlwaysSavesAlert() {
        executor.execute(rule(0, 0), "SIMULATED_REQUEST", 3, "1.2.3.4", 9L, "/x", "GET", "d", "m");
        verify(securityAlertService).saveAsync(eq("SIMULATED_REQUEST"), eq(3), eq("1.2.3.4"), eq(9L),
                eq("/x"), eq("GET"), eq("d"), eq("m"));
    }

    @Test
    public void testAutoBanOnly() {
        executor.execute(rule(1, 0), "SIMULATED_REQUEST", 3, "1.2.3.4", 9L, "/x", "GET", "d", "m");
        verify(ipBlacklistService).addToBlacklist(eq("1.2.3.4"), anyString(), eq(true), any());
        verify(adminUserService, never()).deleteUserForSecurity(any());
    }

    @Test
    public void testAutoDeleteUserOnly() {
        executor.execute(rule(0, 1), "SIMULATED_REQUEST", 3, "1.2.3.4", 9L, "/x", "GET", "d", "m");
        verify(adminUserService).deleteUserForSecurity(9L);
        verify(ipBlacklistService, never()).addToBlacklist(anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    public void testBothActions() {
        executor.execute(rule(1, 1), "SIMULATED_REQUEST", 3, "1.2.3.4", 9L, "/x", "GET", "d", "m");
        verify(ipBlacklistService).addToBlacklist(eq("1.2.3.4"), anyString(), eq(true), any());
        verify(adminUserService).deleteUserForSecurity(9L);
    }

    @Test
    public void testDeleteUserSkippedWhenNoUserId() {
        executor.execute(rule(1, 1), "SIMULATED_REQUEST", 3, "1.2.3.4", null, "/x", "GET", "d", "m");
        verify(adminUserService, never()).deleteUserForSecurity(any());
    }

    @Test
    public void testAllowUserActionsFalse_forcesNoUserAction_evenIfRuleOn() {
        // 模拟请求场景：即便规则 auto_delete_user=1，也强制不动账号，只封IP
        executor.execute(rule(1, 1), "SIMULATED_REQUEST", 3, "1.2.3.4", 9L, "/x", "GET", "d", "m", false);
        verify(ipBlacklistService).addToBlacklist(eq("1.2.3.4"), anyString(), eq(true), any());
        verify(adminUserService, never()).deleteUserForSecurity(any());
    }

    @Test
    public void testAutoResetPassword_changesPwdAndRecords() {
        when(adminUserService.resetPasswordAndKickForSecurity(eq(9L), anyString())).thenReturn(true);
        executor.execute(ruleReset(1), "URL_MONITOR", 3, "1.2.3.4", 9L, "/x", "GET", "d", "m", true);
        verify(adminUserService).resetPasswordAndKickForSecurity(eq(9L), anyString());
        verify(securityPwdResetLogMapper).insert(any(SecurityPwdResetLogDO.class));
    }

    @Test
    public void testAutoResetPassword_skippedWhenNotAllowed() {
        executor.execute(ruleReset(1), "URL_MONITOR", 3, "1.2.3.4", 9L, "/x", "GET", "d", "m", false);
        verify(adminUserService, never()).resetPasswordAndKickForSecurity(any(), anyString());
    }

    @Test
    public void testAutoResetPassword_skippedWhenNoUserId() {
        executor.execute(ruleReset(1), "URL_MONITOR", 3, "1.2.3.4", null, "/x", "GET", "d", "m", true);
        verify(adminUserService, never()).resetPasswordAndKickForSecurity(any(), anyString());
    }
}
