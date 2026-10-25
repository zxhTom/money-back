package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.GlobalIpDiagnoseVO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpUserRefDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.LoginUserStatsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.LoginLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.logger.OperateLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.DataAccessLogMapper;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GlobalDiagnoseSecurityTest {

    private GlobalDiagnoseServiceImpl diagnoseService;
    private GlobalDiagnoseAsyncTask asyncTask;
    private GlobalDiagnoseAccessGuard accessGuard;

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private AdminUserService adminUserService;
    private PermissionService permissionService;
    private WebSocketSenderApi webSocketSenderApi;

    private LoginLogMapper loginLogMapper;
    private OperateLogMapper operateLogMapper;
    private DataAccessLogMapper dataAccessLogMapper;

    private final Map<String, String> inMemoryRedis = new ConcurrentHashMap<>();

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void login(Long userId, Integer userType, Long tenantId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(userId);
        loginUser.setUserType(userType);
        loginUser.setTenantId(tenantId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
        TenantContextHolder.setTenantId(tenantId);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        inMemoryRedis.clear();

        diagnoseService = new GlobalDiagnoseServiceImpl();
        asyncTask = new GlobalDiagnoseAsyncTask();
        GlobalDiagnoseAsyncTask mockAsyncTask = mock(GlobalDiagnoseAsyncTask.class);
        accessGuard = new GlobalDiagnoseAccessGuard();

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get(anyString())).thenAnswer(inv -> inMemoryRedis.get(inv.getArgument(0)));
        doAnswer(inv -> {
            inMemoryRedis.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            String val = inv.getArgument(1);
            return inMemoryRedis.putIfAbsent(key, val) == null;
        });

        doAnswer(inv -> {
            RedisScript<?> script = inv.getArgument(0);
            assertSame(GlobalDiagnoseAsyncTask.RELEASE_LOCK_SCRIPT, script, "必须使用生产 RELEASE_LOCK_SCRIPT 实例执行原子释放");
            String scriptText = script.getScriptAsString();
            assertEquals(GlobalDiagnoseAsyncTask.RELEASE_LOCK_LUA, scriptText);

            List<String> keys = inv.getArgument(1);
            Object[] rawArgs = inv.getArguments();
            String token = null;
            if (rawArgs.length > 2) {
                Object arg2 = rawArgs[2];
                if (arg2 instanceof Object[]) {
                    Object[] arr = (Object[]) arg2;
                    token = arr.length > 0 ? String.valueOf(arr[0]) : null;
                } else {
                    token = String.valueOf(arg2);
                }
            }
            String key = keys.get(0);
            String current = inMemoryRedis.get(key);
            // 真实匹配生产 Lua 语义：仅当 key 对应值与传入 token 匹配时才删除并返回 1，否则不删除并返回 0
            if (current != null && current.equals(token)) {
                inMemoryRedis.remove(key);
                return 1L;
            } else {
                return 0L;
            }
        }).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        adminUserService = mock(AdminUserService.class);
        permissionService = mock(PermissionService.class);
        webSocketSenderApi = mock(WebSocketSenderApi.class);

        loginLogMapper = mock(LoginLogMapper.class);
        operateLogMapper = mock(OperateLogMapper.class);
        dataAccessLogMapper = mock(DataAccessLogMapper.class);

        inject(accessGuard, "adminUserService", adminUserService);
        inject(accessGuard, "permissionService", permissionService);

        inject(diagnoseService, "asyncTask", mockAsyncTask);
        inject(diagnoseService, "stringRedisTemplate", redisTemplate);
        inject(diagnoseService, "diagnoseAccessGuard", accessGuard);

        inject(asyncTask, "stringRedisTemplate", redisTemplate);
        inject(asyncTask, "webSocketSenderApi", webSocketSenderApi);
        inject(asyncTask, "diagnoseAccessGuard", accessGuard);
        inject(asyncTask, "loginLogMapper", loginLogMapper);
        inject(asyncTask, "operateLogMapper", operateLogMapper);
        inject(asyncTask, "dataAccessLogMapper", dataAccessLogMapper);
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
        inMemoryRedis.clear();
    }

    @Test
    public void testTwoTenantsAndTwoUsersGenerateDistinctCacheAndLockKeys() {
        login(101L, UserTypeEnum.ADMIN.getValue(), 1L);
        AdminUserDO u1 = new AdminUserDO();
        u1.setId(101L);
        u1.setTenantId(1L);
        u1.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(101L)).thenReturn(u1);
        when(permissionService.hasAnyPermissions(eq(101L), any())).thenReturn(true);

        diagnoseService.triggerScanAllIps(7);
        assertTrue(inMemoryRedis.keySet().stream().anyMatch(k -> k.equals("diagnose:v2:scan:running:1:101:7")));

        login(102L, UserTypeEnum.ADMIN.getValue(), 1L);
        AdminUserDO u2 = new AdminUserDO();
        u2.setId(102L);
        u2.setTenantId(1L);
        u2.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(102L)).thenReturn(u2);
        when(permissionService.hasAnyPermissions(eq(102L), any())).thenReturn(true);

        diagnoseService.triggerScanAllIps(7);
        assertTrue(inMemoryRedis.keySet().stream().anyMatch(k -> k.equals("diagnose:v2:scan:running:1:102:7")));

        login(201L, UserTypeEnum.ADMIN.getValue(), 2L);
        AdminUserDO u3 = new AdminUserDO();
        u3.setId(201L);
        u3.setTenantId(2L);
        u3.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(201L)).thenReturn(u3);
        when(permissionService.hasAnyPermissions(eq(201L), any())).thenReturn(true);

        diagnoseService.triggerAnalyzeUserBehavior();
        assertTrue(inMemoryRedis.keySet().stream().anyMatch(k -> k.equals("diagnose:v2:behavior:running:2:201")));

        // 严格断言每次调用的具体 TTL：扫描为 600 秒，行为分析为 300 秒，严禁模糊放宽
        verify(valueOperations).setIfAbsent(eq("diagnose:v2:scan:running:1:101:7"), anyString(), eq(600L), eq(TimeUnit.SECONDS));
        verify(valueOperations).setIfAbsent(eq("diagnose:v2:scan:running:1:102:7"), anyString(), eq(600L), eq(TimeUnit.SECONDS));
        verify(valueOperations).setIfAbsent(eq("diagnose:v2:behavior:running:2:201"), anyString(), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testCacheHitOnlyWhenTenantUserAndDaysMatchEnvelope() {
        login(101L, UserTypeEnum.ADMIN.getValue(), 1L);
        AdminUserDO u1 = new AdminUserDO();
        u1.setId(101L);
        u1.setTenantId(1L);
        u1.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(101L)).thenReturn(u1);
        when(permissionService.hasAnyPermissions(eq(101L), any())).thenReturn(true);

        GlobalDiagnoseCacheEnvelope dayMismatchEnvelope = GlobalDiagnoseCacheEnvelope.builder()
                .tenantId(1L)
                .userId(101L)
                .taskType("scan-ips")
                .days(30)
                .dataJson("[]")
                .build();
        inMemoryRedis.put("diagnose:v2:scan:result:1:101:7", JSON.toJSONString(dayMismatchEnvelope));

        Map<String, Object> mismatchRes = diagnoseService.triggerScanAllIps(7);
        assertEquals("processing", mismatchRes.get("status"));

        GlobalDiagnoseCacheEnvelope tenantMismatchEnvelope = GlobalDiagnoseCacheEnvelope.builder()
                .tenantId(2L)
                .userId(101L)
                .taskType("scan-ips")
                .days(7)
                .dataJson("[]")
                .build();
        inMemoryRedis.put("diagnose:v2:scan:result:1:101:7", JSON.toJSONString(tenantMismatchEnvelope));
        inMemoryRedis.remove("diagnose:v2:scan:running:1:101:7");
        Map<String, Object> tenantMismatchRes = diagnoseService.triggerScanAllIps(7);
        assertEquals("processing", tenantMismatchRes.get("status"));

        GlobalDiagnoseCacheEnvelope validEnvelope = GlobalDiagnoseCacheEnvelope.builder()
                .tenantId(1L)
                .userId(101L)
                .taskType("scan-ips")
                .days(7)
                .dataJson("[]")
                .build();
        inMemoryRedis.put("diagnose:v2:scan:result:1:101:7", JSON.toJSONString(validEnvelope));
        Map<String, Object> cachedRes = diagnoseService.triggerScanAllIps(7);
        assertEquals("cached", cachedRes.get("status"));
    }

    @Test
    public void testSuccessfulExecutionPerformsTargetedPushAndReleasesOwnLock() {
        GlobalDiagnoseTaskContext context = new GlobalDiagnoseTaskContext(1L, 101L, "scan-ips", 7, "token-scan-123");
        String runningKey = "diagnose:v2:scan:running:1:101:7";
        String resultKey = "diagnose:v2:scan:result:1:101:7";
        inMemoryRedis.put(runningKey, "token-scan-123");

        AdminUserDO user = new AdminUserDO();
        user.setId(101L);
        user.setTenantId(1L);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(101L)).thenReturn(user);
        when(permissionService.hasAnyPermissions(eq(101L), any())).thenReturn(true);

        IpUserRefDO row = new IpUserRefDO();
        row.setIp("10.0.0.1");
        row.setUserId(101L);
        row.setUsername("admin1");
        row.setLastSeen("2026-10-24 01:00:00");
        when(loginLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.singletonList(row));
        when(operateLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());
        when(dataAccessLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());

        asyncTask.doScanAllIpsAsync(context, resultKey, runningKey);

        ArgumentCaptor<Map<String, Object>> doneCaptor = ArgumentCaptor.forClass(Map.class);
        verify(webSocketSenderApi).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(101L), eq("diagnose-done"), doneCaptor.capture());
        Map<String, Object> payload = doneCaptor.getValue();
        assertEquals("scan-ips", payload.get("taskType"));
        assertNotNull(payload.get("data"));
        assertTrue(payload.get("data") instanceof List);
        List<?> dataList = (List<?>) payload.get("data");
        assertEquals(1, dataList.size());
        assertTrue(dataList.get(0) instanceof GlobalIpDiagnoseVO);
        GlobalIpDiagnoseVO vo = (GlobalIpDiagnoseVO) dataList.get(0);
        assertEquals("10.0.0.1", vo.getIp());
        assertEquals("private", vo.getType());
        assertFalse(vo.isRisky());

        // 验证绝不广播，且不发给其他用户
        verify(webSocketSenderApi, never()).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq("diagnose-done"), any());
        verify(webSocketSenderApi, never()).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(102L), anyString(), any());
        verify(webSocketSenderApi, never()).sendObject(anyInt(), anyLong(), eq("diagnose-error"), any());

        assertNull(inMemoryRedis.get(runningKey));
        assertNotNull(inMemoryRedis.get(resultKey));
    }

    @Test
    public void testRevocationDuringExecutionStopsPushingResults() {
        GlobalDiagnoseTaskContext context = new GlobalDiagnoseTaskContext(1L, 101L, "user-behavior", null, "token-behavior-999");
        String runningKey = "diagnose:v2:behavior:running:1:101";
        String resultKey = "diagnose:v2:behavior:result:1:101";
        inMemoryRedis.put(runningKey, "token-behavior-999");

        AdminUserDO enabledUser = new AdminUserDO();
        enabledUser.setId(101L);
        enabledUser.setTenantId(1L);
        enabledUser.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 1. 模拟中途撤销权限路径：任务开始时存在且权限有效（true），但在推送结果前权限被撤销（false）
        when(adminUserService.getUser(101L)).thenReturn(enabledUser);

        when(permissionService.hasAnyPermissions(eq(101L), any()))
                .thenReturn(true)  // 初始进度校验通过
                .thenReturn(false); // 最终推送结果前被撤销权限

        // 明确验证 canPushResult 在中途状态从 true 变为 false 的转换行为
        assertTrue(accessGuard.canPushResult(1L, 101L), "初始阶段用户必须具备合法诊断权限 (true)");
        assertFalse(accessGuard.canPushResult(1L, 101L), "撤权后用户诊断权限状态必须变为 (false)");

        // 再次将 mock 序列设为：进度检查通过(true)，完成检查拒绝(false)
        when(permissionService.hasAnyPermissions(eq(101L), any()))
                .thenReturn(true)
                .thenReturn(false);

        LoginUserStatsDO stat = new LoginUserStatsDO();
        stat.setUserId(101L);
        stat.setUsername("admin1");
        when(loginLogMapper.selectDiagnoseUserLoginStats(1L)).thenReturn(Collections.singletonList(stat));
        when(loginLogMapper.selectDiagnoseUserLoginCountLastHour(1L)).thenReturn(Collections.emptyList());

        asyncTask.doAnalyzeBehaviorAsync(context, resultKey, runningKey);

        // 验证进度事件曾成功推送，但完成结果绝对因中途权限撤销未推送
        verify(webSocketSenderApi, times(1)).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(101L), eq("diagnose-progress"), any());
        verify(webSocketSenderApi, never()).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(101L), eq("diagnose-done"), any());
        verify(webSocketSenderApi, never()).sendObject(anyInt(), anyLong(), eq("diagnose-error"), any());
        assertNull(inMemoryRedis.get(runningKey));
    }

    @Test
    public void testUserDisabledDuringExecutionStopsPushingResults() {
        GlobalDiagnoseTaskContext context = new GlobalDiagnoseTaskContext(1L, 101L, "user-behavior", null, "token-behavior-888");
        String runningKey = "diagnose:v2:behavior:running:1:101";
        String resultKey = "diagnose:v2:behavior:result:1:101";
        inMemoryRedis.put(runningKey, "token-behavior-888");

        AdminUserDO enabledUser = new AdminUserDO();
        enabledUser.setId(101L);
        enabledUser.setTenantId(1L);
        enabledUser.setStatus(CommonStatusEnum.ENABLE.getStatus());

        AdminUserDO disabledUser = new AdminUserDO();
        disabledUser.setId(101L);
        disabledUser.setTenantId(1L);
        disabledUser.setStatus(CommonStatusEnum.DISABLE.getStatus());

        when(permissionService.hasAnyPermissions(eq(101L), any())).thenReturn(true);

        // 明确验证 canPushResult 在用户停用时由 true 变为 false 的状态切换
        when(adminUserService.getUser(101L)).thenReturn(enabledUser);
        assertTrue(accessGuard.canPushResult(1L, 101L), "初始阶段用户启用状态下权限有效");

        when(adminUserService.getUser(101L)).thenReturn(disabledUser);
        assertFalse(accessGuard.canPushResult(1L, 101L), "用户停用后 canPushResult 状态必须转为 false");

        // 2. 模拟中途用户被停用路径：任务开始时用户启用且权限具备，但推送结果前账号被停用
        when(adminUserService.getUser(101L))
                .thenReturn(enabledUser)   // 初始进度检查用户启用
                .thenReturn(disabledUser); // 结果推送前账号被禁用

        LoginUserStatsDO stat = new LoginUserStatsDO();
        stat.setUserId(101L);
        stat.setUsername("admin1");
        when(loginLogMapper.selectDiagnoseUserLoginStats(1L)).thenReturn(Collections.singletonList(stat));
        when(loginLogMapper.selectDiagnoseUserLoginCountLastHour(1L)).thenReturn(Collections.emptyList());

        asyncTask.doAnalyzeBehaviorAsync(context, resultKey, runningKey);

        // 验证进度事件曾成功推送，但用户停用路径下绝不推送最终敏感结果
        verify(webSocketSenderApi, times(1)).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(101L), eq("diagnose-progress"), any());
        verify(webSocketSenderApi, never()).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(101L), eq("diagnose-done"), any());
        verify(webSocketSenderApi, never()).sendObject(anyInt(), anyLong(), eq("diagnose-error"), any());
        assertNull(inMemoryRedis.get(runningKey));
    }

    @Test
    public void testLockCompareAndDeleteSemantics() {
        // 验证 Lua 脚本文本及 DefaultRedisScript 语义完全一致
        assertEquals("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
                GlobalDiagnoseAsyncTask.RELEASE_LOCK_LUA);
        assertEquals(GlobalDiagnoseAsyncTask.RELEASE_LOCK_LUA,
                GlobalDiagnoseAsyncTask.RELEASE_LOCK_SCRIPT.getScriptAsString());
        assertEquals(Long.class, GlobalDiagnoseAsyncTask.RELEASE_LOCK_SCRIPT.getResultType());

        String runningKey = "diagnose:v2:scan:running:1:101:7";
        inMemoryRedis.put(runningKey, "new-task-token-888");

        // token 不匹配：旧任务执行完毕不应释放新任务的锁
        GlobalDiagnoseTaskContext oldContext = new GlobalDiagnoseTaskContext(1L, 101L, "scan-ips", 7, "old-task-token-111");
        asyncTask.doScanAllIpsAsync(oldContext, "result-key", runningKey);
        assertEquals("new-task-token-888", inMemoryRedis.get(runningKey), "token 不匹配时锁绝不能被误释放");

        // token 匹配：持有当前锁 token 的任务正常释放锁
        GlobalDiagnoseTaskContext newContext = new GlobalDiagnoseTaskContext(1L, 101L, "scan-ips", 7, "new-task-token-888");
        asyncTask.doScanAllIpsAsync(newContext, "result-key", runningKey);
        assertNull(inMemoryRedis.get(runningKey), "token 匹配时必须成功原子删除释放锁");

        // 断言 redisTemplate 实际以 RELEASE_LOCK_SCRIPT 作为参数调用过 execute
        verify(redisTemplate, atLeastOnce()).execute(eq(GlobalDiagnoseAsyncTask.RELEASE_LOCK_SCRIPT),
                eq(Collections.singletonList(runningKey)), any());
    }

    @Test
    public void testStrictTenantIsolationOnLogsDbQuery() {
        IpUserRefDO t1Log = new IpUserRefDO();
        t1Log.setIp("10.1.1.1");
        t1Log.setUserId(101L);

        IpUserRefDO t2Log = new IpUserRefDO();
        t2Log.setIp("10.2.2.2");
        t2Log.setUserId(201L);

        when(loginLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.singletonList(t1Log));
        when(loginLogMapper.selectDiagnoseIpUserRefs(2L, 7)).thenReturn(Collections.singletonList(t2Log));
        when(operateLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());
        when(operateLogMapper.selectDiagnoseIpUserRefs(2L, 7)).thenReturn(Collections.emptyList());
        when(dataAccessLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());
        when(dataAccessLogMapper.selectDiagnoseIpUserRefs(2L, 7)).thenReturn(Collections.emptyList());

        GlobalDiagnoseTaskContext ctxT1 = new GlobalDiagnoseTaskContext(1L, 101L, "scan-ips", 7, "t1");
        GlobalDiagnoseTaskContext ctxT2 = new GlobalDiagnoseTaskContext(2L, 201L, "scan-ips", 7, "t2");

        AdminUserDO u1 = new AdminUserDO();
        u1.setId(101L);
        u1.setTenantId(1L);
        u1.setStatus(CommonStatusEnum.ENABLE.getStatus());
        AdminUserDO u2 = new AdminUserDO();
        u2.setId(201L);
        u2.setTenantId(2L);
        u2.setStatus(CommonStatusEnum.ENABLE.getStatus());

        when(adminUserService.getUser(101L)).thenReturn(u1);
        when(adminUserService.getUser(201L)).thenReturn(u2);
        when(permissionService.hasAnyPermissions(anyLong(), any())).thenReturn(true);

        asyncTask.doScanAllIpsAsync(ctxT1, "res1", "run1");
        asyncTask.doScanAllIpsAsync(ctxT2, "res2", "run2");

        verify(loginLogMapper).selectDiagnoseIpUserRefs(1L, 7);
        verify(loginLogMapper).selectDiagnoseIpUserRefs(2L, 7);
        verify(operateLogMapper).selectDiagnoseIpUserRefs(1L, 7);
        verify(operateLogMapper).selectDiagnoseIpUserRefs(2L, 7);
        verify(dataAccessLogMapper).selectDiagnoseIpUserRefs(1L, 7);
        verify(dataAccessLogMapper).selectDiagnoseIpUserRefs(2L, 7);

        // 验证推送结果互不串扰
        ArgumentCaptor<Map<String, Object>> t1Done = ArgumentCaptor.forClass(Map.class);
        verify(webSocketSenderApi).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(101L), eq("diagnose-done"), t1Done.capture());
        List<GlobalIpDiagnoseVO> t1List = (List<GlobalIpDiagnoseVO>) t1Done.getValue().get("data");
        assertEquals(1, t1List.size());
        assertEquals("10.1.1.1", t1List.get(0).getIp());

        ArgumentCaptor<Map<String, Object>> t2Done = ArgumentCaptor.forClass(Map.class);
        verify(webSocketSenderApi).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(201L), eq("diagnose-done"), t2Done.capture());
        List<GlobalIpDiagnoseVO> t2List = (List<GlobalIpDiagnoseVO>) t2Done.getValue().get("data");
        assertEquals(1, t2List.size());
        assertEquals("10.2.2.2", t2List.get(0).getIp());
    }

    @Test
    public void testExistingLogsWithTenantZeroFoundForDiagnoseUser() {
        // 验证用户在租户 1 下执行诊断时，即便历史日志写入 tenant_id=0（BaseDO 默认不传租户），
        // 只要 system_users 归属为当前租户 1 且 user_type=ADMIN，诊断即可正确查到该用户关联 IP
        IpUserRefDO t0Log = new IpUserRefDO();
        t0Log.setIp("10.0.0.88");
        t0Log.setUserId(101L);
        t0Log.setUsername("admin1");

        when(loginLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.singletonList(t0Log));
        when(operateLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());
        when(dataAccessLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());

        GlobalDiagnoseTaskContext context = new GlobalDiagnoseTaskContext(1L, 101L, "scan-ips", 7, "token-t0");
        AdminUserDO user = new AdminUserDO();
        user.setId(101L);
        user.setTenantId(1L);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(101L)).thenReturn(user);
        when(permissionService.hasAnyPermissions(eq(101L), any())).thenReturn(true);

        asyncTask.doScanAllIpsAsync(context, "resT0", "runT0");

        ArgumentCaptor<Map<String, Object>> doneCaptor = ArgumentCaptor.forClass(Map.class);
        verify(webSocketSenderApi).sendObject(eq(UserTypeEnum.ADMIN.getValue()), eq(101L), eq("diagnose-done"), doneCaptor.capture());
        List<GlobalIpDiagnoseVO> voList = (List<GlobalIpDiagnoseVO>) doneCaptor.getValue().get("data");
        assertEquals(1, voList.size());
        assertEquals("10.0.0.88", voList.get(0).getIp());
    }

    @Test
    public void testTenantContextRestorationAfterExecution() {
        TenantContextHolder.setTenantId(999L);
        GlobalDiagnoseTaskContext context = new GlobalDiagnoseTaskContext(1L, 101L, "scan-ips", 7, "token-restore-1");

        AdminUserDO user = new AdminUserDO();
        user.setId(101L);
        user.setTenantId(1L);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(101L)).thenReturn(user);
        when(permissionService.hasAnyPermissions(eq(101L), any())).thenReturn(true);

        when(loginLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());
        when(operateLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());
        when(dataAccessLogMapper.selectDiagnoseIpUserRefs(1L, 7)).thenReturn(Collections.emptyList());

        asyncTask.doScanAllIpsAsync(context, "resKey", "runKey");

        // 验证执行完成后 TenantContextHolder 恢复为原值 999L
        assertEquals(999L, TenantContextHolder.getTenantId());
    }

    @Test
    public void testSyncScanAllIps_Unauthenticated_RejectedWithoutMapperQuery() {
        // 无登录状态
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();

        ServiceException ex = assertThrows(ServiceException.class, () -> diagnoseService.scanAllIps(7));
        assertEquals(401, ex.getCode());

        verifyNoInteractions(loginLogMapper);
        verifyNoInteractions(operateLogMapper);
        verifyNoInteractions(dataAccessLogMapper);
    }

    @Test
    public void testSyncScanAllIps_MissingTenant_RejectedWithoutMapperQuery() {
        // 登录用户无 tenantId
        login(101L, UserTypeEnum.ADMIN.getValue(), null);
        TenantContextHolder.clear();

        ServiceException ex = assertThrows(ServiceException.class, () -> diagnoseService.scanAllIps(7));
        assertEquals(403, ex.getCode());

        verifyNoInteractions(loginLogMapper);
        verifyNoInteractions(operateLogMapper);
        verifyNoInteractions(dataAccessLogMapper);
    }

    @Test
    public void testSyncScanAllIps_MismatchedTenant_RejectedWithoutMapperQuery() {
        // 登录租户为 1，但上下文租户被篡改为 2
        login(101L, UserTypeEnum.ADMIN.getValue(), 1L);
        TenantContextHolder.setTenantId(2L);

        ServiceException ex = assertThrows(ServiceException.class, () -> diagnoseService.scanAllIps(7));
        assertEquals(403, ex.getCode());

        verifyNoInteractions(loginLogMapper);
        verifyNoInteractions(operateLogMapper);
        verifyNoInteractions(dataAccessLogMapper);
    }

    @Test
    public void testSyncScanAllIps_DisabledUser_RejectedWithoutMapperQuery() {
        // 用户被禁用
        login(101L, UserTypeEnum.ADMIN.getValue(), 1L);
        AdminUserDO user = new AdminUserDO();
        user.setId(101L);
        user.setTenantId(1L);
        user.setStatus(CommonStatusEnum.DISABLE.getStatus());
        when(adminUserService.getUser(101L)).thenReturn(user);

        ServiceException ex = assertThrows(ServiceException.class, () -> diagnoseService.scanAllIps(7));
        assertEquals(403, ex.getCode());

        verifyNoInteractions(loginLogMapper);
        verifyNoInteractions(operateLogMapper);
        verifyNoInteractions(dataAccessLogMapper);
    }

    @Test
    public void testSyncScanAllIps_NoPermission_RejectedWithoutMapperQuery() {
        // 用户无诊断权限
        login(101L, UserTypeEnum.ADMIN.getValue(), 1L);
        AdminUserDO user = new AdminUserDO();
        user.setId(101L);
        user.setTenantId(1L);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(101L)).thenReturn(user);
        when(permissionService.hasAnyPermissions(eq(101L), any())).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class, () -> diagnoseService.scanAllIps(7));
        assertEquals(403, ex.getCode());

        verifyNoInteractions(loginLogMapper);
        verifyNoInteractions(operateLogMapper);
        verifyNoInteractions(dataAccessLogMapper);
    }

    @Test
    public void testSyncServicePassesValidatedTenantIdToAsyncTask() throws Exception {
        // 将真实的 asyncTask 注入 diagnoseService
        inject(diagnoseService, "asyncTask", asyncTask);

        login(201L, UserTypeEnum.ADMIN.getValue(), 2L);
        AdminUserDO user = new AdminUserDO();
        user.setId(201L);
        user.setTenantId(2L);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(adminUserService.getUser(201L)).thenReturn(user);
        when(permissionService.hasAnyPermissions(eq(201L), any())).thenReturn(true);

        when(loginLogMapper.selectDiagnoseIpUserRefs(2L, 7)).thenReturn(Collections.emptyList());
        when(operateLogMapper.selectDiagnoseIpUserRefs(2L, 7)).thenReturn(Collections.emptyList());
        when(dataAccessLogMapper.selectDiagnoseIpUserRefs(2L, 7)).thenReturn(Collections.emptyList());

        List<GlobalIpDiagnoseVO> scanRes = diagnoseService.scanAllIps(7);
        assertNotNull(scanRes);

        // 验证只向传入租户 2L 查询，绝不查询租户 1L
        verify(loginLogMapper).selectDiagnoseIpUserRefs(eq(2L), eq(7));
        verify(operateLogMapper).selectDiagnoseIpUserRefs(eq(2L), eq(7));
        verify(dataAccessLogMapper).selectDiagnoseIpUserRefs(eq(2L), eq(7));
        verify(loginLogMapper, never()).selectDiagnoseIpUserRefs(eq(1L), anyInt());

        when(loginLogMapper.selectDiagnoseUserLoginStats(2L)).thenReturn(Collections.emptyList());
        when(loginLogMapper.selectDiagnoseUserLoginCountLastHour(2L)).thenReturn(Collections.emptyList());

        diagnoseService.analyzeUserBehavior();

        verify(loginLogMapper).selectDiagnoseUserLoginStats(eq(2L));
        verify(loginLogMapper).selectDiagnoseUserLoginCountLastHour(eq(2L));
        verify(loginLogMapper, never()).selectDiagnoseUserLoginStats(eq(1L));
    }

    @Test
    public void testDirectAsyncTaskWithMissingTenantId_RejectedWithoutDefaultingToTenantOne() {
        TenantContextHolder.clear();

        // 1. 同步 scanAllIps(null, days) 必须抛异常且不查询 mapper
        ServiceException ex1 = assertThrows(ServiceException.class, () -> asyncTask.scanAllIps(null, 7));
        assertEquals(403, ex1.getCode());

        // 2. 同步 scanAllIps(days) 在 TenantContextHolder 为空时也必须抛异常
        ServiceException ex2 = assertThrows(ServiceException.class, () -> asyncTask.scanAllIps(7));
        assertEquals(403, ex2.getCode());

        // 3. 同步 analyzeUserBehavior(null) 必须抛异常
        ServiceException ex3 = assertThrows(ServiceException.class, () -> asyncTask.analyzeUserBehavior(null));
        assertEquals(403, ex3.getCode());

        // 4. 同步 analyzeUserBehavior() 在 TenantContextHolder 为空时抛异常
        ServiceException ex4 = assertThrows(ServiceException.class, () -> asyncTask.analyzeUserBehavior());
        assertEquals(403, ex4.getCode());

        // 5. 异步 doScanAllIpsAsync context tenantId 为空时静默失败关闭，不查询 mapper
        GlobalDiagnoseTaskContext nullTenantContext = new GlobalDiagnoseTaskContext(null, 101L, "scan-ips", 7, "token-null");
        asyncTask.doScanAllIpsAsync(nullTenantContext, "resKey", "runKey");

        // 6. 异步 doAnalyzeBehaviorAsync context tenantId 为空时失败关闭，不查询 mapper
        asyncTask.doAnalyzeBehaviorAsync(nullTenantContext, "resKey", "runKey");

        // 确认没有对任何 mapper 发起过查询（更绝无对 tenant 1 的默认查询）
        verifyNoInteractions(loginLogMapper);
        verifyNoInteractions(operateLogMapper);
        verifyNoInteractions(dataAccessLogMapper);
    }
}
