package cn.iocoder.yudao.framework.common.util.servlet;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 {@link ServletUtils#getClientRealIp} 的可信取法：优先 X-Real-IP、其次 XFF 最右一跳，
 * 重点是最左 XFF 的伪造内网IP 不能被采信。
 */
public class ServletUtilsClientIpTest {

    /** 用动态代理造一个只关心 getHeader/getRemoteAddr 的假 request，避免引入 Mockito */
    private HttpServletRequest req(String realIp, String xff, String remoteAddr) {
        Map<String, String> headers = new HashMap<>();
        if (realIp != null) headers.put("X-Real-IP", realIp);
        if (xff != null) headers.put("X-Forwarded-For", xff);
        return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getHeader":
                            return headers.get((String) args[0]);
                        case "getRemoteAddr":
                            return remoteAddr;
                        default:
                            Class<?> rt = method.getReturnType();
                            if (rt.equals(boolean.class)) return false;
                            if (rt.isPrimitive()) return 0;
                            return null;
                    }
                });
    }

    @Test
    public void testPrefersXRealIp() {
        assertEquals("203.0.113.7",
                ServletUtils.getClientRealIp(req("203.0.113.7", "192.168.100.1, 203.0.113.7", "192.168.100.1")));
    }

    @Test
    public void testSpoofedLeftmostInternalIsIgnored_takesRightmost() {
        // 攻击者伪造 X-Forwarded-For: 192.168.100.1，nginx 追加真实IP在最右
        assertEquals("203.0.113.7",
                ServletUtils.getClientRealIp(req(null, "192.168.100.1, 203.0.113.7", "192.168.100.1")));
    }

    @Test
    public void testXffSingleValue() {
        assertEquals("203.0.113.7",
                ServletUtils.getClientRealIp(req(null, "203.0.113.7", "192.168.100.1")));
    }

    @Test
    public void testXffTrailingUnknownSkipped() {
        assertEquals("203.0.113.7",
                ServletUtils.getClientRealIp(req(null, "203.0.113.7, unknown", "192.168.100.1")));
    }

    @Test
    public void testFallbackToRemoteAddr() {
        assertEquals("192.168.100.1",
                ServletUtils.getClientRealIp(req(null, null, "192.168.100.1")));
    }

    @Test
    public void testGenuineInternalClientRecordedAsInternal() {
        // 真正来自内网（健康检查等）：X-Real-IP 就是内网地址，如实记录
        assertEquals("192.168.100.1",
                ServletUtils.getClientRealIp(req("192.168.100.1", null, "192.168.100.1")));
    }

    @Test
    public void testNullRequest() {
        assertNull(ServletUtils.getClientRealIp(null));
    }

    @Test
    public void testIsInternalIp() {
        assertTrue(ServletUtils.isInternalIp("192.168.100.1"));
        assertTrue(ServletUtils.isInternalIp("10.0.0.5"));
        assertTrue(ServletUtils.isInternalIp("172.16.0.1"));
        assertTrue(ServletUtils.isInternalIp("172.31.255.255"));
        assertTrue(ServletUtils.isInternalIp("127.0.0.1"));
        assertTrue(ServletUtils.isInternalIp("::1"));
        assertFalse(ServletUtils.isInternalIp("203.0.113.7"));
        assertFalse(ServletUtils.isInternalIp("172.32.0.1")); // 超出 16-31 段
        assertFalse(ServletUtils.isInternalIp("8.8.8.8"));
        assertFalse(ServletUtils.isInternalIp(""));
        assertFalse(ServletUtils.isInternalIp(null));
    }
}
