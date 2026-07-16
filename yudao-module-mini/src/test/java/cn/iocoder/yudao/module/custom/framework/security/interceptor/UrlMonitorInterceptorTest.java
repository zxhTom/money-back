package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlMonitorInterceptorTest {

    @Test
    public void testNormalize_stripsAdminApiPrefix() {
        // 带前缀与不带前缀归一化后相等 → 两种配置都能命中同一请求
        assertEquals(UrlMonitorInterceptor.normalize("/admin-api/custom/contract/get"),
                UrlMonitorInterceptor.normalize("/custom/contract/get"));
        assertEquals("/custom/contract/get",
                UrlMonitorInterceptor.normalize("/admin-api/custom/contract/get"));
    }

    @Test
    public void testNormalize_stripsQueryAndTrailingSlash() {
        assertEquals("/custom/contract/get",
                UrlMonitorInterceptor.normalize("/admin-api/custom/contract/get?id=1"));
        assertEquals("/custom/contract/get",
                UrlMonitorInterceptor.normalize("/custom/contract/get/"));
    }

    @Test
    public void testNormalize_blank() {
        assertEquals("", UrlMonitorInterceptor.normalize(null));
        assertEquals("", UrlMonitorInterceptor.normalize("  "));
    }
}
