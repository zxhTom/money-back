package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileServiceImpl;
import cn.iocoder.yudao.module.custom.framework.iconset.util.SvgIconValidator;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.IconSetProfileController;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.WechatLoginController;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeakBoundaryRegressionTest {
    static void inject(Object o,String name,Object v)throws Exception {Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,v);}
    @BeforeEach void login(){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new LoginUser().setId(42L).setUserType(2),null));TenantContextHolder.setTenantId(1L);}
    @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
    @Test void attackerControlledFindInSetValueOnlyAppearsInBoundParameters(){
        String input="example.invalid') OR 'fixture'='fixture";
        for(DbType type:Arrays.asList(DbType.MYSQL,DbType.POSTGRE_SQL,DbType.SQL_SERVER,DbType.DM)){
            QueryWrapper<Object> query=new QueryWrapper<>();query.apply(MyBatisUtils.findInSet(type,"websites"),input);
            String sql=query.getSqlSegment();assertFalse(sql.contains(input));assertTrue(sql.contains("#{ew.paramNameValuePairs."));
            assertEquals(Collections.singleton(input),new HashSet<>(query.getParamNameValuePairs().values()));
            assertFalse(sql.contains("'#{"));
        }
    }
    @Test void ordinaryDomainsAndMailAddressesRemainUnmodifiedParameters(){
        for(String value:Arrays.asList("org.toms.chat","o'connor@example.invalid","a\\b@example.invalid")){
            QueryWrapper<Object> query=new QueryWrapper<>();query.apply(MyBatisUtils.findInSet(DbType.MYSQL,"to_mails"),value);query.getSqlSegment();
            assertTrue(query.getParamNameValuePairs().containsValue(value));
        }
        assertTrue(MyBatisUtils.findInSet(DbType.MYSQL,"t.websites").contains("t.websites"));
    }
    @Test void unsafeColumnsUnsupportedDatabasesAndLegacyStringsFailClosed(){
        assertThrows(IllegalArgumentException.class,()->MyBatisUtils.findInSet(DbType.MYSQL,"websites) OR 1=1"));
        assertThrows(IllegalArgumentException.class,()->MyBatisUtils.findInSet(DbType.H2,"websites"));
        assertThrows(IllegalArgumentException.class,()->MyBatisUtils.findInSet("websites",(Object)"example.invalid"));
    }
    @Test void commonStaticSvgShapesAndLocalGradientReferencesRemainValid(){
        assertTrue(SvgIconValidator.isValid("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\"><path d=\"M1 2L3 4\" fill=\"none\" stroke=\"currentColor\"/></svg>"));
        assertTrue(SvgIconValidator.isValid("<svg><defs><linearGradient id=\"paint\"><stop offset=\"0\" stop-color=\"#fff\"/></linearGradient></defs><rect width=\"10\" height=\"10\" fill=\"url(#paint)\"/></svg>"));
        assertTrue(SvgIconValidator.isValid("/images/avatar.png"));assertTrue(SvgIconValidator.isValid("https://example.invalid/a.png"));
    }
    @Test void svgCannotEmbedHtmlOrExecutableNodesOrExternalReferences(){
        for(String svg:Arrays.asList("<svg><foreignObject><iframe srcdoc=\"&lt;script&gt;1&lt;/script&gt;\"/></foreignObject></svg>","<svg><script>1</script></svg>","<svg><animate attributeName=\"href\"/></svg>","<svg><use href=\"https://example.invalid/a.svg#x\"/></svg>","<svg><path style=\"fill:red\"/></svg>","<svg onload=\"1\"></svg>"))assertFalse(SvgIconValidator.isValid(svg),svg);
    }
    @Test void svgRejectsDoctypeInvalidXmlAndExcessiveNesting(){
        assertFalse(SvgIconValidator.isValid("<!DOCTYPE svg SYSTEM 'https://example.invalid/a'><svg/>"));
        assertFalse(SvgIconValidator.isValid("<svg><path></svg>"));
        StringBuilder s=new StringBuilder("<svg>");for(int i=0;i<80;i++)s.append("<g>");for(int i=0;i<80;i++)s.append("</g>");s.append("</svg>");assertFalse(SvgIconValidator.isValid(s.toString()));
    }
    @Test void ordinaryMenuPermissionsCannotAuthorizeIconMutationButAdminsKeepThem()throws Exception{
        SecurityFrameworkService ss=mock(SecurityFrameworkService.class);when(ss.hasPermission(anyString())).thenReturn(true);
        StandardEvaluationContext context=new StandardEvaluationContext();context.setBeanResolver((ctx,name)->ss);
        for(Method m:IconSetProfileController.class.getMethods()){
            if(!Arrays.asList("createIconSetProfile","updateIconSetProfile","deleteIconSetProfile","useIconSetProfile","cloneIconSetProfile").contains(m.getName()))continue;
            String expr=m.getAnnotation(PreAuthorize.class).value();when(ss.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(false);
            assertFalse(new SpelExpressionParser().parseExpression(expr).getValue(context,Boolean.class));
            when(ss.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(true);assertTrue(new SpelExpressionParser().parseExpression(expr).getValue(context,Boolean.class));
        }
    }
    FileMapper files;SecurityFrameworkService security;FileServiceImpl service;
    void files()throws Exception {files=mock(FileMapper.class);security=mock(SecurityFrameworkService.class);service=new FileServiceImpl();inject(service,"fileMapper",files);inject(service,"security",security);when(files.selectScopedPage(any(),any(),any())).thenReturn(new PageResult<>(Collections.emptyList(),0L));}
    @Test void ordinaryFileQueryIsLimitedToSelfAndTenant()throws Exception{files();FilePageReqVO q=new FilePageReqVO();service.getFilePage(q);verify(files).selectScopedPage(q,42L,1L);}
    @Test void tenantAdministratorKeepsTenantWideFileManagement()throws Exception{files();when(security.hasAnyRoles("tenant_admin","contract-manager")).thenReturn(true);FilePageReqVO q=new FilePageReqVO();service.getFilePage(q);verify(files).selectScopedPage(q,null,1L);}
    @Test void platformAdministratorKeepsGlobalFileManagement()throws Exception{files();when(security.hasAnyRoles("super_admin")).thenReturn(true);FilePageReqVO q=new FilePageReqVO();service.getFilePage(q);verify(files).selectScopedPage(q,null,null);}
    @Test void anonymousOrMissingTenantCannotQueryFiles()throws Exception{files();SecurityContextHolder.clearContext();assertThrows(RuntimeException.class,()->service.getFilePage(new FilePageReqVO()));verifyNoInteractions(files);login();TenantContextHolder.clear();assertThrows(RuntimeException.class,()->service.getFilePage(new FilePageReqVO()));verifyNoInteractions(files);}
    @Test void fileMapperBindsOwnerAndTenantWithoutTrustingUserFilters(){
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),"leak-file-test"),FileDO.class);
        FileMapper mapper=mock(FileMapper.class,CALLS_REAL_METHODS);
        doAnswer(invocation->{AbstractWrapper query=invocation.getArgument(1);String sql=query.getSqlSegment();assertTrue(sql.contains("creator"));assertTrue(sql.contains("tenant_id"));assertTrue(query.getParamNameValuePairs().containsValue("42"));assertTrue(query.getParamNameValuePairs().containsValue(1L));return new PageResult<FileDO>(Collections.emptyList(),0L);}).when(mapper).selectPage(any(PageParam.class),any(Wrapper.class));
        mapper.selectScopedPage(new FilePageReqVO(),42L,1L);
    }
    @Test void legacyContractUploadDirectoriesAreRemappedButPublicAvatarsAreNot()throws Exception{
        FileServiceImpl svc=new FileServiceImpl();Method method=FileServiceImpl.class.getDeclaredMethod("validateUploadDirectory",String.class);method.setAccessible(true);
        assertEquals("private/contracts/42",method.invoke(svc,"contract"));assertEquals("private/contracts/42",method.invoke(svc,"contracts"));assertEquals("avatar",method.invoke(svc,"avatar"));assertNull(method.invoke(svc,new Object[]{null}));
    }
    @Test void loginResponsesNeverExposeHashOrMutateTheOriginalEntity()throws Exception{
        AdminUserDO source=new AdminUserDO();source.setId(42L);source.setRealname("Synthetic User");source.setIdNo("cipher");source.setPassword("synthetic-login-hash");source.setPayPassword("synthetic-pay-hash");
        AuthLoginRespVO response=AuthLoginRespVO.builder().user(source).accessToken("synthetic-token").build();String json=new ObjectMapper().writeValueAsString(response);
        assertFalse(json.contains("synthetic-login-hash"));assertFalse(json.contains("synthetic-pay-hash"));assertTrue(json.contains("Synthetic User"));assertTrue(json.contains("synthetic-token"));assertEquals("synthetic-login-hash",source.getPassword());assertEquals("synthetic-pay-hash",source.getPayPassword());
        WechatLoginController.WechatLoginResponse wx=new WechatLoginController.WechatLoginResponse("synthetic-token",source,true,"synthetic-openid");assertFalse(new ObjectMapper().writeValueAsString(wx).contains("synthetic-pay-hash"));
    }

    @Test void privateContractBytesNeverEnterPublicObjectStorage()throws Exception {
        FileServiceImpl svc=new FileServiceImpl();
        cn.iocoder.yudao.module.infra.service.file.FileConfigService configs=mock(cn.iocoder.yudao.module.infra.service.file.FileConfigService.class);
        cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient client=mock(cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient.class);
        cn.iocoder.yudao.module.infra.dal.mysql.file.FileContentMapper content=mock(cn.iocoder.yudao.module.infra.dal.mysql.file.FileContentMapper.class);
        FileMapper mapper=mock(FileMapper.class);when(configs.getMasterFileClient()).thenReturn(client);when(client.getId()).thenReturn(28L);
        inject(svc,"fileConfigService",configs);inject(svc,"fileMapper",mapper);inject(svc,"privateContents",content);
        byte[] bytes="%PDF-synthetic-private-fixture".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String url=svc.createFile(bytes,"synthetic.pdf","contract","application/pdf");
        assertTrue(url.startsWith("/admin-api/infra/file/28/get/private/contracts/42/"));
        verify(client,never()).upload(any(byte[].class),anyString(),anyString());
        org.mockito.ArgumentCaptor<cn.iocoder.yudao.module.infra.dal.dataobject.file.FileContentDO> captured=org.mockito.ArgumentCaptor.forClass(cn.iocoder.yudao.module.infra.dal.dataobject.file.FileContentDO.class);
        verify(content).insert(captured.capture());assertArrayEquals(bytes,captured.getValue().getContent());
        verify(mapper).insert(any(FileDO.class));
    }
    @Test void privateDownloadReadsOnlyControlledContentAndNeverFallsBackToS3()throws Exception {
        FileServiceImpl svc=new FileServiceImpl();
        cn.iocoder.yudao.module.infra.dal.mysql.file.FileContentMapper content=mock(cn.iocoder.yudao.module.infra.dal.mysql.file.FileContentMapper.class);
        cn.iocoder.yudao.module.infra.service.file.FileConfigService configs=mock(cn.iocoder.yudao.module.infra.service.file.FileConfigService.class);
        inject(svc,"privateContents",content);inject(svc,"fileConfigService",configs);
        byte[] bytes=new byte[]{1,2,3};
        when(content.selectListByConfigIdAndPath(28L,"private/contracts/42/a.pdf")).thenReturn(Collections.singletonList(new cn.iocoder.yudao.module.infra.dal.dataobject.file.FileContentDO().setId(1L).setContent(bytes)));
        assertArrayEquals(bytes,svc.getFileContent(28L,"private/contracts/42/a.pdf"));verifyNoInteractions(configs);
    }
    @Test void genericDirectUploadAndRegistrationCannotPublishPrivateObjects()throws Exception {
        FileServiceImpl svc=new FileServiceImpl();
        assertThrows(RuntimeException.class,()->svc.presignPutUrl("synthetic.pdf","contract"));
        cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO input=new cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO();input.setPath("private/contracts/42/a.pdf");
        assertThrows(RuntimeException.class,()->svc.createFile(input));
    }
    @Test void deletingPrivateAttachmentsClearsControlledContentInsteadOfPublicStorage()throws Exception {
        FileServiceImpl svc=new FileServiceImpl();FileMapper mapper=mock(FileMapper.class);
        cn.iocoder.yudao.module.infra.service.file.FileConfigService configs=mock(cn.iocoder.yudao.module.infra.service.file.FileConfigService.class);
        cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient client=mock(cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient.class);
        cn.iocoder.yudao.module.infra.dal.mysql.file.FileContentMapper content=mock(cn.iocoder.yudao.module.infra.dal.mysql.file.FileContentMapper.class);
        inject(svc,"fileMapper",mapper);inject(svc,"fileConfigService",configs);inject(svc,"privateContents",content);
        when(mapper.selectById(7L)).thenReturn(new FileDO().setId(7L).setConfigId(28L).setPath("private/contracts/42/a.pdf"));when(configs.getFileClient(28L)).thenReturn(client);
        SecurityFrameworkService ss=mock(SecurityFrameworkService.class);when(ss.hasAnyRoles("super_admin")).thenReturn(true);inject(svc,"security",ss);
        svc.deleteFile(7L);verify(content).deleteByConfigIdAndPath(28L,"private/contracts/42/a.pdf");verify(client,never()).delete(anyString());verify(mapper).deleteById(7L);
    }

    @Test void authorizedFileManagementReceivesUsableSignedPrivateDownloadLinks()throws Exception {
        cn.iocoder.yudao.module.custom.framework.security.config.ContractAttachmentResponseAdvice advice=new cn.iocoder.yudao.module.custom.framework.security.config.ContractAttachmentResponseAdvice();
        cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentAccessInterceptor signer=mock(cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentAccessInterceptor.class);
        inject(advice,"attachments",signer);
        javax.servlet.http.HttpServletRequest request=mock(javax.servlet.http.HttpServletRequest.class);when(request.getRequestURI()).thenReturn("/admin-api/infra/file/page");
        when(signer.signForAuthorizedResponse(anyString(),eq(request))).thenReturn("https://staging.toms.chat/private-fixture?signature=synthetic");
        cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileRespVO row=new cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileRespVO();row.setPath("private/contracts/42/a.pdf");row.setConfigId(28L);row.setUrl("/admin-api/infra/file/28/get/private/contracts/42/a.pdf");
        Object body=cn.iocoder.yudao.framework.common.pojo.CommonResult.success(new PageResult<>(Collections.singletonList(row),1L));
        advice.beforeBodyWrite(body,null,org.springframework.http.MediaType.APPLICATION_JSON,null,new org.springframework.http.server.ServletServerHttpRequest(request),null);
        assertTrue(row.getUrl().contains("signature=synthetic"));verify(signer).signForAuthorizedResponse(anyString(),eq(request));
    }
}
