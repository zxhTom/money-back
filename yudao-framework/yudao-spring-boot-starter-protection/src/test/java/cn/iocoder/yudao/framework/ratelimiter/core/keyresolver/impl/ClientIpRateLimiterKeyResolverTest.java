package cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.aop.RateLimiterAspect;
import cn.iocoder.yudao.framework.ratelimiter.core.redis.RateLimiterRedisDAO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientIpRateLimiterKeyResolverTest {
    private ClientIpRateLimiterKeyResolver resolver;
    private HttpServletRequest request;
    private JoinPoint point;
    private Signature signature;
    @BeforeEach void setup() {
        resolver=new ClientIpRateLimiterKeyResolver();request=mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("198.51.100.10");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        point=mock(JoinPoint.class);signature=mock(Signature.class);
        when(point.getSignature()).thenReturn(signature);when(signature.toString()).thenReturn("login(String,String)");
    }
    @AfterEach void cleanup(){RequestContextHolder.resetRequestAttributes();}
    private String key(){return resolver.resolver(point,null);}

    @Test void changedPasswordAndUsernameShareTheSameIpBudget() {
        when(point.getArgs()).thenReturn(new Object[]{"user-a","synthetic-password-a"});String first=key();
        when(point.getArgs()).thenReturn(new Object[]{"user-a","synthetic-password-b"});assertEquals(first,key());
        when(point.getArgs()).thenReturn(new Object[]{"user-b","synthetic-password-c"});assertEquals(first,key());
        verify(point,never()).getArgs();
    }
    @Test void forwardedHeaderSpoofingCannotChangeTheCounter() {
        String first=key();when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.2, 198.51.100.10");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("203.0.113.3");assertEquals(first,key());
    }
    @Test void distinctRealClientsHaveIndependentCounters() {
        String first=key();when(request.getHeader("X-Real-IP")).thenReturn("198.51.100.11");assertNotEquals(first,key());
    }
    @Test void distinctEndpointsHaveIndependentCounters() {
        String first=key();when(signature.toString()).thenReturn("register(String,String)");assertNotEquals(first,key());
    }
    @Test void forwardedFallbackUsesLastHopInsteadOfUserSuppliedPrefix() {
        when(request.getHeader("X-Real-IP")).thenReturn(null);String first=key();
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.2, 203.0.113.3, 198.51.100.10");assertEquals(first,key());
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.2, 198.51.100.11");assertNotEquals(first,key());
    }
    @Test void requestsWithoutProxyHeadersUseDirectPeer() {
        when(request.getHeader("X-Real-IP")).thenReturn(null);when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        String first=key();when(request.getRemoteAddr()).thenReturn("198.51.100.11");assertNotEquals(first,key());
    }
    @Test void ipv6RealClientsKeepStableCounters() {
        when(request.getHeader("X-Real-IP")).thenReturn("2001:db8::1");String first=key();
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.2, 2001:db8::1");assertEquals(first,key());
        when(request.getHeader("X-Real-IP")).thenReturn("2001:db8::2");assertNotEquals(first,key());
    }
    @Test void missingHttpRequestDoesNotBreakBackgroundInvocation() {
        RequestContextHolder.resetRequestAttributes();assertNotNull(key());assertEquals(key(),key());
    }
    @Test void aspectEnforcesSharedCounterWithoutLoggingPasswordArguments() {
        RateLimiter rule=mock(RateLimiter.class);doReturn(ClientIpRateLimiterKeyResolver.class).when(rule).keyResolver();
        when(rule.count()).thenReturn(2);when(rule.time()).thenReturn(60);when(rule.timeUnit()).thenReturn(TimeUnit.SECONDS);
        when(rule.message()).thenReturn("请求过于频繁，请稍后再试");
        RateLimiterRedisDAO redis=mock(RateLimiterRedisDAO.class);Map<String,Integer> counters=new HashMap<>();
        when(redis.tryAcquire(anyString(),eq(2),eq(60),eq(TimeUnit.SECONDS))).thenAnswer(i -> counters.merge(i.getArgument(0),1,Integer::sum)<=2);
        RateLimiterAspect aspect=new RateLimiterAspect(Collections.singletonList(resolver),redis);
        when(point.getArgs()).thenReturn(new Object[]{"user","synthetic-a"});assertDoesNotThrow(() -> aspect.beforePointCut(point,rule));
        when(point.getArgs()).thenReturn(new Object[]{"user","synthetic-b"});assertDoesNotThrow(() -> aspect.beforePointCut(point,rule));
        when(point.getArgs()).thenReturn(new Object[]{"other-user","synthetic-c"});
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.2, 198.51.100.10");
        ServiceException exception=assertThrows(ServiceException.class,() -> aspect.beforePointCut(point,rule));
        assertEquals(429,exception.getCode());assertEquals("请求过于频繁，请稍后再试",exception.getMessage());
        assertEquals(1,counters.size());verify(point,never()).getArgs();
    }
}
