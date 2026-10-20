package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.audit.vo.AuditLogPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.audit.AuditLogMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.framework.audit.util.AuditLogPrivacy;
import cn.iocoder.yudao.module.custom.service.audit.AuditLogServiceImpl;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineServiceImpl;
import cn.iocoder.yudao.module.custom.service.contract.ContractTeaseGuard;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.lang.reflect.Field;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuditIdentityPrivacyTest {
 static void set(Object o,String n,Object v)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 AuditLogServiceImpl service;AuditLogMapper mapper;SecurityFrameworkService security;AuditLogPageReqVO query;
 static final String IDENTITY="110101199001010010",NAME="Synthetic Private Name";
 @BeforeEach void setup()throws Exception{
  LoginUser u=new LoginUser();u.setId(42L);u.setUserType(2);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null));TenantContextHolder.setTenantId(1L);
  service=new AuditLogServiceImpl();mapper=mock(AuditLogMapper.class);security=mock(SecurityFrameworkService.class);query=new AuditLogPageReqVO();set(service,"auditLogMapper",mapper);set(service,"securityFrameworkService",security);
 }
 @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
 void admin(){when(security.hasAnyRoles("super_admin","tenant_admin")).thenReturn(true);when(security.hasAnyRoles("super_admin")).thenReturn(true);when(security.hasPermission(anyString())).thenReturn(true);}
 AuditLogDO row(){AuditLogDO d=new AuditLogDO();d.setId(7L);d.setUserId(99L);d.setEntityId(8L);d.setOperationType("CREATE");d.setUsername(NAME);d.setOperation("创建合同 "+NAME+" "+IDENTITY);d.setRequestParams("{\"realname\":\""+NAME+"\",\"idNo\":\""+IDENTITY+"\",\"id\":8,\"status\":1,\"freeText\":\""+NAME+" "+IDENTITY+"\"}");d.setBeforeData(d.getRequestParams());d.setAfterData(d.getRequestParams());d.setErrorMessage(NAME+IDENTITY);d.setRequestUrl("/custom/contract/create?realname="+NAME+"&idNo="+IDENTITY);d.setUserAgent(NAME);d.setAllIpHeaders(NAME);return d;}
 void noPrivate(AuditLogDO d)throws Exception{String text=new ObjectMapper().writeValueAsString(d);assertFalse(text.contains(NAME));assertFalse(text.contains(IDENTITY));}
 @Test void ordinaryRoleCannotReadEvenWhenLegacyPermissionIsGranted(){when(security.hasPermission(anyString())).thenReturn(true);assertThrows(AccessDeniedException.class,()->service.getPage(query));assertThrows(AccessDeniedException.class,()->service.get(7L));assertThrows(AccessDeniedException.class,()->service.getExportPage(query));verifyNoInteractions(mapper);}
 @Test void adminWithoutExplicitPermissionCannotRead(){when(security.hasAnyRoles("super_admin","tenant_admin")).thenReturn(true);assertThrows(AccessDeniedException.class,()->service.getPage(query));assertThrows(AccessDeniedException.class,()->service.get(7L));assertThrows(AccessDeniedException.class,()->service.getExportPage(query));verifyNoInteractions(mapper);}
 @Test void anonymousRequestCannotReadEvenIfMethodIsCalledWithoutSecurityProxy(){SecurityContextHolder.clearContext();assertThrows(AccessDeniedException.class,()->service.getPage(query));verifyNoInteractions(mapper);}
 @Test void legacyPageResponseRemovesPrivateDataAndPreservesPagination()throws Exception{admin();AuditLogDO original=row();when(mapper.selectPage(query,(Long)null)).thenReturn(new PageResult<>(Collections.singletonList(original),17L));PageResult<AuditLogDO> p=service.getPage(query);assertEquals(17L,p.getTotal());noPrivate(p.getList().get(0));assertEquals(7L,p.getList().get(0).getId());assertTrue(original.getRequestParams().contains(IDENTITY));}
 @Test void legacyDetailIsSanitizedWithoutMutatingStoredObject()throws Exception{admin();AuditLogDO original=row();when(mapper.selectByIdInTenant(7L,null)).thenReturn(original);AuditLogDO d=service.get(7L);noPrivate(d);assertNotSame(original,d);assertTrue(original.getOperation().contains(NAME));}
 @Test void nonexistentDetailRemainsNull(){admin();assertNull(service.get(7L));}
 @Test void exportPermissionDoesNotRequireQueryPermissionAndStillSanitizes()throws Exception{admin();when(security.hasPermission("custom:audit-log:query")).thenReturn(false);when(mapper.selectPage(query,(Long)null)).thenReturn(new PageResult<>(Collections.singletonList(row()),1L));noPrivate(service.getExportPage(query).getList().get(0));assertThrows(AccessDeniedException.class,()->service.getPage(query));}
 @Test void queryPermissionDoesNotImplicitlyGrantExport(){admin();when(security.hasPermission("custom:audit-log:export")).thenReturn(false);assertThrows(AccessDeniedException.class,()->service.getExportPage(query));verifyNoInteractions(mapper);}
 @Test void newAuditWritesAreSanitizedWithoutChangingBusinessArguments()throws Exception{AuditLogDO original=row();service.saveAsync(original);ArgumentCaptor<AuditLogDO> c=ArgumentCaptor.forClass(AuditLogDO.class);verify(mapper).insert(c.capture());noPrivate(c.getValue());assertTrue(original.getRequestParams().contains(IDENTITY));}
 @Test void malformedHistoricalPayloadFailsClosed(){String result=AuditLogPrivacy.payload("{bad:"+NAME+IDENTITY);assertFalse(result.contains(NAME));assertFalse(result.contains(IDENTITY));}
 @Test void embeddedJsonAndUnknownTextCannotBypassRedaction()throws Exception{String nested="{\"realname\":\""+NAME+"\",\"idNo\":\""+IDENTITY+"\",\"misc\":\""+NAME+"\"}";Map<String,Object> outer=new HashMap<>();outer.put("embedded",nested);outer.put("unknown",NAME);outer.put("numericIdentity",Long.parseLong(IDENTITY));String result=AuditLogPrivacy.payload(new ObjectMapper().writeValueAsString(outer));assertFalse(result.contains(NAME));assertFalse(result.contains(IDENTITY));new ObjectMapper().readTree(result);}
 @Test void operationalIdsBooleansAndDatesRemainUseful()throws Exception{String json=AuditLogPrivacy.payload("{\"id\":8,\"status\":1,\"verified\":true,\"createTime\":\"2026-10-20 12:30:00\"}");assertEquals(8,new ObjectMapper().readTree(json).get("id").asInt());assertTrue(json.contains("2026-10-20 12:30:00"));assertTrue(json.contains("true"));}
 @Test void tenantAdminReadsAreScopedToTheirTenant(){admin();when(security.hasAnyRoles("super_admin")).thenReturn(false);when(mapper.selectPage(query,1L)).thenReturn(new PageResult<>(Collections.emptyList(),0L));service.getPage(query);service.get(7L);verify(mapper).selectPage(query,1L);verify(mapper).selectByIdInTenant(7L,1L);}
 @Test void missingTenantCannotFallBackToGlobalAuditAccess(){admin();when(security.hasAnyRoles("super_admin")).thenReturn(false);TenantContextHolder.clear();assertThrows(RuntimeException.class,()->service.getPage(query));verifyNoInteractions(mapper);}
 CustomDefineServiceImpl contacts;AdminUserService users;CustomDefineMapper custom;ContractMapper contracts;AdminUserDO caller;
 void contacts(Integer verified)throws Exception{contacts=new CustomDefineServiceImpl();users=mock(AdminUserService.class);custom=mock(CustomDefineMapper.class);contracts=mock(ContractMapper.class);caller=new AdminUserDO();caller.setId(42L);caller.setVerified(verified);caller.setRealname(NAME);caller.setIdNo(IDENTITY);when(users.getUser(42L)).thenReturn(caller);set(contacts,"adminUserService",users);set(contacts,"customDefineMapper",custom);set(contacts,"contractMapper",contracts);set(contacts,"idCardCipherService",mock(IdCardCipherService.class));set(contacts,"teaseGuard",mock(ContractTeaseGuard.class));}
 @Test void unverifiedProfilesCannotReachContactOrContractQueries()throws Exception{contacts(0);assertTrue(contacts.rencentContractList().isEmpty());assertTrue(contacts.page(new ContractPageReqDtoVO()).isEmpty());verifyNoInteractions(custom,contracts);}
 @Test void missingVerificationStateAlsoCannotReachQueries()throws Exception{contacts(null);assertTrue(contacts.rencentContractList().isEmpty());assertTrue(contacts.page(new ContractPageReqDtoVO()).isEmpty());verifyNoInteractions(custom,contracts);}
 @Test void blankCertifiedIdentityCannotRemoveDatabaseQueryScope()throws Exception{contacts(1);caller.setIdNo("");assertTrue(contacts.rencentContractList().isEmpty());assertTrue(contacts.page(new ContractPageReqDtoVO()).isEmpty());verifyNoInteractions(custom,contracts);}
 @Test void certifiedUsersKeepRecentContactNameAndFieldShape()throws Exception{contacts(1);RecentContractVO c=new RecentContractVO();c.setName("Synthetic Counterparty");when(custom.rencentContractList(IDENTITY)).thenReturn(Collections.singletonList(c));assertEquals("Synthetic Counterparty",contacts.rencentContractList().get(0).getName());}
 @Test void certifiedUsersKeepExistingContractList()throws Exception{contacts(1);com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(new org.apache.ibatis.builder.MapperBuilderAssistant(new com.baomidou.mybatisplus.core.MybatisConfiguration(),"audit-test"),ContractDO.class);ContractDO c=new ContractDO();c.setId(8L);c.setCreditorName("Synthetic Counterparty");when(contracts.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Collections.singletonList(c));ContractPageReqDtoVO q=new ContractPageReqDtoVO();q.setLoanType("borrow");assertEquals(8L,contacts.page(q).get(0).getId());assertEquals(IDENTITY,q.getIndebtedId());}
}
