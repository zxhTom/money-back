package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.apilog.core.filter.SensitiveLogSanitizer;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.service.contract.*;
import cn.iocoder.yudao.module.custom.service.wechat.impl.MiniUserServiceImpl;
import cn.iocoder.yudao.module.custom.service.wechat.model.CombineUser;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dal.mysql.wechat.MiniUserMapper;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.permission.*;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.*;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.controller.admin.code.QRCodeController;
import cn.iocoder.yudao.module.system.controller.admin.code.vo.QRCodeGenerator;
import cn.iocoder.yudao.module.system.controller.admin.code.vo.QRCodeRequest;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.*;import java.lang.reflect.*;import java.util.concurrent.Semaphore;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RoleWechatQrSecurityTest {
    static void inject(Object o,String n,Object v)throws Exception{Class<?> type=o.getClass();while(type!=null){try{Field f=type.getDeclaredField(n);f.setAccessible(true);f.set(o,v);return;}catch(NoSuchFieldException missing){type=type.getSuperclass();}}throw new NoSuchFieldException(n);}
    @BeforeEach void login(){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new LoginUser().setId(42L).setUserType(2),null));TenantContextHolder.setTenantId(1L);}
    @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
    RoleDO role(String code,Integer status){return new RoleDO().setId(9L).setCode(code).setStatus(status);}
    @Test void disabledAndUnknownManagerStatusNeverAuthorizesForeignContract()throws Exception {
        for(Integer status:Arrays.asList(1,null)) {
            RoleService roles=mock(RoleService.class);UserRoleMapper bindings=mock(UserRoleMapper.class);when(bindings.selectListByUserId(42L)).thenReturn(Collections.singletonList(new UserRoleDO().setRoleId(9L)));when(roles.getRoleList(anyCollection())).thenReturn(Collections.singletonList(role("contract-manager",status)));
            ContractOwnershipGuard guard=new ContractOwnershipGuard();inject(guard,"roleService",roles);inject(guard,"userRoleMapper",bindings);inject(guard,"adminUserService",mock(AdminUserService.class));ContractDO foreign=new ContractDO();foreign.setCreator("777");assertFalse(guard.isAuthorized(foreign,42L));foreign.setCreator("42");assertTrue(guard.isAuthorized(foreign,42L));
        }
    }
    @Test void enabledManagerStillAuthorizesForeignContract()throws Exception {
        RoleService roles=mock(RoleService.class);UserRoleMapper bindings=mock(UserRoleMapper.class);when(bindings.selectListByUserId(42L)).thenReturn(Collections.singletonList(new UserRoleDO().setRoleId(9L)));when(roles.getRoleList(anyCollection())).thenReturn(Collections.singletonList(role("contract-manager",0)));
        ContractOwnershipGuard guard=new ContractOwnershipGuard();inject(guard,"roleService",roles);inject(guard,"userRoleMapper",bindings);inject(guard,"adminUserService",mock(AdminUserService.class));ContractDO foreign=new ContractDO();foreign.setCreator("777");assertTrue(guard.isAuthorized(foreign,42L));
    }
    @Test void listAndExportScopesFollowEnabledManagerStatus()throws Exception {
        for(int status:new int[]{0,1}) {
            RoleService roles=mock(RoleService.class);UserRoleMapper bindings=mock(UserRoleMapper.class);when(bindings.selectListByUserId(42L)).thenReturn(Collections.singletonList(new UserRoleDO().setRoleId(9L)));when(roles.getRoleList(anyCollection())).thenReturn(Arrays.asList(role("contract",0),role("contract-manager",status)));
            ContractServiceImpl service=new ContractServiceImpl();ContractMapper mapper=mock(ContractMapper.class);inject(service,"roleService",roles);inject(service,"userRoleMapper",bindings);inject(service,"contractMapper",mapper);inject(service,"teaseGuard",mock(ContractTeaseGuard.class));when(mapper.selectSelfPage(any(),any())).thenReturn(new PageResult<>(Collections.emptyList(),0L));service.getSelfContractPage(new ContractPageReqVO());verify(mapper).selectSelfPage(any(),eq(status==0?null:"42"));
        }
    }
    @Test void actualSuperAdminCheckerRejectsDisabledRoleAndPreservesEnabledRole() {
        org.springframework.context.ApplicationContext previous=cn.hutool.extra.spring.SpringUtil.getApplicationContext();org.springframework.context.support.GenericApplicationContext context=new org.springframework.context.support.GenericApplicationContext();
        RoleServiceImpl actual=new RoleServiceImpl();RoleServiceImpl proxy=mock(RoleServiceImpl.class);context.getBeanFactory().registerSingleton("roleService",proxy);context.refresh();new cn.hutool.extra.spring.SpringUtil().setApplicationContext(context);
        try {for(Integer status:Arrays.asList(0,1,null)){when(proxy.getRoleFromCache(9L)).thenReturn(role("super_admin",status));assertEquals(Integer.valueOf(0).equals(status),actual.hasAnySuperAdmin(Collections.singleton(9L)));}} finally {new cn.hutool.extra.spring.SpringUtil().setApplicationContext(previous);context.close();}
    }
    @Test void wechatSessionKeyAliasesAndNestedJsonAreRedacted() {
        String raw="{\"session_key\":\"fixture-a\",\"sessionKey\":\"fixture-b\",\"nested\":\"{\\\"session_key\\\":\\\"fixture-c\\\"}\",\"ok\":true}";String safe=SensitiveLogSanitizer.sanitize(raw);for(String secret:Arrays.asList("fixture-a","fixture-b","fixture-c"))assertFalse(safe.contains(secret));assertTrue(safe.contains("ok"));
    }
    @Test void actualWechatBindingDoesNotLogTheSessionResponse()throws Exception {
        cn.iocoder.yudao.module.custom.service.custom.CustomDefineServiceImpl service=new cn.iocoder.yudao.module.custom.service.custom.CustomDefineServiceImpl();
        cn.iocoder.yudao.module.custom.service.wechat.WechatService wx=mock(cn.iocoder.yudao.module.custom.service.wechat.WechatService.class);inject(service,"wechatService",wx);inject(service,"miniUserMapper",mock(MiniUserMapper.class));
        cn.iocoder.yudao.module.custom.dto.Code2SessionResponse response=new cn.iocoder.yudao.module.custom.dto.Code2SessionResponse();response.setOpenid("fixture-openid");response.setSession_key("fixture-session-secret");when(wx.code2Session("fixture-code")).thenReturn(response);
        cn.iocoder.yudao.module.custom.controller.admin.wechat.WechatLoginController.WechatLoginRequest request=new cn.iocoder.yudao.module.custom.controller.admin.wechat.WechatLoginController.WechatLoginRequest();request.setAppId("fixture-app");request.setCode("fixture-code");
        ch.qos.logback.classic.Logger logger=(ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(service.getClass());ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> captured=new ch.qos.logback.core.read.ListAppender<>();captured.start();logger.addAppender(captured);
        try {service.bindmini2user(request);for(ch.qos.logback.classic.spi.ILoggingEvent event:captured.list){assertFalse(event.getFormattedMessage().contains("fixture-session-secret"));assertFalse(event.getFormattedMessage().contains("fixture-openid"));}}finally{logger.detachAppender(captured);captured.stop();}
    }
    MiniUserServiceImpl mini;AdminUserService users;
    void mini()throws Exception {mini=spy(new MiniUserServiceImpl());users=mock(AdminUserService.class);inject(mini,"userService",users);inject(mini,"miniUserMapper",mock(MiniUserMapper.class));inject(mini,"roleService",mock(RoleService.class));inject(mini,"userRoleMapper",mock(UserRoleMapper.class));inject(mini,"deptService",mock(DeptService.class));}
    @Test void newWechatAccountsReceiveIndependentRandomLoginAndPaymentSecrets()throws Exception {
        mini();doReturn(null).when(mini).selectMiniUser(anyString(),anyString());Set<String> secrets=new HashSet<>();
        for(String openId:Arrays.asList("fixture-a","fixture-b")){CombineUser result=mini.selectMiniUserOrInitUserWithPrefix("fixture-app",openId,"fixture-union","wechat");AdminUserDO u=result.getMaltcloud();assertEquals(43,u.getPassword().length());assertEquals(43,u.getPayPassword().length());assertTrue(secrets.add(u.getPassword()));assertTrue(secrets.add(u.getPayPassword()));}verify(users,times(2)).insertUserSimply(any(AdminUserDO.class));
    }
    @Test void existingWechatAccountCredentialsRemainUnchanged()throws Exception {
        mini();MiniUserDo linked=new MiniUserDo();linked.setUserId(777L);linked.setUnionId("fixture-union");doReturn(linked).when(mini).selectMiniUser("fixture-app","fixture-open");AdminUserDO existing=new AdminUserDO();existing.setPassword("existing-hash");existing.setPayPassword("existing-pay-hash");when(users.getUserSimple(777L)).thenReturn(existing);assertSame(existing,mini.selectMiniUserOrInitUserWithPrefix("fixture-app","fixture-open","fixture-union","wechat").getMaltcloud());assertEquals("existing-hash",existing.getPassword());assertEquals("existing-pay-hash",existing.getPayPassword());verify(users,never()).insertUserSimply(any());
    }
    @Test void deletedLinkedUserRecreationAlsoUsesIndependentSecrets()throws Exception {
        mini();MiniUserDo linked=new MiniUserDo();linked.setUserId(777L);linked.setUnionId("fixture-union");doReturn(linked).when(mini).selectMiniUser("fixture-app","fixture-open");AdminUserDO replacement=mini.selectMiniUserOrInitUserWithPrefix("fixture-app","fixture-open","fixture-union","wechat").getMaltcloud();assertEquals(43,replacement.getPassword().length());assertNotEquals(replacement.getPassword(),replacement.getPayPassword());verify(users).insertUserSimply(replacement);
    }
    @Test void invalidQrSizeAndContentRejectedBeforeEncodingOrStorage()throws Exception {
        QRCodeController controller=new QRCodeController();FileService files=mock(FileService.class);cn.binarywang.wx.miniapp.api.WxMaService wx=mock(cn.binarywang.wx.miniapp.api.WxMaService.class);QRCodeGenerator generator=mock(QRCodeGenerator.class);inject(controller,"fileService",files);inject(controller,"wxMaService",wx);inject(controller,"qrCodeGenerator",generator);
        for(int size:new int[]{-1,0,63,1025,Integer.MAX_VALUE}){ServiceException e=assertThrows(ServiceException.class,()->controller.generateQRCodeUrl("fixture",size,300));assertEquals(400,e.getCode());assertThrows(ServiceException.class,()->controller.generateQRCode("fixture",300,size));assertThrows(ServiceException.class,()->controller.generateQRCodeBase64("fixture",size,300));QRCodeRequest request=new QRCodeRequest();request.setContent("fixture");request.setHeight(size);assertThrows(ServiceException.class,()->controller.generateQRCodeWithText(request));}
        for(String content:Arrays.asList(null,"","   ",String.join("",Collections.nCopies(1000,"汉"))))assertThrows(ServiceException.class,()->QRCodeGenerator.validateRequest(content,300,300));verifyNoInteractions(files,wx,generator);
    }
    @Test void normalShareQrStillGeneratesPngWithoutUnusedStorageWrites()throws Exception {
        QRCodeController controller=new QRCodeController();inject(controller,"qrCodeGenerator",new QRCodeGenerator());FileService files=mock(FileService.class);inject(controller,"fileService",files);byte[] png=controller.generateQRCode("https://fixture.invalid/contract",300,300).getData();assertEquals((byte)0x89,png[0]);java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));assertEquals(300,image.getWidth());assertEquals(300,image.getHeight());verifyNoInteractions(files);
    }
    @Test void encoderConcurrencyIsBoundedAndSlotsReleasedAfterFailure()throws Exception {
        Field f=QRCodeGenerator.class.getDeclaredField("ENCODERS");f.setAccessible(true);Semaphore slots=(Semaphore)f.get(null);assertTrue(slots.tryAcquire(4));try{ServiceException e=assertThrows(ServiceException.class,()->new QRCodeGenerator().generateQRCodeImage("fixture",300,300));assertEquals(429,e.getCode());}finally{slots.release(4);}assertThrows(RuntimeException.class,()->new QRCodeGenerator().generateQRCodeImage(String.join("",Collections.nCopies(2000,"x")),300,300));assertEquals(4,slots.availablePermits());assertTrue(new QRCodeGenerator().generateQRCodeImage("fixture",300,300).length>0);
    }
    @Test void allQrEndpointsHaveClientIpRateLimits() {
        int count=0;for(Method method:QRCodeController.class.getDeclaredMethods()){if(!method.getName().startsWith("generateQRCode"))continue;count++;RateLimiter limit=method.getAnnotation(RateLimiter.class);assertNotNull(limit);assertEquals(ClientIpRateLimiterKeyResolver.class,limit.keyResolver());assertEquals(60,limit.time());assertEquals(60,limit.count());}assertEquals(4,count);
    }
}
