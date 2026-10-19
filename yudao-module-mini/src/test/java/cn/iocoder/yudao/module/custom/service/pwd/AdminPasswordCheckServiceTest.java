package cn.iocoder.yudao.module.custom.service.pwd;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.custom.controller.admin.pwd.AdminPasswordCheckController;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.*;
import javax.sql.DataSource;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AdminPasswordCheckServiceTest {
 private JdbcTemplate jdbc; private AdminUserService users; private SecurityFrameworkService security; private AdminPasswordCheckService service;
 static void set(Object o,String name,Object value) throws Exception {java.lang.reflect.Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 @BeforeEach void setup() throws Exception {
  jdbc=mock(JdbcTemplate.class);users=mock(AdminUserService.class);security=mock(SecurityFrameworkService.class);
  when(security.hasAnyRoles("super_admin","tenant_admin")).thenReturn(true);
  service=new AdminPasswordCheckService(mock(DataSource.class),users,security);set(service,"jdbc",jdbc);TenantContextHolder.setTenantId(7L);
 }
 @AfterEach void cleanup(){TenantContextHolder.clear();}
 @Test void correctAndIncorrectLoginUsesExistingEncoder() {
  BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(4);String hash=encoder.encode("SyntheticOnly!42");
  doReturn(Collections.singletonList(hash)).when(jdbc).query(anyString(),any(RowMapper.class),any(),any());
  when(users.isPasswordMatch(anyString(),anyString())).thenAnswer(i->encoder.matches(i.getArgument(0),i.getArgument(1)));
  assertTrue(service.verify("someone","SyntheticOnly!42","login"));assertFalse(service.verify("someone","incorrect","login"));
  verify(jdbc,times(2)).query(contains("SELECT password "),any(RowMapper.class),eq(7L),eq("someone"));
 }
 @Test void paymentIsCheckedIndependentlyAndNeverTrimsPassword() {
  doReturn(Collections.singletonList("pay-hash")).when(jdbc).query(anyString(),any(RowMapper.class),any(),any());
  when(users.isPasswordMatch(" 123456 ","pay-hash")).thenReturn(true);
  assertTrue(service.verify(" someone "," 123456 ","pay"));
  verify(jdbc).query(contains("SELECT pay_password "),any(RowMapper.class),eq(7L),eq("someone"));
 }
 @Test void missingUnsetAndAmbiguousReturnFalse() {
  doReturn(Collections.emptyList(),Collections.singletonList(null),Arrays.asList("a","b")).when(jdbc).query(anyString(),any(RowMapper.class),any(),any());
  assertFalse(service.verify("missing","anything","login"));assertFalse(service.verify("unset","anything","pay"));assertFalse(service.verify("duplicate","anything","login"));verifyNoInteractions(users);
 }
 @Test void nonAdminCannotSearchOrVerify() {
  when(security.hasAnyRoles("super_admin","tenant_admin")).thenReturn(false);
  assertThrows(AccessDeniedException.class,()->service.verify("a","b","login"));assertThrows(AccessDeniedException.class,()->service.search("a"));verifyNoInteractions(jdbc,users);
 }
 @Test void invalidKindAndEmptyPasswordRejectedBeforeQuery() {
  assertThrows(RuntimeException.class,()->service.verify("a","b","sql"));assertThrows(RuntimeException.class,()->service.verify("a","","login"));verifyNoInteractions(jdbc,users);
 }
 @Test void passwordsNotSerializedOrIncludedInToString() throws Exception {
  AdminPasswordCheckController.CheckRequest r=new AdminPasswordCheckController.CheckRequest();r.setPassword("SyntheticSecret");
  assertFalse(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(r).contains("SyntheticSecret"));assertFalse(r.toString().contains("SyntheticSecret"));
 }
 @Test void endpointEnforcesAdminAndSuppressesBodyLogging() throws Exception {
  java.lang.reflect.Method method=AdminPasswordCheckController.class.getMethod("verify",AdminPasswordCheckController.CheckRequest.class);
  assertTrue(method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class).value().contains("hasAnyRoles"));
  assertFalse(method.getAnnotation(cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog.class).requestEnable());
  assertEquals(10,method.getAnnotation(cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter.class).count());
 }
}
