package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.infra.controller.admin.file.FileConfigController;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigSaveReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.*;
import cn.iocoder.yudao.module.infra.dal.mysql.file.*;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import cn.iocoder.yudao.module.infra.service.file.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.lang.reflect.*;
import java.time.Duration;
import java.util.*;
import javax.validation.Validation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileManagementBoundaryTest {
    static void inject(Object target,String field,Object value)throws Exception {Field f=target.getClass().getDeclaredField(field);f.setAccessible(true);f.set(target,value);}
    void login(long actor,long tenant) {SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new LoginUser().setId(actor).setUserType(2),null));TenantContextHolder.setTenantId(tenant);}
    FileMapper mapper;FileServiceImpl service;SecurityFrameworkService security;FileConfigService configs;FileClient client;FileContentMapper content;
    @BeforeEach void setup()throws Exception {
        login(42,1);mapper=mock(FileMapper.class);security=mock(SecurityFrameworkService.class);configs=mock(FileConfigService.class);client=mock(FileClient.class);content=mock(FileContentMapper.class);
        service=new FileServiceImpl();inject(service,"fileMapper",mapper);inject(service,"security",security);inject(service,"fileConfigService",configs);inject(service,"privateContents",content);
        when(configs.getMasterFileClient()).thenReturn(client);when(configs.getFileClient(28L)).thenReturn(client);when(client.getId()).thenReturn(28L);
        when(mapper.selectScopedFiles(anyCollection(),any(),any())).thenReturn(Collections.emptyList());
    }
    @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
    S3FileClientConfig config() {
        S3FileClientConfig c=new S3FileClientConfig();c.setEndpoint("https://storage.example.invalid");c.setBucket("fixture");c.setAccessKey("fixture-access-key");c.setAccessSecret("fixture-secret");c.setEnablePathStyleAccess(true);c.setEnablePublicAccess(true);return c;
    }
    @Test void commonPermissionsAloneNeverAuthorizeConfigManagementButAdminRolesKeepPermissions() {
        StandardEvaluationContext ctx=new StandardEvaluationContext();ctx.setBeanResolver((c,name)->security);when(security.hasPermission(anyString())).thenReturn(true);
        int count=0;
        for(Method m:FileConfigController.class.getDeclaredMethods()) {
            PreAuthorize a=m.getAnnotation(PreAuthorize.class);if(a==null)continue;count++;
            when(security.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(false);assertEquals(false,new SpelExpressionParser().parseExpression(a.value()).getValue(ctx));
            when(security.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(true);assertEquals(true,new SpelExpressionParser().parseExpression(a.value()).getValue(ctx));
            when(security.hasPermission(anyString())).thenReturn(false);assertEquals(false,new SpelExpressionParser().parseExpression(a.value()).getValue(ctx));when(security.hasPermission(anyString())).thenReturn(true);
        }
        assertEquals(8,count);
    }
    @Test void masterConfigIdRemainsAvailableWithoutConfigManagementRole()throws Exception {assertNull(FileConfigController.class.getMethod("getMasterFileConfigId").getAnnotation(PreAuthorize.class));}
    @Test void secretsAreMaskedWithoutMutatingPersistentConfig() {
        S3FileClientConfig c=config();Map<String,Object> masked=FileConfigSecretPolicy.masked(c);
        assertEquals("********",masked.get("accessKey"));assertEquals("********",masked.get("accessSecret"));assertEquals(c.getEndpoint(),masked.get("endpoint"));
        assertFalse(JsonUtils.toJsonString(masked).contains("fixture-secret"));assertEquals("fixture-secret",c.getAccessSecret());assertEquals("fixture-access-key",c.getAccessKey());
    }
    @Test void blankMissingAndMaskUpdatesKeepCredentialsButExplicitReplacementWorks() {
        for(Object v:Arrays.asList(null,"","  ","********")) {
            Map<String,Object> input=FileConfigSecretPolicy.asMap(config());input.put("accessSecret",v);input.remove("accessKey");Map<String,Object> merged=FileConfigSecretPolicy.mergeUpdate(config(),input);
            assertEquals("fixture-secret",merged.get("accessSecret"));assertEquals("fixture-access-key",merged.get("accessKey"));assertEquals(v,input.get("accessSecret"));
        }
        Map<String,Object> input=new HashMap<>();input.put("accessSecret","replacement");assertEquals("replacement",FileConfigSecretPolicy.mergeUpdate(config(),input).get("accessSecret"));
    }
    @Test void configControllerSerializesOnlyMaskedCredentials()throws Exception {
        FileConfigController controller=new FileConfigController();inject(controller,"fileConfigService",configs);when(configs.getFileConfig(28L)).thenReturn(new FileConfigDO().setId(28L).setStorage(20).setConfig(config()));
        String response=JsonUtils.toJsonString(controller.getFileConfig(28L));assertTrue(response.contains("********"));assertFalse(response.contains("fixture-secret"));assertFalse(response.contains("fixture-access-key"));
    }
    @Test void realUpdateParserPreservesCredentialsWhileChangingMetadata()throws Exception {
        FileConfigServiceImpl svc=new FileConfigServiceImpl();FileConfigMapper files=mock(FileConfigMapper.class);inject(svc,"fileConfigMapper",files);
        try(javax.validation.ValidatorFactory factory=Validation.buildDefaultValidatorFactory()) {
            inject(svc,"validator",factory.getValidator());when(files.selectById(28L)).thenReturn(new FileConfigDO().setId(28L).setStorage(20).setConfig(config()));
            FileConfigSaveReqVO input=new FileConfigSaveReqVO();input.setId(28L);input.setName("updated");input.setStorage(20);Map<String,Object> values=FileConfigSecretPolicy.masked(config());values.put("accessSecret","");input.setConfig(values);svc.updateFileConfig(input);
            ArgumentCaptor<FileConfigDO> captured=ArgumentCaptor.forClass(FileConfigDO.class);verify(files).updateById(captured.capture());S3FileClientConfig saved=(S3FileClientConfig)captured.getValue().getConfig();assertEquals("fixture-secret",saved.getAccessSecret());assertEquals("fixture-access-key",saved.getAccessKey());assertEquals("updated",captured.getValue().getName());
        }
    }
    @Test void nestedStorageCredentialsNeverEnterApiLogs() {
        String input="{\"config\":{\"accessKey\":\"fixture-access-key\",\"accessSecret\":\"fixture-secret\",\"password\":\"fixture-ftp\",\"endpoint\":\"fixture.invalid\"}}";
        String cleaned=cn.iocoder.yudao.framework.apilog.core.filter.SensitiveLogSanitizer.sanitize(input);
        assertFalse(cleaned.contains("fixture-secret"));assertFalse(cleaned.contains("fixture-access-key"));assertFalse(cleaned.contains("fixture-ftp"));assertTrue(cleaned.contains("fixture.invalid"));
    }
    FileUploadGrantService grants()throws Exception {
        StringRedisTemplate redis=mock(StringRedisTemplate.class);ValueOperations<String,String> values=mock(ValueOperations.class);Map<String,String> state=new HashMap<>();when(redis.opsForValue()).thenReturn(values);
        doAnswer(i->{assertEquals(Duration.ofMinutes(30),i.getArgument(2));state.put(i.getArgument(0),i.getArgument(1));return null;}).when(values).set(anyString(),anyString(),any(Duration.class));
        when(values.get(anyString())).thenAnswer(i->state.get(i.getArgument(0)));when(redis.delete(anyString())).thenAnswer(i->state.remove(i.getArgument(0))!=null);
        FileUploadGrantService grants=new FileUploadGrantService();inject(grants,"redis",redis);inject(service,"uploadGrants",grants);return grants;
    }
    @Test void grantsBindExactActorTenantConfigPathAndCanonicalUrl()throws Exception {
        FileUploadGrantService grant=grants();grant.issue(28L,"avatar/a.png","https://storage.example.invalid/avatar/a.png?synthetic=signature");
        assertEquals("https://storage.example.invalid/avatar/a.png",grant.verify(28L,"avatar/a.png","https://storage.example.invalid/avatar/a.png"));
        assertThrows(RuntimeException.class,()->grant.verify(29L,"avatar/a.png","https://storage.example.invalid/avatar/a.png"));assertThrows(RuntimeException.class,()->grant.verify(28L,"avatar/b.png","https://storage.example.invalid/avatar/a.png"));assertThrows(RuntimeException.class,()->grant.verify(28L,"avatar/a.png","https://evil.invalid/a.png"));
        login(777,1);assertThrows(RuntimeException.class,()->grant.verify(28L,"avatar/a.png","https://storage.example.invalid/avatar/a.png"));login(42,2);assertThrows(RuntimeException.class,()->grant.verify(28L,"avatar/a.png","https://storage.example.invalid/avatar/a.png"));
        login(42,1);grant.complete(28L,"avatar/a.png");assertThrows(RuntimeException.class,()->grant.verify(28L,"avatar/a.png","https://storage.example.invalid/avatar/a.png"));
    }
    @Test void genuinePresignAndRegisterWorksWithExistingClientFieldsAndRandomPath()throws Exception {
        grants();when(client.presignPutUrl(anyString())).thenAnswer(i->"https://storage.example.invalid/"+i.getArgument(0)+"?put=fixture");when(client.presignGetUrl(anyString(),isNull())).thenAnswer(i->"https://storage.example.invalid/"+i.getArgument(0)+"?get=fixture");
        FilePresignedUrlRespVO result=service.presignPutUrl("avatar.png","avatar");assertTrue(result.getPath().startsWith("avatar/"));assertTrue(result.getPath().endsWith(".png"));assertNotEquals("avatar/avatar.png",result.getPath());
        FileCreateReqVO input=new FileCreateReqVO();input.setConfigId(result.getConfigId());input.setPath(result.getPath());input.setUrl(result.getUrl());input.setName("avatar.png");service.createFile(input);
        ArgumentCaptor<FileDO> captured=ArgumentCaptor.forClass(FileDO.class);verify(mapper).insert(captured.capture());assertFalse(captured.getValue().getUrl().contains("?"));assertThrows(RuntimeException.class,()->service.createFile(input));verify(mapper,times(1)).insert(any(FileDO.class));
    }
    @Test void forgedAndEncodedPrivateRegistrationIsRejectedBeforeAnyWrite()throws Exception {
        grants();for(String url:Arrays.asList("/admin-api/infra/file/28/get/private/contracts/777/fixture.pdf","/admin-api/infra/file/28/get/%2570rivate%252Fcontracts%252F777%252Ffixture.pdf")) {
            FileCreateReqVO input=new FileCreateReqVO();input.setConfigId(28L);input.setPath("public/fixture.pdf");input.setUrl(url);assertThrows(RuntimeException.class,()->service.createFile(input));
        }verifyNoInteractions(mapper);
    }
    @Test void unissuedPublicRegistrationIsRejectedBeforeAnyWrite()throws Exception {grants();FileCreateReqVO input=new FileCreateReqVO();input.setConfigId(28L);input.setPath("avatar/chosen.png");input.setUrl("https://storage.example.invalid/avatar/chosen.png");assertThrows(RuntimeException.class,()->service.createFile(input));verifyNoInteractions(mapper);}
    FileDO row(long id,String owner,String path) {FileDO f=new FileDO().setId(id).setConfigId(28L).setPath(path).setUrl("/admin-api/infra/file/28/get/private/contracts/777/foreign.pdf");f.setCreator(owner);return f;}
    @Test void historicalPublicRowsCannotBecomePrivateDownloadLinksAndPrivatePathsUseTheirOwnMetadata() {
        FileDO forged=row(1,"42","public/fixture.pdf"), own=row(2,"42","private/contracts/42/own file.pdf"), wrongOwner=row(3,"42","private/contracts/777/foreign.pdf");
        when(mapper.selectScopedPage(any(),eq(42L),eq(1L))).thenReturn(new PageResult<>(Arrays.asList(forged,own,wrongOwner),3L));service.getFilePage(new FilePageReqVO());assertNull(forged.getUrl());assertNull(wrongOwner.getUrl());assertEquals("/admin-api/infra/file/28/get/private/contracts/42/own%20file.pdf",own.getUrl());
    }
    @Test void responseSignerNeverSignsPrivateUrlOnPublicMetadata()throws Exception {
        cn.iocoder.yudao.module.custom.framework.security.config.ContractAttachmentResponseAdvice advice=new cn.iocoder.yudao.module.custom.framework.security.config.ContractAttachmentResponseAdvice();
        cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentAccessInterceptor signer=mock(cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentAccessInterceptor.class);inject(advice,"attachments",signer);
        javax.servlet.http.HttpServletRequest request=mock(javax.servlet.http.HttpServletRequest.class);when(request.getRequestURI()).thenReturn("/admin-api/infra/file/page");
        FileRespVO row=new FileRespVO();row.setPath("public/fixture.pdf");row.setConfigId(28L);row.setUrl("/admin-api/infra/file/28/get/private/contracts/777/foreign.pdf");
        advice.beforeBodyWrite(cn.iocoder.yudao.framework.common.pojo.CommonResult.success(new PageResult<>(Collections.singletonList(row),1L)),null,org.springframework.http.MediaType.APPLICATION_JSON,null,new org.springframework.http.server.ServletServerHttpRequest(request),null);
        assertNull(row.getUrl());verifyNoInteractions(signer);
    }
    @Test void ordinarySingleDeletionRejectsForeignOwnerBeforeStorageAccess() {FileDO foreign=row(7,"777","private/contracts/777/a.pdf");when(mapper.selectById(7L)).thenReturn(foreign);assertThrows(RuntimeException.class,()->service.deleteFile(7L));verifyNoInteractions(configs,content,client);verify(mapper,never()).deleteById(anyLong());}
    @Test void ownDeletionStillWorks()throws Exception {FileDO own=row(7,"42","avatar/a.png");when(mapper.selectById(7L)).thenReturn(own);when(mapper.selectScopedFiles(Collections.singletonList(7L),42L,1L)).thenReturn(Collections.singletonList(own));service.deleteFile(7L);verify(client).delete("avatar/a.png");verify(mapper).deleteById(7L);}
    @Test void mixedBatchChecksAllRowsBeforeDeletingAnything() {FileDO own=row(7,"42","avatar/a.png"),foreign=row(8,"777","avatar/b.png");when(mapper.selectByIds(anyCollection())).thenReturn(Arrays.asList(own,foreign));when(mapper.selectScopedFiles(Arrays.asList(7L,8L),42L,1L)).thenReturn(Collections.singletonList(own));assertThrows(RuntimeException.class,()->service.deleteFileList(Arrays.asList(7L,8L)));verifyNoInteractions(configs,client,content);verify(mapper,never()).deleteByIds(anyCollection());}
    @Test void tenantManagersKeepSameTenantDeletionButCannotDeleteAcrossTenants()throws Exception {when(security.hasAnyRoles("tenant_admin","contract-manager")).thenReturn(true);FileDO foreign=row(7,"777","avatar/a.png");when(mapper.selectById(7L)).thenReturn(foreign);assertThrows(RuntimeException.class,()->service.deleteFile(7L));verifyNoInteractions(client);when(mapper.selectScopedFiles(Collections.singletonList(7L),null,1L)).thenReturn(Collections.singletonList(foreign));service.deleteFile(7L);verify(client).delete("avatar/a.png");}
    @Test void platformAdminsKeepGlobalBatchDeletionAndDuplicateIdsDoNotFail() {when(security.hasAnyRoles("super_admin")).thenReturn(true);when(mapper.selectByIds(anyCollection())).thenReturn(Collections.singletonList(row(7,"777","avatar/a.png")));assertDoesNotThrow(()->service.deleteFileList(Arrays.asList(7L,7L)));verify(mapper,never()).selectScopedFiles(anyCollection(),any(),any());}
    @Test void anonymousAndMissingTenantDeletionFailClosed() {when(mapper.selectById(7L)).thenReturn(row(7,"42","avatar/a.png"));SecurityContextHolder.clearContext();assertThrows(RuntimeException.class,()->service.deleteFile(7L));login(42,1);TenantContextHolder.clear();assertThrows(RuntimeException.class,()->service.deleteFile(7L));verifyNoInteractions(configs,client,content);}
}
