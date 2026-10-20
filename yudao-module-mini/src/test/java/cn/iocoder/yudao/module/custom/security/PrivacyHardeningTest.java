package cn.iocoder.yudao.module.custom.security;
import cn.iocoder.yudao.framework.apilog.core.filter.SensitiveLogSanitizer;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.module.custom.controller.admin.archive.ArchiveQueryController;
import cn.iocoder.yudao.module.custom.controller.admin.contract.ContractController;
import cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentAccessInterceptor;
import org.junit.jupiter.api.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.data.redis.core.*;
import com.fasterxml.jackson.databind.*;
import javax.servlet.http.*;
import java.net.*;
import java.lang.reflect.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class PrivacyHardeningTest {
 static void inject(Object o,String key,Object v)throws Exception{Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);f.set(o,v);}
 boolean authorize(Class<?> type,String method,boolean permission,boolean role)throws Exception {
  Method m=java.util.Arrays.stream(type.getMethods()).filter(x->x.getName().equals(method)).findFirst().get();
  SecurityFrameworkService ss=mock(SecurityFrameworkService.class);
  when(ss.hasPermission(anyString())).thenReturn(permission);when(ss.hasAnyRoles(any(String[].class))).thenReturn(role);
  StandardEvaluationContext context=new StandardEvaluationContext();context.setBeanResolver((ctx,name)->ss);
  return new SpelExpressionParser().parseExpression(m.getAnnotation(PreAuthorize.class).value()).getValue(context,Boolean.class);
 }
 @Test void ordinaryRoleCannotUseArchiveEvenWhenMenuPermissionGranted()throws Exception{assertFalse(authorize(ArchiveQueryController.class,"query",true,false));assertFalse(authorize(ArchiveQueryController.class,"tables",true,false));assertFalse(authorize(ArchiveQueryController.class,"runNow",true,false));}
 @Test void legitimateAdminKeepsArchiveAccess()throws Exception{assertTrue(authorize(ArchiveQueryController.class,"query",true,true));assertFalse(authorize(ArchiveQueryController.class,"query",false,true));}
 @Test void ordinaryRoleCannotReadUserDirectoryOrAdminContracts()throws Exception{assertFalse(authorize(ContractController.class,"adminAllUsers",true,false));assertFalse(authorize(ContractController.class,"adminSearch",true,false));assertFalse(authorize(ContractController.class,"adminDetail",true,false));assertTrue(authorize(ContractController.class,"adminAllUsers",true,true));}
 @Test void credentialVariantsAndNestedEncodedJsonAreRedacted()throws Exception{
  ObjectMapper mapper=new ObjectMapper();com.fasterxml.jackson.databind.node.ObjectNode root=mapper.createObjectNode();
  root.put("request_params","{\"body\":\"{\\\"oldPayPassword\\\":\\\"sample-secret\\\",\\\"new_password\\\":\\\"sample-secret\\\",\\\"realname\\\":\\\"sample-name\\\",\\\"keep\\\":1}\"}");
  root.put("Access_Token","sample-token");root.put("signature","sample-signature");root.put("safeId",42);
  String clean=SensitiveLogSanitizer.sanitize(root.toString());assertFalse(clean.contains("sample-"));assertTrue(clean.contains("safeId"));assertTrue(clean.contains("keep"));
 }
 @Test void malformedPayloadIsNeverReturnedUnredacted(){String value=SensitiveLogSanitizer.sanitize("{\"password\":\"sample-secret");assertFalse(value.contains("sample-secret"));assertTrue(value.contains("REDACTED"));}
 @Test void arraysAndExtraKeysAreRedacted(){String clean=SensitiveLogSanitizer.sanitize("[{\"payPassword\":\"sample-secret\",\"customKey\":\"sample-secret\",\"id\":1}]","customKey");assertFalse(clean.contains("sample-secret"));assertTrue(clean.contains("id"));}
 ContractAttachmentAccessInterceptor attachment()throws Exception{
  ContractAttachmentAccessInterceptor service=new ContractAttachmentAccessInterceptor();StringRedisTemplate redis=mock(StringRedisTemplate.class);ValueOperations<String,String> ops=mock(ValueOperations.class);when(redis.opsForValue()).thenReturn(ops);when(ops.get(anyString())).thenReturn(java.util.Base64.getEncoder().encodeToString(new byte[32]));inject(service,"stringRedisTemplate",redis);return service;
 }
 @Test void signedAttachmentWorksAndTamperedPathFails()throws Exception{
  ContractAttachmentAccessInterceptor service=attachment();HttpServletRequest req=mock(HttpServletRequest.class);when(req.getServerName()).thenReturn("staging.toms.chat");
  URI signed=new URI(service.signForAuthorizedResponse("https://org.toms.chat/admin-api/infra/file/28/get/private/contracts/42/a.png",req));
  String[] query=signed.getRawQuery().split("&");when(req.getRequestURI()).thenReturn(signed.getRawPath());when(req.getParameter("expires")).thenReturn(query[0].substring(8));when(req.getParameter("signature")).thenReturn(query[1].substring(10));
  assertEquals("staging.toms.chat",signed.getHost());assertTrue(service.preHandle(req,mock(HttpServletResponse.class),null));
  HttpServletResponse denied=mock(HttpServletResponse.class);when(denied.getWriter()).thenReturn(new java.io.PrintWriter(new java.io.StringWriter()));
  when(req.getRequestURI()).thenReturn(signed.getRawPath()+"other");assertFalse(service.preHandle(req,denied,null));
 }
 @Test void unsignedOrExpiredPrivateAttachmentIsDeniedButPublicImagesWork()throws Exception{
  ContractAttachmentAccessInterceptor service=attachment();HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse resp=mock(HttpServletResponse.class);when(resp.getWriter()).thenReturn(new java.io.PrintWriter(new java.io.StringWriter()));
  when(req.getRequestURI()).thenReturn("/admin-api/infra/file/28/get/private%2Fcontracts/42/a.png");assertFalse(service.preHandle(req,resp,null));
  when(req.getParameter("expires")).thenReturn("1");when(req.getParameter("signature")).thenReturn("bad");assertFalse(service.preHandle(req,resp,null));
  when(req.getRequestURI()).thenReturn("/admin-api/infra/file/28/get/public/brand.png");assertTrue(service.preHandle(req,resp,null));
 }
 @Test void adminContractSqlAlwaysBindsTenantBeforeSearchFilters() {
  org.apache.ibatis.session.Configuration config=new org.apache.ibatis.session.Configuration();
  config.addMapper(cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper.class);
  java.util.Map<String,Object> params=new java.util.HashMap<>();params.put("tenantId",162L);params.put("reqVO",new cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO());
  org.apache.ibatis.mapping.BoundSql search=config.getMappedStatement("cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper.selectAdminSearchPage").getBoundSql(params);
  assertTrue(search.getSql().contains("t.tenant_id = ?"));assertEquals("tenantId",search.getParameterMappings().get(0).getProperty());
  params.put("id",42L);org.apache.ibatis.mapping.BoundSql detail=config.getMappedStatement("cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper.selectByIdIgnoreDeleted").getBoundSql(params);
  assertTrue(detail.getSql().contains("AND tenant_id = ?"));assertEquals("tenantId",detail.getParameterMappings().get(1).getProperty());
 }

 @Test void archiveRejectsMissingAndForeignTenantEvenBeforeQueryingStorage() {
  ArchiveQueryController controller=new ArchiveQueryController();
  try {
   cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.clear();assertThrows(cn.iocoder.yudao.framework.common.exception.ServiceException.class,controller::tables);
   cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.setTenantId(162L);assertThrows(cn.iocoder.yudao.framework.common.exception.ServiceException.class,controller::tables);
   cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.setTenantId(1L);assertNotNull(controller.tables().getData());
  } finally {cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.clear();}
 }
 @Test void administratorDirectoryFiltersForeignTenantAndNeverDecryptsIdentity()throws Exception {
  ContractController controller=new ContractController();cn.iocoder.yudao.module.system.service.user.AdminUserService users=mock(cn.iocoder.yudao.module.system.service.user.AdminUserService.class);
  cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService cipher=mock(cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService.class);
  cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO own=new cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO();own.setId(1L);own.setTenantId(1L);own.setRealname("Example");own.setIdNo("stored-value");
  cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO foreign=new cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO();foreign.setId(2L);foreign.setTenantId(162L);
  when(users.getUserListByStatus(anyInt())).thenReturn(java.util.Arrays.asList(own,foreign));when(cipher.storedToCipherForResponse("stored-value")).thenReturn("encrypted-value");inject(controller,"contractAdminUserService",users);inject(controller,"idCardCipherService",cipher);
  try {cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.setTenantId(1L);java.util.List<java.util.Map<String,String>> data=controller.adminAllUsers().getData();assertEquals(1,data.size());assertEquals("encrypted-value",data.get(0).get("idNo"));verify(cipher,never()).resolveToPlain(anyString());}
  finally {cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.clear();}
 }

 @Test void unicodeAttachmentFilenameSurvivesBrowserEncoding()throws Exception {
  ContractAttachmentAccessInterceptor service=attachment();HttpServletRequest req=mock(HttpServletRequest.class);when(req.getServerName()).thenReturn("staging.toms.chat");
  URI uri=new URI(new URI(service.signForAuthorizedResponse("https://org.toms.chat/admin-api/infra/file/28/get/private/contracts/42/合同+1.png",req)).toASCIIString());
  String[] query=uri.getRawQuery().split("&");when(req.getRequestURI()).thenReturn(uri.getRawPath());when(req.getParameter("expires")).thenReturn(query[0].substring(8));when(req.getParameter("signature")).thenReturn(query[1].substring(10));
  assertTrue(service.preHandle(req,mock(HttpServletResponse.class),null));
 }

 @Test void traversalAndRepeatedSeparatorsCannotBypassPrivateDownloadGuard()throws Exception {
  ContractAttachmentAccessInterceptor service=attachment();HttpServletRequest req=mock(HttpServletRequest.class);HttpServletResponse response=mock(HttpServletResponse.class);when(response.getWriter()).thenReturn(new java.io.PrintWriter(new java.io.StringWriter()));
  for(String path:java.util.Arrays.asList("/admin-api/infra/file/28/get/public/../private/contracts/42/a.png","/admin-api/infra/file/28/get//private/contracts/42/a.png","/admin-api/infra/file/28/get/public/%2e%2e/private/contracts/42/a.png")) {
   when(req.getRequestURI()).thenReturn(path);assertFalse(service.preHandle(req,response,null));
  }
 }

 @Test void newAttachmentCannotReferenceAnotherUploaderAndExistingReferencesRemainValid()throws Exception {
  cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentBindingAspect aspect=new cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentBindingAspect();
  cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper contracts=mock(cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper.class);inject(aspect,"contractMapper",contracts);inject(aspect,"fileMapper",mock(cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper.class));
  cn.iocoder.yudao.framework.security.core.LoginUser user=new cn.iocoder.yudao.framework.security.core.LoginUser();user.setId(42L);org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user,null));
  try {
   cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO input=new cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO();input.setFile("https://org.toms.chat/admin-api/infra/file/28/get/private/contracts/99/a.png");
   org.aspectj.lang.JoinPoint point=mock(org.aspectj.lang.JoinPoint.class);when(point.getArgs()).thenReturn(new Object[]{input});assertThrows(cn.iocoder.yudao.framework.common.exception.ServiceException.class,()->aspect.validatePrivateReferences(point));
   cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO old=new cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO();old.setFile(input.getFile());input.setId(7L);when(contracts.selectById(7L)).thenReturn(old);assertDoesNotThrow(()->aspect.validatePrivateReferences(point));
   input.setFile("https://org.toms.chat/admin-api/infra/file/28/get/public/%2e%2e/private/contracts/99/a.png");assertThrows(cn.iocoder.yudao.framework.common.exception.ServiceException.class,()->aspect.validatePrivateReferences(point));
  } finally {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
 }
 @Test void genericUploadCannotWriteOtherUsersPrivateDirectoryOrTraversal()throws Exception {
  cn.iocoder.yudao.module.infra.service.file.FileServiceImpl files=new cn.iocoder.yudao.module.infra.service.file.FileServiceImpl();
  Method validate=files.getClass().getDeclaredMethod("validateUploadDirectory",String.class);validate.setAccessible(true);
  cn.iocoder.yudao.framework.security.core.LoginUser user=new cn.iocoder.yudao.framework.security.core.LoginUser();user.setId(42L);org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user,null));
  try {assertEquals("private/contracts/42",validate.invoke(files,"private/contracts/42"));assertEquals("public/brand",validate.invoke(files,"public/brand"));assertThrows(java.lang.reflect.InvocationTargetException.class,()->validate.invoke(files,"private/contracts/99"));assertThrows(java.lang.reflect.InvocationTargetException.class,()->validate.invoke(files,"public/../private/contracts/99"));}
  finally {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
 }

}
