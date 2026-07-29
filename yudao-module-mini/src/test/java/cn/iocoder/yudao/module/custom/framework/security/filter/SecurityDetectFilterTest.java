package cn.iocoder.yudao.module.custom.framework.security.filter;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.IpRiskCheckService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityDetectFilterTest {

    @Mock private IpBlacklistService ipBlacklistService;
    @Mock private SecurityAlertService securityAlertService;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private IpRiskCheckService ipRiskCheckService;
    @Mock private AlertRuleService alertRuleService;
    @Mock private FilterChain filterChain;

    private SecurityDetectFilter newFilter() {
        return new SecurityDetectFilter(ipBlacklistService, securityAlertService,
                stringRedisTemplate, ipRiskCheckService, alertRuleService, null);
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRemoteAddr("1.2.3.4");
        return req;
    }

    // ── IP 黑名单：通用 vs 暴露原因 ──────────────────────────────
    @Test
    public void testBlacklist_exposeOff_genericMessage() throws Exception {
        when(ipBlacklistService.isBlacklisted("1.2.3.4")).thenReturn(true);
        when(alertRuleService.getCachedRule("IP_BLACKLIST_BLOCK")).thenReturn(rule(false));

        MockHttpServletResponse response = new MockHttpServletResponse();
        newFilter().doFilter(request("/custom/contract/get"), response, filterChain);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("{\"code\":10030,\"msg\":\"访问被拒绝\",\"data\":null}", response.getContentAsString());
        verify(ipBlacklistService, never()).getActiveEntry(any());
        verifyNoInteractions(filterChain);
    }

    @Test
    public void testBlacklist_exposeOn_detailedMessageWithExpireTime() throws Exception {
        when(ipBlacklistService.isBlacklisted("1.2.3.4")).thenReturn(true);
        when(alertRuleService.getCachedRule("IP_BLACKLIST_BLOCK")).thenReturn(rule(true));
        IpBlacklistDO entry = new IpBlacklistDO();
        entry.setReason("自动封禁：暴力破解检测");
        entry.setExpireTime(java.time.LocalDateTime.of(2026, 8, 1, 12, 0, 0));
        when(ipBlacklistService.getActiveEntry("1.2.3.4")).thenReturn(entry);

        MockHttpServletResponse response = new MockHttpServletResponse();
        newFilter().doFilter(request("/custom/contract/get"), response, filterChain);

        assertEquals("{\"code\":10030,\"msg\":\"您的IP因【自动封禁：暴力破解检测】被限制访问，解封时间：2026-08-01 12:00:00\",\"data\":null}",
                response.getContentAsString());
    }

    // ── 暴力破解 ────────────────────────────────────────────────
    @Test
    public void testBruteForce_exposeOn_detailedMessage() throws Exception {
        when(ipBlacklistService.isBlacklisted("1.2.3.4")).thenReturn(false);
        when(alertRuleService.getCachedRule("BRUTE_FORCE")).thenReturn(ruleWithThreshold(true, 10, 300));
        lenientRedisIncrement(11L);

        MockHttpServletResponse response = new MockHttpServletResponse();
        newFilter().doFilter(request("/system/auth/login"), response, filterChain);

        assertEquals("{\"code\":10028,\"msg\":\"300秒内请求次数达 11 次，已超过阈值 10，请稍后再试\",\"data\":null}",
                response.getContentAsString());
    }

    @Test
    public void testBruteForce_exposeOff_genericMessage() throws Exception {
        when(ipBlacklistService.isBlacklisted("1.2.3.4")).thenReturn(false);
        when(alertRuleService.getCachedRule("BRUTE_FORCE")).thenReturn(ruleWithThreshold(false, 10, 300));
        lenientRedisIncrement(11L);

        MockHttpServletResponse response = new MockHttpServletResponse();
        newFilter().doFilter(request("/system/auth/login"), response, filterChain);

        assertEquals("{\"code\":10028,\"msg\":\"请求过于频繁，请稍后再试\",\"data\":null}", response.getContentAsString());
    }

    // ── SQL 注入：安全红线，暴露开关打开也不回显攻击特征 ──────────
    @Test
    public void testSqlInjection_exposeOn_stillSafeMessage_noPayloadEcho() throws Exception {
        when(ipBlacklistService.isBlacklisted("1.2.3.4")).thenReturn(false);
        when(alertRuleService.getCachedRule("SQL_INJECTION")).thenReturn(rule(true));
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(any())).thenReturn(1L);

        MockHttpServletRequest req = request("/custom/contract/page");
        req.addParameter("indebtedName", "' UNION SELECT password FROM sys_user--");
        MockHttpServletResponse response = new MockHttpServletResponse();
        newFilter().doFilter(req, response, filterChain);

        String body = response.getContentAsString();
        assertEquals("{\"code\":10029,\"msg\":\"检测到您的请求包含可疑内容，已被拦截\",\"data\":null}", body);
        assertFalse(body.contains("UNION"), "开关打开也绝不能回显命中的攻击特征");
        assertFalse(body.contains("sys_user"), "开关打开也绝不能回显命中的攻击特征");
    }

    @Test
    public void testSqlInjection_exposeOff_genericMessage() throws Exception {
        when(ipBlacklistService.isBlacklisted("1.2.3.4")).thenReturn(false);
        when(alertRuleService.getCachedRule("SQL_INJECTION")).thenReturn(rule(false));
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(any())).thenReturn(1L);

        MockHttpServletRequest req = request("/custom/contract/page");
        req.addParameter("indebtedName", "' UNION SELECT password FROM sys_user--");
        MockHttpServletResponse response = new MockHttpServletResponse();
        newFilter().doFilter(req, response, filterChain);

        assertEquals("{\"code\":10029,\"msg\":\"请求被拦截\",\"data\":null}", response.getContentAsString());
    }

    private AlertRuleDO rule(boolean exposeReason) {
        AlertRuleDO r = new AlertRuleDO();
        r.setExposeReason(exposeReason ? 1 : 0);
        // handleSqlInjection 里 "rule.getSeverity()"/"rule.getAutoBan()" 拆箱要求非 null，必须显式设置避免 NPE
        r.setSeverity(2);
        r.setAutoBan(0);
        return r;
    }

    private AlertRuleDO ruleWithThreshold(boolean exposeReason, int threshold, int windowSeconds) {
        AlertRuleDO r = rule(exposeReason);
        // checkBruteForce 里有 "rule.getEnabled() == 0" 这行现存代码，Integer 拆箱要求非 null，
        // 这里必须显式设成 1，否则会在这一行 NPE（handleSqlInjection 没有这个判断，rule() 那边不需要设）
        r.setEnabled(1);
        r.setThreshold(threshold);
        r.setWindowSeconds(windowSeconds);
        r.setAutoBan(0);
        r.setSeverity(2);
        return r;
    }

    private void lenientRedisIncrement(long count) {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(any())).thenReturn(count);
    }
}
