package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.module.system.service.monitor.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.framework.security.aop.DataLeakageMonitorAspect;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.*;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.*;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.event.SecurityAlertEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.*;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WhitelistAlertThrottleTest {
    private static final String WHITE="192.0.2.10";
    private SecurityAlertServiceImpl service;
    private SecurityAlertMapper alerts;
    private NotifyMessageMapper messages;
    private ApplicationEventPublisher events;
    private IpWhitelistService whitelist;
    private StringRedisTemplate redis;
    private ValueOperations<String,String> values;
    private final ConcurrentHashMap<String,Long> expires=new ConcurrentHashMap<>();
    private final AtomicLong clock=new AtomicLong(0);

    @BeforeEach void setup() throws Exception {
        service=new SecurityAlertServiceImpl();alerts=mock(SecurityAlertMapper.class);messages=mock(NotifyMessageMapper.class);
        events=mock(ApplicationEventPublisher.class);whitelist=mock(IpWhitelistService.class);redis=mock(StringRedisTemplate.class);
        values=mock(ValueOperations.class);when(redis.opsForValue()).thenReturn(values);
        when(whitelist.isWhitelisted(anyString())).thenAnswer(i -> ((String)i.getArgument(0)).startsWith("192.0.2."));
        when(values.setIfAbsent(anyString(),eq("1"),eq(24L),eq(TimeUnit.HOURS))).thenAnswer(i -> {
            AtomicBoolean acquired=new AtomicBoolean();
            expires.compute(i.getArgument(0),(key,expiry) -> {
                if(expiry==null || expiry<=clock.get()) {acquired.set(true);return clock.get()+TimeUnit.HOURS.toMillis(24);}
                return expiry;
            });return acquired.get();
        });
        AlertRuleMapper rules=mock(AlertRuleMapper.class);AlertRuleService ruleService=mock(AlertRuleService.class);
        when(rules.selectByAlertType(anyString())).thenReturn(new AlertRuleDO().setId(1L).setEnabled(1).setNotifyChannels("IN_APP,WECHAT_MP"));
        when(ruleService.resolveNotifyUserIds(1L)).thenReturn(Collections.singleton(7L));
        inject(service,"securityAlertMapper",alerts);inject(service,"notifyMessageMapper",messages);
        inject(service,"eventPublisher",events);inject(service,"alertRuleMapper",rules);inject(service,"alertRuleService",ruleService);
        inject(service,"ipWhitelistService",whitelist);inject(service,"stringRedisTemplate",redis);
    }
    @AfterEach void cleanup() {RequestContextHolder.resetRequestAttributes();SecurityContextHolder.clearContext();}
    private static void inject(Object target,String name,Object value) throws Exception {
        Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);
    }
    private void full(String type,String ip,long uid) {service.saveAsync(type,2,ip,uid,"/synthetic-read","GET",null,"test-only");}
    private void record(String ip) {service.saveAsync(new SecurityAlertDO().setAlertType("DATA_LEAKAGE").setSourceIp(ip).setUserId(99L));}

    @Test void whitelistedIpHasOneAlertAndOneNotificationAcrossTypesAndAccounts() {
        full("URL_MONITOR",WHITE,1);full("DATA_LEAKAGE",WHITE,2);full("IP_RISK",WHITE,3);
        verify(alerts).insert(any(SecurityAlertDO.class));verify(messages).insert(any(NotifyMessageDO.class));verify(events).publishEvent(any(SecurityAlertEvent.class));
    }
    @Test void nonWhitelistedIpsKeepExistingAlertFrequency() {
        full("IP_RISK","198.51.100.10",1);full("IP_RISK","198.51.100.10",1);
        verify(alerts,times(2)).insert(any(SecurityAlertDO.class));verify(messages,times(2)).insert(any(NotifyMessageDO.class));verifyNoInteractions(redis);
    }
    @Test void differentWhitelistedIpsHaveIndependentAllowances() {
        full("XSS",WHITE,1);full("XSS","192.0.2.11",1);full("XSS",WHITE,1);
        verify(alerts,times(2)).insert(any(SecurityAlertDO.class));assertEquals(2,expires.size());
    }
    @Test void recordOnlyAndNotifyingOverloadsShareOneAllowance() {
        record(WHITE);full("XSS",WHITE,1);verify(alerts).insert(any(SecurityAlertDO.class));verifyNoInteractions(messages,events);
    }
    @Test void notifyingOverloadAlsoSuppressesSubsequentRecordOnlyAlerts() {
        full("XSS",WHITE,1);record(WHITE);verify(alerts).insert(any(SecurityAlertDO.class));verify(messages).insert(any(NotifyMessageDO.class));
    }
    @Test void allowanceExpiresAfter24HoursAndRepeatsDoNotExtendIt() {
        full("XSS",WHITE,1);clock.set(TimeUnit.HOURS.toMillis(23));full("SQL_INJECTION",WHITE,2);
        clock.set(TimeUnit.HOURS.toMillis(24)-1);full("URL_MONITOR",WHITE,3);verify(alerts).insert(any(SecurityAlertDO.class));
        clock.incrementAndGet();full("IP_RISK",WHITE,4);verify(alerts,times(2)).insert(any(SecurityAlertDO.class));
        verify(redis,never()).expire(anyString(),anyLong(),any(TimeUnit.class));
    }
    @Test void concurrentEventsForSameIpProduceOnlyOneAlert() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(8);CountDownLatch start=new CountDownLatch(1);List<Future<?>> tasks=new ArrayList<>();
        try {
            for(int i=0;i<40;i++) tasks.add(pool.submit(() -> {try {start.await();}catch(InterruptedException ex){throw new RuntimeException(ex);}full("XSS",WHITE,1);}));
            start.countDown();for(Future<?> task:tasks)task.get(10,TimeUnit.SECONDS);
            verify(alerts).insert(any(SecurityAlertDO.class));verify(messages).insert(any(NotifyMessageDO.class));
        } finally {pool.shutdownNow();}
    }
    @Test void removingWhitelistRestoresNormalAlertingImmediatelyAfterMatcherRefresh() {
        full("XSS",WHITE,1);when(whitelist.isWhitelisted(WHITE)).thenReturn(false);
        full("XSS",WHITE,1);verify(alerts,times(2)).insert(any(SecurityAlertDO.class));
    }
    @Test void surroundingWhitespaceDoesNotCreateAnotherAllowance() {
        full("XSS",WHITE,1);full("XSS","  "+WHITE+"  ",1);verify(alerts).insert(any(SecurityAlertDO.class));
    }
    @Test void unknownOrMissingSourceIsNotSilentlyTreatedAsWhitelisted() {
        full("FACE_AUTH_UNCOMPLETED",null,1);full("DATA_LEAKAGE","",1);full("DATA_LEAKAGE"," ",1);
        verify(alerts,times(3)).insert(any(SecurityAlertDO.class));verifyNoInteractions(redis,whitelist);
    }
    @Test void uncertainRedisReservationDoesNotCreateDuplicateTrustedIpAlert() {
        when(values.setIfAbsent(anyString(),eq("1"),eq(24L),eq(TimeUnit.HOURS))).thenReturn(null);
        full("XSS",WHITE,1);verifyNoInteractions(alerts,messages,events);
    }
    @Test void redisOutageCannotTurnTrustedIpNotificationsIntoFloodAndDoesNotSuppressOtherIps() {
        when(values.setIfAbsent(anyString(),eq("1"),eq(24L),eq(TimeUnit.HOURS))).thenThrow(new IllegalStateException("synthetic redis failure"));
        full("XSS",WHITE,1);full("XSS","198.51.100.10",1);verify(alerts).insert(any(SecurityAlertDO.class));
    }
    @Test void pageQueryAspectCapturesTrustedIpBeforeAsynchronousAlerting() throws Throwable {
        HttpServletRequest request=mock(HttpServletRequest.class);when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn(WHITE);when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.99, "+WHITE);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(42L).setUserType(2),request);
        DataLeakageMonitorAspect aspect=new DataLeakageMonitorAspect();inject(aspect,"securityAlertService",service);inject(aspect,"stringRedisTemplate",redis);
        ProceedingJoinPoint point=mock(ProceedingJoinPoint.class);Signature signature=mock(Signature.class);
        when(point.getSignature()).thenReturn(signature);when(signature.toShortString()).thenReturn("syntheticPage()");
        PageResult<Long> page=new PageResult<>(Collections.nCopies(501,1L),501L);when(point.proceed()).thenReturn(page);
        when(values.increment(anyString(),eq(501L))).thenReturn(1001L);
        assertSame(page,aspect.monitorPageQuery(point));
        verify(alerts).insert(org.mockito.ArgumentMatchers.<SecurityAlertDO>argThat(a -> WHITE.equals(a.getSourceIp())));
        verify(messages).insert(any(NotifyMessageDO.class));
    }
    @Test void asynchronousAccessMonitoringKeepsEveryAccessLogButOnlyOneWhitelistedAlert() throws Exception {
        DataAccessMonitorServiceImpl monitor=new DataAccessMonitorServiceImpl();DataAccessLogMapper logs=mock(DataAccessLogMapper.class);
        DataAccessConfigMapper configs=mock(DataAccessConfigMapper.class);
        DataAccessConfigDO config=new DataAccessConfigDO().setEnabled(true).setMaxRecordsPerWindow(1).setWindowSeconds(3600);
        when(configs.selectByModuleAndEntityType("test","fixture")).thenReturn(Collections.singletonList(config));
        when(values.increment(anyString(),eq(1L))).thenReturn(1L);
        inject(monitor,"dataAccessLogMapper",logs);inject(monitor,"dataAccessConfigMapper",configs);inject(monitor,"securityAlertService",service);inject(monitor,"stringRedisTemplate",redis);
        for(int i=0;i<2;i++) monitor.recordAccess(42L,"fixture","test","fixture","{}",Collections.singletonList(1L),1,"/synthetic-read",WHITE);
        verify(logs,times(2)).insert(any(DataAccessLogDO.class));
        verify(alerts).insert(org.mockito.ArgumentMatchers.<SecurityAlertDO>argThat(a -> WHITE.equals(a.getSourceIp())));
    }
}
