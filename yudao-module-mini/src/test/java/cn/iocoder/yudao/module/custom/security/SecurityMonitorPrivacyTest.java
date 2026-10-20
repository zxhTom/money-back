package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.module.system.controller.admin.monitor.SecurityMonitorController;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.lang.reflect.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SecurityMonitorPrivacyTest {
 SecurityMonitorController controller;SecurityFrameworkService security;SecurityAlertService alerts;
 @BeforeEach void setup()throws Exception{
  LoginUser u=new LoginUser();u.setId(42L);u.setUserType(2);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null));
  controller=new SecurityMonitorController();security=mock(SecurityFrameworkService.class);alerts=mock(SecurityAlertService.class);set("securityFrameworkService",security);set("securityAlertService",alerts);
 }
 void set(String n,Object v)throws Exception{Field f=controller.getClass().getDeclaredField(n);f.setAccessible(true);f.set(controller,v);}
 @AfterEach void cleanup(){SecurityContextHolder.clearContext();}
 void admin(){when(security.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(true);when(security.hasAnyPermissions(anyString())).thenReturn(true);when(security.hasAnyPermissions(anyString(),anyString())).thenReturn(true);}
 @Test void everyMonitorOperationRejectsOrdinaryRoleBeforeAnyDataAccess()throws Exception{
  when(security.hasAnyPermissions(anyString())).thenReturn(true);when(security.hasAnyPermissions(anyString(),anyString())).thenReturn(true);int checked=0;
  for(Method m:SecurityMonitorController.class.getMethods()){
   if(m.getAnnotation(PreAuthorize.class)==null)continue;
   Object[] args=new Object[m.getParameterCount()];for(int i=0;i<args.length;i++)if(m.getParameterTypes()[i]==int.class)args[i]=1;
   InvocationTargetException e=assertThrows(InvocationTargetException.class,()->m.invoke(controller,args),m.getName());assertTrue(e.getCause() instanceof AccessDeniedException,m.getName());checked++;
  }
  assertEquals(15,checked);verifyNoInteractions(alerts);
 }
 @Test void anonymousCallerCannotUseInternalRoleChecksAsBypass(){admin();SecurityContextHolder.clearContext();assertThrows(AccessDeniedException.class,()->controller.onlineLookup(""));}
 @Test void managementRoleStillNeedsSpecificPermission(){admin();when(security.hasAnyPermissions("custom:security:online-lookup")).thenReturn(false);assertThrows(AccessDeniedException.class,()->controller.onlineLookup(""));}
 @Test void administratorKeepsOnlineLookupResponseShape(){admin();assertEquals(0,controller.onlineLookup("").getData().getTotal());assertEquals("USER",controller.onlineLookup("").getData().getType());}
 @Test void existingAlertPayloadsAreRedactedWithoutChangingStoredRows(){admin();String id="110101199001010010",name="Synthetic Private Name";SecurityAlertDO row=new SecurityAlertDO();row.setId(7L);row.setUserId(99L);row.setAlertMessage("User "+name+" has identity "+id);row.setDetail("{\"realname\":\""+name+"\",\"idNo\":\""+id+"\",\"safeId\":8}");row.setSuspiciousContent(row.getDetail());row.setHandleRemark("Identity "+id);row.setRequestUrl("/custom/test?idNo="+id);when(alerts.getPage(any(SecurityAlertPageReqVO.class))).thenReturn(new PageResult<>(Collections.singletonList(row),11L));PageResult<SecurityAlertDO> result=controller.getAlertPage(new SecurityAlertPageReqVO()).getData();SecurityAlertDO safe=result.getList().get(0);assertEquals(11L,result.getTotal());assertEquals(7L,safe.getId());assertEquals(99L,safe.getUserId());assertFalse(safe.getDetail().contains(name));assertFalse(safe.getDetail().contains(id));assertFalse(safe.getSuspiciousContent().contains(id));assertFalse(safe.getAlertMessage().contains(id));assertFalse(safe.getHandleRemark().contains(id));assertFalse(safe.getRequestUrl().contains(id));assertTrue(row.getDetail().contains(id));assertNotSame(row,safe);}
 @Test void malformedAlertJsonFailsClosed(){admin();SecurityAlertDO row=new SecurityAlertDO();row.setDetail("{malformed-private-text");when(alerts.getPage(any(SecurityAlertPageReqVO.class))).thenReturn(new PageResult<>(Collections.singletonList(row),1L));assertFalse(controller.getAlertPage(new SecurityAlertPageReqVO()).getData().getList().get(0).getDetail().contains("malformed-private-text"));}
 @Test void securityAlertManagementKeepsExistingMiniAdminPermissionAlias(){admin();when(security.hasAnyPermissions("custom:security:alert:query","mini:admin:security:alert")).thenReturn(true);when(alerts.getPage(any(SecurityAlertPageReqVO.class))).thenReturn(new PageResult<>(Collections.emptyList(),0L));assertEquals(0L,controller.getAlertPage(new SecurityAlertPageReqVO()).getData().getTotal());verify(security).hasAnyPermissions("custom:security:alert:query","mini:admin:security:alert");}
}
