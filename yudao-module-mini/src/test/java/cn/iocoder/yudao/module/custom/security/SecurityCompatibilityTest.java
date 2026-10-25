package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.PayOrderVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.DeviceModuleAccessController;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractGuessRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.framework.faceauth.FaceAuthCallbackTokenStore;
import cn.iocoder.yudao.module.custom.service.contract.*;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineServiceImpl;
import cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard;
import cn.iocoder.yudao.module.custom.service.autotest.AutoTestServiceImpl;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserServiceImpl;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.redis.core.*;
import java.lang.reflect.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityCompatibilityTest {
 static void set(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 @BeforeEach void login(){LoginUser u=new LoginUser();u.setId(42L);u.setUserType(2);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null));TenantContextHolder.setTenantId(1L);}
 @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
 AdminUserDO user(){AdminUserDO u=new AdminUserDO();u.setId(42L);u.setTenantId(1L);u.setIdNo("stored-id");u.setRealname("Test Person");u.setVerified(1);return u;}
 ContractPaymentAccessGuard payment;PayOrderService orders;AdminUserService users;CustomDefineMapper custom;ContractMapper contracts;ContractOwnershipGuard ownership;PayOrderDO order;
 void payment()throws Exception{
  payment=new ContractPaymentAccessGuard();orders=mock(PayOrderService.class);users=mock(AdminUserService.class);custom=mock(CustomDefineMapper.class);contracts=mock(ContractMapper.class);ownership=mock(ContractOwnershipGuard.class);
  set(payment,"orderService",orders);set(payment,"userService",users);set(payment,"customDefineMapper",custom);set(payment,"contractMapper",contracts);set(payment,"ownershipGuard",ownership);
  order=new PayOrderDO();order.setId(7L);order.setUserId(42L);order.setUserType(2);when(orders.getOrder(7L)).thenReturn(order);when(users.getUser(42L)).thenReturn(user());
 }
 @Test void releasedClientCanReadAndPollOwnOrder()throws Exception{payment();assertSame(order,payment.requireOrder(7L));}
 @Test void unrelatedOrderIsDenied()throws Exception{payment();order.setUserId(99L);AdminUserDO other=user();other.setId(99L);when(users.getUser(99L)).thenReturn(other);assertThrows(AccessDeniedException.class,()->payment.requireOrder(7L));}
 @Test void anotherTenantOrderIsDeniedEvenWhenOwnerMatches()throws Exception{payment();AdminUserDO foreign=user();foreign.setTenantId(2L);when(users.getUser(42L)).thenReturn(foreign);assertThrows(AccessDeniedException.class,()->payment.requireOrder(7L));}
 @Test void memberTokenRealmIsNeverAcceptedForAdminPayment()throws Exception{payment();order.setUserType(1);assertThrows(AccessDeniedException.class,()->payment.requireOrder(7L));}
 @Test void legitimateContractPartyKeepsPaymentReadAccess()throws Exception{payment();order.setUserId(99L);AdminUserDO other=user();other.setId(99L);when(users.getUser(99L)).thenReturn(other);when(custom.selectContractByPayOrderId(7L)).thenReturn(8L);ContractDO c=new ContractDO();when(contracts.selectById(8L)).thenReturn(c);when(ownership.isAuthorized(c,42L)).thenReturn(true);assertSame(order,payment.requireOrder(7L));}
 ContractServiceImpl recommendation;IdCardCipherService cipher;
 void recommendation()throws Exception{recommendation=new ContractServiceImpl();users=mock(AdminUserService.class);contracts=mock(ContractMapper.class);cipher=mock(IdCardCipherService.class);when(users.getUser(42L)).thenReturn(user());when(cipher.sameIdCard("stored-id","own-id")).thenReturn(true);when(cipher.normalizeRequestToCipher("stored-id")).thenReturn("cipher-id");when(cipher.resolveToPlain("stored-id")).thenReturn("plain-id");set(recommendation,"adminUserService",users);set(recommendation,"contractMapper",contracts);set(recommendation,"idCardCipherService",cipher);}
 @Test void cannotRecommendForAnotherIdentity()throws Exception{recommendation();assertThrows(AccessDeniedException.class,()->recommendation.guessYouLike("from","other-id"));verifyNoInteractions(contracts);}
 @Test void ownRecommendationKeepsLegacyFieldsAndEncryptsHistoricalPlaintext()throws Exception{recommendation();ContractGuessRespVO v=new ContractGuessRespVO();v.setTargetName("Synthetic Target");v.setTargetIdCard("historical-plain");when(contracts.guessDebtor("stored-id","plain-id")).thenReturn(v);when(cipher.requestToStoredCipher("historical-plain")).thenReturn("encrypted-target");when(cipher.maskFromStored("historical-plain")).thenReturn("110101********001X");assertSame(v,recommendation.guessYouLike("from","own-id"));assertEquals("Synthetic Target",v.getTargetName());assertEquals("encrypted-target",v.getTargetIdCard());assertEquals("110101********001X",v.getTargetIdCardDisplay());}
 AdminUserServiceImpl profiles;AdminUserMapper userMapper;UserProfileUpdateReqVO profile;
 void profile()throws Exception{profiles=new AdminUserServiceImpl();userMapper=mock(AdminUserMapper.class);cipher=mock(IdCardCipherService.class);when(userMapper.selectById(42L)).thenReturn(user());when(userMapper.update(any(AdminUserDO.class),any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(1);set(profiles,"userMapper",userMapper);set(profiles,"idCardCipherService",cipher);profile=new UserProfileUpdateReqVO();}
 @Test void verifiedIdentityChangeIsDeniedBeforeDatabaseWrite()throws Exception{profile();profile.setRealname("Other Person");assertThrows(AccessDeniedException.class,()->profiles.updateUserProfile(42L,profile));verify(userMapper,never()).updateById(any(AdminUserDO.class));}
 @Test void verifiedIdentityUnchangedCipherFromReleasedClientStillSaves()throws Exception{profile();profile.setRealname("Test Person");profile.setIdNo("old-client-cipher");when(cipher.sameIdCard("old-client-cipher","stored-id")).thenReturn(true);when(cipher.resolveToPlain("old-client-cipher")).thenReturn("plain-id");profiles.updateUserProfile(42L,profile);verify(userMapper).updateById(any(AdminUserDO.class));}
 @Test void verifiedUserCanSaveOtherProfileFieldsWithoutIdentity()throws Exception{profile();profile.setAddress("Synthetic address");profiles.updateUserProfile(42L,profile);verify(userMapper).updateById(any(AdminUserDO.class));}
 @Test void unverifiedUserCanCompleteIdentityBeforeFirstFaceAuth()throws Exception{profile();AdminUserDO u=user();u.setVerified(0);when(userMapper.selectById(42L)).thenReturn(u);profile.setIdNo("new-identity");profile.setRealname("New Person");when(cipher.resolveToPlain("new-identity")).thenReturn("new-identity");profiles.updateUserProfile(42L,profile);verify(userMapper).update(any(AdminUserDO.class),any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));}
 FaceAuthSessionGuard face;StringRedisTemplate redis;ValueOperations<String,String> ops;FaceAuthCallbackTokenStore legacy;
 void face()throws Exception{face=new FaceAuthSessionGuard();users=mock(AdminUserService.class);cipher=mock(IdCardCipherService.class);redis=mock(StringRedisTemplate.class);ops=mock(ValueOperations.class);legacy=mock(FaceAuthCallbackTokenStore.class);when(redis.opsForValue()).thenReturn(ops);when(users.getUser(42L)).thenReturn(user());when(cipher.sameIdCard("stored-id","stored-id")).thenReturn(true);when(cipher.normalizeRequestToCipher("stored-id")).thenReturn("cipher-id");when(cipher.resolveToPlain("stored-id")).thenReturn("plain-id");set(face,"users",users);set(face,"cipher",cipher);set(face,"redis",redis);set(face,"legacy",legacy);}
 @Test void faceCannotStartForAnotherIdentity()throws Exception{face();assertThrows(AccessDeniedException.class,()->face.requireIdentity("other-id"));}
 @Test void faceCannotPollAnotherUsersToken()throws Exception{face();when(ops.get(anyString())).thenReturn("1:99:other");assertThrows(AccessDeniedException.class,()->face.requireToken("foreign-token"));verifyNoInteractions(legacy);}
 @Test void previousReleaseOwnPendingSessionCanBeAdopted()throws Exception{face();when(legacy.get("plain-id")).thenReturn("legacy-own-token");face.requireToken("legacy-own-token");verify(ops).set(startsWith("face:verify:owner:"),startsWith("1:42:"),eq(30L),eq(TimeUnit.MINUTES));}
 @Test void unknownTokenIsDeniedWithoutCallingSupplier()throws Exception{face();assertThrows(AccessDeniedException.class,()->face.requireToken("unknown-token"));}
 @Test void defaultProductionAutotestCannotRunEvenForAdministrator()throws Exception{AutoTestServiceImpl svc=new AutoTestServiceImpl();SecurityFrameworkService ss=mock(SecurityFrameworkService.class);set(svc,"securityFrameworkService",ss);when(ss.hasAnyRoles("super_admin","tenant_admin")).thenReturn(true);assertThrows(AccessDeniedException.class,svc::runFullTest);}
 @Test void ordinaryRoleIsNotMistakenForAdministrator()throws Exception{AutoTestServiceImpl svc=new AutoTestServiceImpl();set(svc,"securityFrameworkService",mock(SecurityFrameworkService.class));assertFalse(svc.hasAdminRole());}
 @Test void deviceManagementRequiresRoleEvenIfOldMenuStillGrantsPermission()throws Exception{for(String m:new String[]{"setAccess","updateAccess","page","get","delete"}){Method method=java.util.Arrays.stream(DeviceModuleAccessController.class.getMethods()).filter(x->x.getName().equals(m)).findFirst().get();String expression=method.getAnnotation(PreAuthorize.class).value();SecurityFrameworkService ss=mock(SecurityFrameworkService.class);when(ss.hasPermission(anyString())).thenReturn(true);org.springframework.expression.spel.support.StandardEvaluationContext c=new org.springframework.expression.spel.support.StandardEvaluationContext();c.setBeanResolver((ctx,name)->ss);assertFalse(new org.springframework.expression.spel.standard.SpelExpressionParser().parseExpression(expression).getValue(c,Boolean.class));}}

 CustomDefineServiceImpl flow;ContractPaymentAccessGuard paymentGuard;PayDemoOrderMapper demos;PayOrderNotifyReqDTO notice;ContractDO contract;
 void flow()throws Exception{
  flow=new CustomDefineServiceImpl();paymentGuard=mock(ContractPaymentAccessGuard.class);demos=mock(PayDemoOrderMapper.class);custom=mock(CustomDefineMapper.class);contracts=mock(ContractMapper.class);ownership=mock(ContractOwnershipGuard.class);
  orders=mock(PayOrderService.class);users=mock(AdminUserService.class);
  set(flow,"paymentAccessGuard",paymentGuard);set(flow,"payOrderService",orders);set(flow,"userService",users);set(flow,"payDemoOrderMapper",demos);set(flow,"customDefineMapper",custom);set(flow,"contractMapper",contracts);set(flow,"ownershipGuard",ownership);
  set(flow,"paymentNotifyCutoverTime",java.time.LocalDateTime.MIN);
  order=new PayOrderDO();order.setId(7L);order.setStatus(cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum.SUCCESS.getStatus());order.setUserId(42L);order.setUserType(2);order.setPrice(100);order.setMerchantOrderId("11");order.setCreateTime(java.time.LocalDateTime.of(2026,10,22,19,0));when(paymentGuard.requireOrder(7L)).thenReturn(order);when(orders.getOrder(7L)).thenReturn(order);when(users.getUser(42L)).thenReturn(user());
  PayDemoOrderDO demo=new PayDemoOrderDO();demo.setId(11L);demo.setSpuId(8L);demo.setUserId(42L);demo.setPayOrderId(7L);demo.setPrice(100);
  when(demos.selectOne(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(demo);when(demos.selectById(11L)).thenReturn(demo);
  when(custom.selectContractByPayOrderId(7L)).thenReturn(8L);contract=new ContractDO();contract.setId(8L);contract.setStatus(1);contract.setTenantId(1L);when(contracts.selectById(8L)).thenReturn(contract);
  when(contracts.update(any(ContractDO.class),any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(1);
  notice=new PayOrderNotifyReqDTO();notice.setPayOrderId(7L);notice.setMerchantOrderId("11");
 }
 @Test void oldGetPayOrderResponseStillContainsIdAndPayOrderId()throws Exception{flow();PayOrderVO input=new PayOrderVO();input.setId(11L);PayOrderVO out=flow.getPayOrder(input);assertEquals(11L,out.getId());assertEquals(7L,out.getPayOrderId());verify(paymentGuard).requireOrder(7L);}
 @Test void cannotUseOldPaymentLookupToBypassAuthorization()throws Exception{flow();doThrow(new AccessDeniedException("denied")).when(paymentGuard).requireOrder(7L);PayOrderVO input=new PayOrderVO();input.setId(11L);assertThrows(AccessDeniedException.class,()->flow.getPayOrder(input));}
 @Test void unpaidOrderCannotConfirmContract()throws Exception{flow();order.setStatus(0);assertThrows(AccessDeniedException.class,()->flow.updateContractConfirmedStatus(notice));verify(contracts,never()).update(any(ContractDO.class),any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));verify(demos,never()).updateByIdAndPayed(anyLong(),anyBoolean(),any(PayDemoOrderDO.class));}
 @Test void genuinePaidOrderCanConfirmWithoutLogin()throws Exception{flow();SecurityContextHolder.clearContext();assertEquals(1,flow.updateContractConfirmedStatus(notice));verify(contracts).update(any(ContractDO.class),any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));verify(demos).updateByIdAndPayed(eq(11L),eq(false),any(PayDemoOrderDO.class));}
 @Test void paidNotificationCannotMixMerchantOrderIds()throws Exception{flow();notice.setMerchantOrderId("wrong");assertThrows(AccessDeniedException.class,()->flow.updateContractConfirmedStatus(notice));}
 @Test void paidNotificationCannotCrossTenant()throws Exception{flow();TenantContextHolder.setTenantId(2L);assertThrows(AccessDeniedException.class,()->flow.updateContractConfirmedStatus(notice));verify(demos,never()).updateByIdAndPayed(anyLong(),anyBoolean(),any(PayDemoOrderDO.class));}
 @Test void oldNotificationDoesNotChangeHistoricalContractOrOrder()throws Exception{flow();order.setCreateTime(java.time.LocalDateTime.of(2026,10,22,18,25));set(flow,"paymentNotifyCutoverTime",java.time.LocalDateTime.of(2026,10,22,19,0));assertEquals(1,flow.updateContractConfirmedStatus(notice));verifyNoInteractions(contracts);verify(demos,never()).updateByIdAndPayed(anyLong(),anyBoolean(),any(PayDemoOrderDO.class));}
 @Test void confirmationIsIdempotentAndDoesNotReviveClosedContract()throws Exception{flow();contract.setStatus(2);assertEquals(1,flow.updateContractConfirmedStatus(notice));verify(demos).updateByIdAndPayed(eq(11L),eq(false),any(PayDemoOrderDO.class));contract.setStatus(4);assertThrows(AccessDeniedException.class,()->flow.updateContractConfirmedStatus(notice));verify(contracts,never()).update(any(ContractDO.class),any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));}
 @Test void unrelatedQrCannotBeReplacedOrCleared()throws Exception{flow();doThrow(new AccessDeniedException("denied")).when(ownership).checkOrThrow(contract,42L);assertThrows(AccessDeniedException.class,()->flow.bingQrcode(8L,"https://example.com/qr.png"));assertThrows(AccessDeniedException.class,()->flow.deleteQrcode(8L,""));verify(custom,never()).updatePayRelation(anyLong(),anyString());}
 @Test void ownQrStillAcceptsReleasedClientsHttpImageUrl()throws Exception{flow();flow.bingQrcode(8L,"https://example.com/qr.png");verify(custom).updatePayRelation(8L,"https://example.com/qr.png");}
 @Test void qrRejectsExecutableSchemes()throws Exception{flow();assertThrows(IllegalArgumentException.class,()->flow.bingQrcode(8L,"javascript:alert(1)"));verify(custom,never()).updatePayRelation(anyLong(),anyString());}

 @Test void ordinaryFeedbackQueryIsScopedToCallerWhileAdminKeepsTenantView()throws Exception{
  cn.iocoder.yudao.module.custom.service.feedback.FeedbackServiceImpl svc=new cn.iocoder.yudao.module.custom.service.feedback.FeedbackServiceImpl();
  cn.iocoder.yudao.module.custom.dal.mysql.feedback.FeedbackMapper mapper=mock(cn.iocoder.yudao.module.custom.dal.mysql.feedback.FeedbackMapper.class);
  SecurityFrameworkService ss=mock(SecurityFrameworkService.class);set(svc,"feedbackMapper",mapper);set(svc,"securityFrameworkService",ss);
  cn.iocoder.yudao.framework.common.pojo.PageParam page=new cn.iocoder.yudao.framework.common.pojo.PageParam();svc.getFeedbackPage(page);
  org.mockito.ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.Wrapper> c=org.mockito.ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.Wrapper.class);
  verify(mapper).selectPage(eq(page),c.capture());assertTrue(c.getValue().getSqlSegment().contains("user_id"));
  assertTrue(((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper)c.getValue()).getParamNameValuePairs().containsValue(42L));
  clearInvocations(mapper);when(ss.hasAnyRoles("super_admin","tenant_admin")).thenReturn(true);svc.getFeedbackPage(page);verify(mapper).selectPage(eq(page),c.capture());assertFalse(c.getValue().getSqlSegment().contains("user_id"));
 }

 @Test void otpAndRecommendationSecretsNeverReachJsonAccessLogs(){String clean=cn.iocoder.yudao.framework.apilog.core.filter.SensitiveLogSanitizer.sanitize("{\"code\":\"synthetic-secret\",\"verifyCode\":\"synthetic-secret\",\"targetName\":\"synthetic-secret\",\"targetIdCard\":\"synthetic-secret\",\"contactInfo\":\"synthetic-secret\",\"safeId\":42}");assertFalse(clean.contains("synthetic-secret"));assertTrue(clean.contains("safeId"));}

 @Test void unverifiedSelfDeclaredIdentityDoesNotGrantPartyAccess()throws Exception{
  ContractOwnershipGuard guard=new ContractOwnershipGuard();AdminUserService us=mock(AdminUserService.class);IdCardCipherService ci=mock(IdCardCipherService.class);
  cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper roles=mock(cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper.class);
  cn.iocoder.yudao.module.system.service.permission.RoleService rs=mock(cn.iocoder.yudao.module.system.service.permission.RoleService.class);
  when(roles.selectListByUserId(42L)).thenReturn(java.util.Collections.emptyList());when(rs.getRoleList(anyCollection())).thenReturn(java.util.Collections.emptyList());
  AdminUserDO u=user();u.setVerified(0);when(us.getUser(42L)).thenReturn(u);when(ci.sameIdCard(anyString(),anyString())).thenReturn(true);
  set(guard,"adminUserService",us);set(guard,"idCardCipherService",ci);set(guard,"userRoleMapper",roles);set(guard,"roleService",rs);
  ContractDO c=new ContractDO();c.setCreator("99");c.setIndebtedId("stored-id");c.setCreditorId("other-id");assertFalse(guard.isAuthorized(c,42L));
  u.setVerified(1);assertTrue(guard.isAuthorized(c,42L));u.setVerified(0);c.setCreator("42");assertTrue(guard.isAuthorized(c,42L));
 }
 @Test void faceCompletionRacePreventsAnOldIdentityChangeFromOverwritingVerifiedProfile()throws Exception{
  profile();AdminUserDO u=user();u.setVerified(0);when(userMapper.selectById(42L)).thenReturn(u);profile.setRealname("Changed Person");
  when(userMapper.update(any(AdminUserDO.class),any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(0);
  assertThrows(AccessDeniedException.class,()->profiles.updateUserProfile(42L,profile));verify(userMapper,never()).updateById(any(AdminUserDO.class));
 }
 @Test void unverifiedIdentityCannotObtainContactRecommendations()throws Exception{recommendation();AdminUserDO u=user();u.setVerified(0);when(users.getUser(42L)).thenReturn(u);assertThrows(AccessDeniedException.class,()->recommendation.guessYouLike("from","own-id"));verifyNoInteractions(contracts);}

 @Test void directCreditDetailCannotBypassExplicitCreditAuthorization()throws Exception{
  recommendation();cn.iocoder.yudao.module.custom.service.custom.CreditQueryAccessChecker checker=mock(cn.iocoder.yudao.module.custom.service.custom.CreditQueryAccessChecker.class);
  ContractTeaseGuard tease=mock(ContractTeaseGuard.class);set(recommendation,"creditQueryAccessChecker",checker);set(recommendation,"teaseGuard",tease);
  cn.iocoder.yudao.module.custom.service.contract.ContractQueryScope scope=new cn.iocoder.yudao.module.custom.service.contract.ContractQueryScope();set(scope,"creditQueryAccessChecker",checker);set(recommendation,"contractQueryScope",scope);
  when(cipher.normalizeRequestToCipher("target")).thenReturn("normalized");
  cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO input=new cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO();input.setCreditorId("target");
  assertThrows(AccessDeniedException.class,()->recommendation.getContractPageForListing(input));verify(contracts,never()).selectPage(any(cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO.class));
 }
}
