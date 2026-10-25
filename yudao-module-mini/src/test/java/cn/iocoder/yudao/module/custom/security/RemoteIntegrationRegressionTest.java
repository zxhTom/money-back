package cn.iocoder.yudao.module.custom.security;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.infra.framework.security.core.PlatformFileConfigGuard;
import cn.iocoder.yudao.module.infra.service.file.*;
import cn.iocoder.yudao.module.infra.service.file.quarantine.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.quarantine.FileQuarantineDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.*;
import cn.iocoder.yudao.module.infra.dal.mysql.file.quarantine.FileQuarantineMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.server.framework.migration.PrivacyStorageReadiness;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.jdbc.core.JdbcOperations;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import java.util.*;import static org.mockito.Mockito.*;import static org.junit.jupiter.api.Assertions.*;

class RemoteIntegrationRegressionTest {
 static void inject(Object o,String n,Object v)throws Exception {java.lang.reflect.Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 void login(long tenant){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new LoginUser().setId(42L).setUserType(2).setTenantId(tenant),null));TenantContextHolder.setTenantId(tenant);}
 @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
 @Test void platformTenantManagersKeepFileConfigButNotSuperAdminMonitorAccess()throws Exception {
  login(1);PlatformFileConfigGuard guard=new PlatformFileConfigGuard();SecurityFrameworkService ss=mock(SecurityFrameworkService.class);inject(guard,"securityFrameworkService",ss);inject(guard,"defaultTenantIdProperty",1L);when(ss.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(true);
  assertTrue(guard.isPlatformFileConfigAdmin());assertFalse(guard.isPlatformSuperAdmin());assertDoesNotThrow(guard::requirePlatformFileConfigAdmin);
  login(2);assertFalse(guard.isPlatformFileConfigAdmin());assertThrows(RuntimeException.class,guard::requirePlatformFileConfigAdmin);
 }
 @Test void crossTenantVisitCannotUseFileConfigAdministration()throws Exception {
  login(1);LoginUser user=(LoginUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();user.setVisitTenantId(2L);PlatformFileConfigGuard guard=new PlatformFileConfigGuard();inject(guard,"defaultTenantIdProperty",1L);inject(guard,"securityFrameworkService",mock(SecurityFrameworkService.class));assertFalse(guard.isPlatformFileConfigAdmin());
 }
 FileServiceImpl service;FileQuarantineMapper quarantine;
 void files(List<FileDO> rows)throws Exception{
  login(1);TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),"quarantine-integration"),FileQuarantineDO.class);
  quarantine=mock(FileQuarantineMapper.class,CALLS_REAL_METHODS);FileQuarantineServiceImpl guard=new FileQuarantineServiceImpl();inject(guard,"fileQuarantineMapper",quarantine);FileConfigService configs=mock(FileConfigService.class);FileClient client=mock(FileClient.class);when(configs.getFileClient(anyLong())).thenReturn(client);when(client.extractStorageKey(anyString())).thenAnswer(i->{String s=i.getArgument(0);return s.startsWith("https://fixture.invalid/")?s.substring("https://fixture.invalid/".length()):s;});inject(guard,"fileConfigService",configs);
  service=new FileServiceImpl();FileMapper mapper=mock(FileMapper.class);SecurityFrameworkService ss=mock(SecurityFrameworkService.class);when(ss.hasAnyRoles("super_admin")).thenReturn(true);inject(service,"fileQuarantineService",guard);inject(service,"fileMapper",mapper);inject(service,"security",ss);when(mapper.selectScopedPage(any(),isNull(),isNull())).thenReturn(new PageResult<>(rows,(long)rows.size()));
 }
 FileDO row(long id,long config,String path){return new FileDO().setId(id).setConfigId(config).setPath(path).setUrl("https://fixture.invalid/"+path);}
 @Test void hundredRowPageRequiresOneQuarantineQuery()throws Exception{
  List<FileDO> rows=new ArrayList<>();for(int i=0;i<100;i++)rows.add(row(i,28,"avatar/fixture-"+i+".png"));files(rows);service.getFilePage(new FilePageReqVO());verify(quarantine,times(1)).selectList(any());for(FileDO file:rows)assertNotNull(file.getUrl());
 }
 @Test void queryCountScalesWithConfigsRatherThanRows()throws Exception{
  List<FileDO> rows=new ArrayList<>();for(int i=0;i<100;i++)rows.add(row(i,i%2==0?28:29,"avatar/fixture-"+i+".png"));files(rows);service.getFilePage(new FilePageReqVO());verify(quarantine,times(2)).selectList(any());
 }
 @Test void batchQuarantinePreservesExactKeysAndDoesNotHideOrdinaryFiles()throws Exception{
  List<FileDO> rows=Arrays.asList(row(1,28,"history/人脸.mp4 "),row(2,28,"history/人脸.mp4"),row(3,28,"avatar/normal.png"));files(rows);
  String path=rows.get(0).getPath();FileQuarantineDO stored=FileQuarantineDO.builder().configId(28L).canonicalPath(path.getBytes(java.nio.charset.StandardCharsets.UTF_8)).pathHash(cn.hutool.crypto.digest.DigestUtil.sha256Hex(path)).build();doReturn(Collections.singletonList(stored)).when(quarantine).selectList(any());service.getFilePage(new FilePageReqVO());assertNull(rows.get(0).getUrl());assertNotNull(rows.get(1).getUrl());assertNotNull(rows.get(2).getUrl());verify(quarantine,times(1)).selectList(any());
 }
 @Test void unavailableQuarantineTableFailsClosed()throws Exception{
  files(Collections.singletonList(row(1,28,"avatar/normal.png")));doThrow(new org.springframework.dao.DataAccessResourceFailureException("fixture table missing")).when(quarantine).selectList(any());assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.getFilePage(new FilePageReqVO()));
 }
 JdbcOperations schema(){JdbcOperations jdbc=mock(JdbcOperations.class);when(jdbc.queryForList(anyString(),eq(String.class),eq("custom_face_evidence"),eq("uk_tenant_session_media"))).thenReturn(Arrays.asList("tenant_id","session_hash","media_type"));when(jdbc.queryForList(anyString(),eq(String.class),eq("infra_file_quarantine"),eq("uk_config_path_hash"))).thenReturn(Arrays.asList("config_id","path_hash"));return jdbc;}
 @Test void readySchemaPassesAndReadinessDependsOnMigrationRunner(){assertArrayEquals(new String[]{"sqlMigrationRunner"},PrivacyStorageReadiness.class.getAnnotation(org.springframework.context.annotation.DependsOn.class).value());assertDoesNotThrow(()->PrivacyStorageReadiness.verifySchema(schema()));}
 @Test void missingTableOrUniqueIndexPreventsReadiness(){JdbcOperations jdbc=schema();doThrow(new org.springframework.dao.DataAccessResourceFailureException("fixture missing")).when(jdbc).execute(anyString());assertThrows(IllegalStateException.class,()->PrivacyStorageReadiness.verifySchema(jdbc));JdbcOperations missingIndex=mock(JdbcOperations.class);assertThrows(IllegalStateException.class,()->PrivacyStorageReadiness.verifySchema(missingIndex));}
}
