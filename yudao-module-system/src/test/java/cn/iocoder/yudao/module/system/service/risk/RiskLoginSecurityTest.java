package cn.iocoder.yudao.module.system.service.risk;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.dal.mysql.risk.RiskUserLockLogMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskUserLockLogDO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class RiskLoginSecurityTest {
 RiskLockServiceImpl service;StringRedisTemplate redis;ValueOperations<String,String> values;JdbcTemplate jdbc;RiskUserLockLogMapper logs;
 static final Long USER=42L;
 static void inject(Object obj,String name,Object value) throws Exception {java.lang.reflect.Field f=RiskLockServiceImpl.class.getDeclaredField(name);f.setAccessible(true);f.set(obj,value);}
 @BeforeEach void setup() throws Exception {
  service=spy(new RiskLockServiceImpl());redis=mock(StringRedisTemplate.class);values=mock(ValueOperations.class);jdbc=mock(JdbcTemplate.class);logs=mock(RiskUserLockLogMapper.class);
  inject(service,"stringRedisTemplate",redis);inject(service,"riskUserLockLogMapper",logs);when(redis.opsForValue()).thenReturn(values);doReturn(jdbc).when(service).jdbc();
  TenantContextHolder.setTenantId(7L);
  doReturn(Collections.emptyList()).when(jdbc).query(anyString(),any(RowMapper.class),eq(USER),eq(7L));
  doReturn(Collections.singletonList(0)).when(jdbc).query(startsWith("SELECT status"),any(RowMapper.class),eq(USER),eq(7L));
 }
 @AfterEach void cleanup(){TenantContextHolder.clear();}
 @Test void temporaryPasswordLockHasDistinctCodeReasonAndDuration(){
  when(values.get("risk:lock_status:42")).thenReturn(String.valueOf(System.currentTimeMillis()+1799000));
  RiskUserLockLogDO log=new RiskUserLockLogDO();log.setLockType("PASSWORD_ERR");when(logs.selectList(any())).thenReturn(Collections.singletonList(log));
  ServiceException ex=assertThrows(ServiceException.class,()->service.checkLockStatus(USER));assertEquals(1002000011,ex.getCode());assertTrue(ex.getMessage().contains("密码错误"));assertTrue(ex.getMessage().contains("分钟"));assertFalse(ex.getMessage().contains("账号被禁用"));
 }
 @Test void permanentLockHasDistinctMessage(){when(values.get("risk:lock_status:42")).thenReturn("-1");ServiceException ex=assertThrows(ServiceException.class,()->service.checkLockStatus(USER));assertEquals(1002000012,ex.getCode());assertTrue(ex.getMessage().contains("管理员解锁"));}
 @Test void expiredLockDoesNotBlock(){when(values.get("risk:lock_status:42")).thenReturn("1");assertDoesNotThrow(()->service.checkLockStatus(USER));verify(redis).delete("risk:lock_status:42");}
 @Test void snapshotReturnsLiveCountAndTenantScopedHistoryWithoutChangingState(){
  when(values.get("risk:pwd_err:42")).thenReturn("4");when(redis.getExpire("risk:pwd_err:42",TimeUnit.SECONDS)).thenReturn(90L);
  Map<String,Object> state=service.getLoginSecurity(USER);assertEquals(4L,state.get("passwordErrorCount"));assertEquals(false,state.get("locked"));assertEquals(5,state.get("passwordErrorLimit"));assertNotNull(state.get("counterExpiresAt"));
  verify(jdbc).query(contains("system_login_log WHERE user_id=? AND (tenant_id=? OR tenant_id=0)"),any(RowMapper.class),eq(USER),eq(7L));verify(redis,never()).delete(anyString());
 }
 @Test void missingOrOtherTenantUserCannotReadOrUnlock(){
  doReturn(Collections.emptyList()).when(jdbc).query(startsWith("SELECT status"),any(RowMapper.class),eq(USER),eq(7L));
  assertThrows(ServiceException.class,()->service.getLoginSecurity(USER));assertThrows(ServiceException.class,()->service.unlockUser(1L,USER));verifyNoInteractions(redis,logs);
 }
 @Test void missingTenantCannotRead(){TenantContextHolder.clear();assertThrows(RuntimeException.class,()->service.getLoginSecurity(USER));verifyNoInteractions(redis,logs);}
 @Test void unlockClearsOnlyChosenAccountAndNeverChangesAccountStatus(){
  when(logs.selectList(any())).thenReturn(Collections.emptyList());service.unlockUser(1L,USER);
  verify(redis).delete("risk:lock_status:42");verify(redis).delete("risk:pwd_err:42");verify(jdbc,never()).update(anyString());
 }
 @Test void controllerReadAndWritePermissionsAreSeparate() throws Exception {
  Class<?> type=cn.iocoder.yudao.module.system.controller.admin.risk.RiskLockController.class;
  assertTrue(type.getMethod("getLoginSecurity",Long.class).getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class).value().contains("system:user:query"));
  assertTrue(type.getMethod("unlockUser",Long.class,String.class).getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class).value().contains("system:risk-lock:update"));
  assertNotNull(type.getMethod("unlockUser",Long.class,String.class).getAnnotation(com.mzt.logapi.starter.annotation.LogRecord.class));
 }
}
