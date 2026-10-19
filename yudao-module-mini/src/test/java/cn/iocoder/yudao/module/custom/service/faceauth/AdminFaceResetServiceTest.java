package cn.iocoder.yudao.module.custom.service.faceauth;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.*;
import javax.sql.DataSource;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AdminFaceResetServiceTest {
 JdbcTemplate jdbc; SecurityFrameworkService security; AdminFaceResetService service;
 @BeforeEach void setup() throws Exception {
  jdbc=mock(JdbcTemplate.class);security=mock(SecurityFrameworkService.class);
  when(security.hasAnyRoles("super_admin","tenant_admin")).thenReturn(true);
  service=new AdminFaceResetService(mock(DataSource.class),security);
  java.lang.reflect.Field field=service.getClass().getDeclaredField("jdbc");field.setAccessible(true);field.set(service,jdbc);
  TenantContextHolder.setTenantId(7L);
 }
 @AfterEach void cleanup(){TenantContextHolder.clear();}
 @Test void onlyResetsChosenUserWithinTenant() {
  doReturn(Collections.singletonList(42L)).when(jdbc).query(anyString(),any(RowMapper.class),eq(7L),eq("someone"));
  service.reset(" someone ");
  verify(jdbc).update("UPDATE system_users SET verified=0,update_time=CURRENT_TIMESTAMP WHERE id=? AND tenant_id=? AND deleted=0",42L,7L);
 }
 @Test void nonAdminCannotMutate(){when(security.hasAnyRoles("super_admin","tenant_admin")).thenReturn(false);assertThrows(AccessDeniedException.class,()->service.reset("someone"));verifyNoInteractions(jdbc);}
 @Test void invalidUsernameCannotMutate(){assertThrows(RuntimeException.class,()->service.reset(" "));assertThrows(RuntimeException.class,()->service.reset(null));verifyNoInteractions(jdbc);}
 @Test void missingOrAmbiguousUserCannotMutate(){
  doReturn(Collections.emptyList(),Arrays.asList(1L,2L)).when(jdbc).query(anyString(),any(RowMapper.class),any(),any());
  assertThrows(RuntimeException.class,()->service.reset("missing"));assertThrows(RuntimeException.class,()->service.reset("ambiguous"));
  verify(jdbc,never()).update(anyString(),any(),any());
 }
 @Test void missingTenantCannotMutate(){TenantContextHolder.clear();assertThrows(RuntimeException.class,()->service.reset("someone"));verifyNoInteractions(jdbc);}
}
