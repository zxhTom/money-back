package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import cn.iocoder.yudao.framework.apilog.core.filter.SensitiveLogSanitizer;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.controller.admin.sms.SmsChannelController;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.channel.*;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsChannelDO;
import cn.iocoder.yudao.module.system.dal.mysql.sms.SmsChannelMapper;
import cn.iocoder.yudao.module.system.service.sms.*;
import cn.iocoder.yudao.module.system.framework.sms.core.client.*;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.yudao.module.infra.controller.admin.config.ConfigController;
import cn.iocoder.yudao.module.infra.controller.admin.logger.*;
import cn.iocoder.yudao.module.infra.service.config.ConfigService;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.service.logger.ApiAccessLogServiceImpl;
import cn.iocoder.yudao.module.infra.dal.mysql.logger.ApiAccessLogMapper;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.PayChannelController;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import org.junit.jupiter.api.*;import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.lang.reflect.*;import java.util.*;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;

class ConfigSmsLogSecurityTest {
 static void inject(Object o,String n,Object v)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 @AfterEach void cleanup(){TenantContextHolder.clear();}
 @Test void managementAndGlobalLogPermissionsAloneNeverBypassAdministratorBoundary(){
  SecurityFrameworkService ss=mock(SecurityFrameworkService.class);StandardEvaluationContext ctx=new StandardEvaluationContext();ctx.setBeanResolver((c,n)->ss);when(ss.hasPermission(anyString())).thenReturn(true);int checked=0;
  for(Class<?> type:Arrays.asList(ConfigController.class,SmsChannelController.class,ApiAccessLogController.class,ApiErrorLogController.class))for(Method method:type.getDeclaredMethods()){
   PreAuthorize policy=method.getAnnotation(PreAuthorize.class);if(policy==null)continue;checked++;
   when(ss.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(false);assertEquals(false,new SpelExpressionParser().parseExpression(policy.value()).getValue(ctx),method.toString());
   when(ss.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(true);assertEquals(true,new SpelExpressionParser().parseExpression(policy.value()).getValue(ctx));when(ss.hasPermission(anyString())).thenReturn(false);assertEquals(false,new SpelExpressionParser().parseExpression(policy.value()).getValue(ctx));when(ss.hasPermission(anyString())).thenReturn(true);
  }assertTrue(checked>=18);
 }
 @Test void visibleConfigReadsKeepExistingBehaviorButHiddenValuesStayHidden()throws Exception{
  assertNull(ConfigController.class.getMethod("getConfigKey",String.class).getAnnotation(PreAuthorize.class));ConfigController controller=new ConfigController();ConfigService configs=mock(ConfigService.class);inject(controller,"configService",configs);ConfigDO item=new ConfigDO();item.setVisible(true);item.setValue("fixture-public-title");when(configs.getConfigByKey("fixture")).thenReturn(item);assertEquals("fixture-public-title",controller.getConfigKey("fixture").getData());item.setVisible(false);assertThrows(RuntimeException.class,()->controller.getConfigKey("fixture"));
 }
 SmsChannelDO channel(){SmsChannelDO c=new SmsChannelDO();c.setId(99L);c.setSignature("fixture-brand");c.setApiKey("fixture-original-key");c.setApiSecret("fixture-original-secret");c.setCode("ALIYUN");c.setStatus(0);return c;}
 @Test void smsDetailAndPageMaskBothCredentialsWithoutChangingStoredEntities()throws Exception{
  SmsChannelDO stored=channel();SmsChannelService service=mock(SmsChannelService.class);when(service.getSmsChannel(99L)).thenReturn(stored);when(service.getSmsChannelPage(any())).thenReturn(new PageResult<>(Collections.singletonList(stored),1L));SmsChannelController controller=new SmsChannelController();inject(controller,"smsChannelService",service);
  for(Object result:Arrays.asList(controller.getSmsChannel(99L),controller.getSmsChannelPage(new SmsChannelPageReqVO()))){String json=JsonUtils.toJsonString(result);assertFalse(json.contains("fixture-original-key"));assertFalse(json.contains("fixture-original-secret"));assertTrue(json.contains("********"));assertTrue(json.contains("fixture-brand"));}
  assertEquals("fixture-original-secret",stored.getApiSecret());assertEquals("fixture-original-key",stored.getApiKey());when(service.getSmsChannel(99L)).thenReturn(null);assertNull(controller.getSmsChannel(99L).getData());
 }
 SmsChannelSaveReqVO change(){SmsChannelSaveReqVO input=new SmsChannelSaveReqVO();input.setId(99L);input.setSignature("new-brand");input.setCode("ALIYUN");input.setStatus(0);return input;}
 @Test void missingBlankAndMaskedSmsCredentialsKeepOriginalValues()throws Exception{
  for(String value:Arrays.asList(null,""," ","********")){SmsChannelServiceImpl service=new SmsChannelServiceImpl();SmsChannelMapper mapper=mock(SmsChannelMapper.class);SmsChannelDO original=channel();when(mapper.selectById(99L)).thenReturn(original);inject(service,"smsChannelMapper",mapper);SmsChannelSaveReqVO input=change();input.setApiKey(value);input.setApiSecret(value);service.updateSmsChannel(input);ArgumentCaptor<SmsChannelDO> saved=ArgumentCaptor.forClass(SmsChannelDO.class);verify(mapper).updateById(saved.capture());assertEquals("fixture-original-key",saved.getValue().getApiKey());assertEquals("fixture-original-secret",saved.getValue().getApiSecret());assertEquals("new-brand",saved.getValue().getSignature());assertEquals("fixture-original-secret",original.getApiSecret());}
 }
 @Test void explicitSmsReplacementIsSavedWithoutChangingOriginalObject()throws Exception{
  SmsChannelServiceImpl service=new SmsChannelServiceImpl();SmsChannelMapper mapper=mock(SmsChannelMapper.class);SmsChannelDO original=channel();when(mapper.selectById(99L)).thenReturn(original);inject(service,"smsChannelMapper",mapper);SmsChannelSaveReqVO input=change();input.setApiKey("replacement-key");input.setApiSecret("replacement-secret");service.updateSmsChannel(input);ArgumentCaptor<SmsChannelDO> saved=ArgumentCaptor.forClass(SmsChannelDO.class);verify(mapper).updateById(saved.capture());assertEquals("replacement-key",saved.getValue().getApiKey());assertEquals("replacement-secret",saved.getValue().getApiSecret());assertEquals("fixture-original-secret",original.getApiSecret());
 }
 @Test void newSmsChannelsRequireRealKeyWhileOptionalSecretRemainsSupported()throws Exception{
  SmsChannelServiceImpl service=new SmsChannelServiceImpl();SmsChannelMapper mapper=mock(SmsChannelMapper.class);inject(service,"smsChannelMapper",mapper);for(String value:Arrays.asList(null,""," ","********")){SmsChannelSaveReqVO input=change();input.setApiKey(value);assertThrows(RuntimeException.class,()->service.createSmsChannel(input));}verifyNoInteractions(mapper);SmsChannelSaveReqVO input=change();input.setApiKey("new-provider-key");service.createSmsChannel(input);verify(mapper).insert(any(SmsChannelDO.class));
 }
 @Test void blankUpdatePassesBeanValidationAndRuntimeSmsClientsStillGetRealCredentials()throws Exception{
  try(javax.validation.ValidatorFactory factory=javax.validation.Validation.buildDefaultValidatorFactory()){assertTrue(factory.getValidator().validate(change()).isEmpty());}
  SmsChannelServiceImpl service=new SmsChannelServiceImpl();SmsChannelMapper mapper=mock(SmsChannelMapper.class);SmsClientFactory clients=mock(SmsClientFactory.class);inject(service,"smsChannelMapper",mapper);inject(service,"smsClientFactory",clients);when(mapper.selectById(99L)).thenReturn(channel());service.getSmsClient(99L);ArgumentCaptor<SmsChannelProperties> properties=ArgumentCaptor.forClass(SmsChannelProperties.class);verify(clients).createOrUpdateSmsClient(properties.capture());assertEquals("fixture-original-key",properties.getValue().getApiKey());assertEquals("fixture-original-secret",properties.getValue().getApiSecret());
 }
 @Test void allSensitiveConfigurationWritesDisableRequestAndResponseLogging(){
  int checked=0;for(Class<?> type:Arrays.asList(ConfigController.class,SmsChannelController.class,PayChannelController.class))for(Method method:type.getDeclaredMethods()){
   if(!(method.getName().startsWith("create")||method.getName().startsWith("update")))continue;checked++;ApiAccessLog policy=method.getAnnotation(ApiAccessLog.class);assertNotNull(policy,method.toString());assertFalse(policy.requestEnable());assertFalse(policy.responseEnable());
  }assertEquals(6,checked);
 }
 @Test void credentialFieldInventoryIsRemovedRecursivelyWithoutDroppingPublicOrBusinessKeys(){
  String json="{\"mchKey\":\"fixture-mch\",\"apiV3Key\":\"fixture-v3\",\"privateKeyContent\":\"fixture-private\",\"keyContent\":\"fixture-p12\",\"encryptKey\":\"fixture-aes\",\"apiSecret\":\"fixture-sms\",\"appSecret\":\"fixture-app\",\"futureClientSecret\":\"fixture-future\",\"publicKeyContent\":\"public-content\",\"moduleKey\":\"business-module\",\"value\":5}";
  String safe=SensitiveLogSanitizer.sanitize(JsonUtils.toJsonString(Collections.singletonMap("body",json)));assertFalse(safe.contains("fixture-"));assertTrue(safe.contains("public-content"));assertTrue(safe.contains("business-module"));assertTrue(safe.contains("value"));
 }
 @Test void sensitiveGenericKeyValueConfigurationsNeverReturnValueInLogs(){
  for(String field:Arrays.asList("key","configKey"))for(String key:Arrays.asList("system.user.init-password","audit.app-secret","wx.api-key","service.private-key","auth.access-token")){
   Map<String,Object> item=new HashMap<>();item.put(field,key);item.put("value","fixture-config-secret");assertFalse(SensitiveLogSanitizer.sanitize(JsonUtils.toJsonString(item)).contains("fixture-config-secret"));
  }assertTrue(SensitiveLogSanitizer.sanitize("{\"key\":\"business.title\",\"value\":\"public-title\"}").contains("public-title"));
 }
 @Test void originalPaymentLogExploitIsBlockedOnWriteAndHistoricalRead()throws Exception{
  WxPayClientConfig config=new WxPayClientConfig();config.setMchKey("fixture-mch");config.setApiV3Key("fixture-v3");config.setPrivateKeyContent("fixture-private");config.setKeyContent("fixture-p12");PayChannelUpdateReqVO input=new PayChannelUpdateReqVO();input.setConfig(JsonUtils.toJsonString(config));String original=JsonUtils.toJsonString(Collections.singletonMap("body",JsonUtils.toJsonString(input)));
  ApiAccessLogServiceImpl service=new ApiAccessLogServiceImpl();ApiAccessLogMapper mapper=mock(ApiAccessLogMapper.class);inject(service,"apiAccessLogMapper",mapper);TenantContextHolder.setTenantId(1L);ApiAccessLogCreateReqDTO dto=new ApiAccessLogCreateReqDTO();dto.setRequestParams(original);service.createApiAccessLog(dto);ArgumentCaptor<ApiAccessLogDO> saved=ArgumentCaptor.forClass(ApiAccessLogDO.class);verify(mapper).insert(saved.capture());assertFalse(saved.getValue().getRequestParams().contains("fixture-"));
  ApiAccessLogDO historical=new ApiAccessLogDO();historical.setRequestParams(original);when(mapper.selectPage(any(ApiAccessLogPageReqVO.class))).thenReturn(new PageResult<>(Collections.singletonList(historical),1L));assertFalse(service.getApiAccessLogPage(new ApiAccessLogPageReqVO()).getList().get(0).getRequestParams().contains("fixture-"));
 }
}
