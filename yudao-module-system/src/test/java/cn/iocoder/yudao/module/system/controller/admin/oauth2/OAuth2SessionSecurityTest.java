package cn.iocoder.yudao.module.system.controller.admin.oauth2;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token.*;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.auth.AdminAuthService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.framework.security.SensitiveRecordAccess;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OAuth2SessionSecurityTest {
    OAuth2TokenController controller;OAuth2TokenService tokens;AdminAuthService auth;OAuth2AccessTokenMapper mapper;
    PermissionService permissions;SecurityFrameworkService security;Map<Long,OAuth2AccessTokenDO> records;
    boolean administrator,root;
    static void inject(Object o,String n,Object v)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
    OAuth2AccessTokenDO row(long id,long user,long tenant,int type){
        OAuth2AccessTokenDO row=new OAuth2AccessTokenDO();row.setId(id);row.setUserId(user);row.setUserType(type);row.setTenantId(tenant);
        row.setAccessToken("SYNTHETIC_ACCESS_"+id);row.setRefreshToken("SYNTHETIC_REFRESH_"+id);records.put(id,row);return row;
    }
    OAuth2AccessTokenPageReqVO page(){OAuth2AccessTokenPageReqVO v=new OAuth2AccessTokenPageReqVO();v.setPageNo(1);v.setPageSize(10);return v;}
    @BeforeEach void setup()throws Exception{
        LoginUser user=new LoginUser();user.setId(42L);user.setUserType(2);user.setTenantId(1L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));TenantContextHolder.setTenantId(1L);
        controller=new OAuth2TokenController();tokens=mock(OAuth2TokenService.class);auth=mock(AdminAuthService.class);mapper=mock(OAuth2AccessTokenMapper.class);
        permissions=mock(PermissionService.class);security=mock(SecurityFrameworkService.class);SensitiveRecordAccess access=new SensitiveRecordAccess();inject(access,"security",security);
        when(security.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenAnswer(i->administrator);
        when(security.hasAnyRoles("super_admin")).thenAnswer(i->root);
        when(permissions.hasAnyRoles(77L,"super_admin")).thenReturn(true);
        inject(controller,"oauth2TokenService",tokens);inject(controller,"authService",auth);inject(controller,"oauth2AccessTokenMapper",mapper);
        inject(controller,"recordAccess",access);inject(controller,"security",security);inject(controller,"permissionService",permissions);
        records=new LinkedHashMap<>();row(101,42,1,2);row(102,99,1,2);row(103,77,1,2);row(104,88,2,2);row(105,42,1,1);
        when(mapper.selectById(anyLong())).thenAnswer(i->records.get(i.getArgument(0)));
        when(tokens.getAccessToken(anyString())).thenAnswer(i->records.values().stream().filter(t->t.getAccessToken().equals(i.getArgument(0))).findFirst().orElse(null));
        when(tokens.getAccessTokenPage(any())).thenAnswer(i->{OAuth2AccessTokenPageReqVO v=i.getArgument(0);
            List<OAuth2AccessTokenDO> list=records.values().stream().filter(t->t.getTenantId().equals(TenantContextHolder.getTenantId())
                    &&(v.getUserId()==null||t.getUserId().equals(v.getUserId()))&&(v.getUserType()==null||t.getUserType().equals(v.getUserType()))).collect(Collectors.toList());
            return new PageResult<>(list,(long)list.size());});
    }
    @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
    @Test void delegatedListIsOwnAdminNamespaceAndNeverContainsCredentials(){
        OAuth2AccessTokenPageReqVO request=page();PageResult<OAuth2AccessTokenRespVO> result=controller.getAccessTokenPage(request).getData();
        assertEquals(42L,request.getUserId());assertEquals(2,request.getUserType());assertEquals(1,result.getTotal());
        assertEquals("session:101",result.getList().get(0).getAccessToken());assertEquals("已隐藏",result.getList().get(0).getRefreshToken());
        assertFalse(JsonUtils.toJsonString(result).contains("SYNTHETIC_"));
        assertEquals("SYNTHETIC_ACCESS_101",records.get(101L).getAccessToken());
    }
    @Test void delegatedCannotSelectForeignUserOrMemberNamespace(){
        OAuth2AccessTokenPageReqVO request=page();request.setUserId(99L);assertThrows(AccessDeniedException.class,()->controller.getAccessTokenPage(request));
        OAuth2AccessTokenPageReqVO member=page();member.setUserType(1);assertThrows(AccessDeniedException.class,()->controller.getAccessTokenPage(member));
        verify(tokens,never()).getAccessTokenPage(any());
    }
    @Test void administratorKeepsAllSessionMetadataWithoutBearerOrRefreshSecrets(){
        administrator=true;PageResult<OAuth2AccessTokenRespVO> result=controller.getAccessTokenPage(page()).getData();
        assertEquals(4,result.getTotal());assertFalse(JsonUtils.toJsonString(result).contains("SYNTHETIC_"));
        assertTrue(result.getList().stream().allMatch(t->t.getAccessToken().startsWith("session:")));
        OAuth2AccessTokenPageReqVO other=page();other.setUserId(99L);assertEquals(1,controller.getAccessTokenPage(other).getData().getTotal());
    }
    @Test void oldFrontendHandleRevokesOwnSession(){
        assertTrue(controller.deleteAccessToken("session:101").getData());verify(auth).logout("SYNTHETIC_ACCESS_101",LoginLogTypeEnum.LOGOUT_DELETE.getType());
    }
    @Test void legacyClientCanStillRevokeOwnRawToken(){
        assertTrue(controller.deleteAccessToken("SYNTHETIC_ACCESS_101").getData());verify(auth).logout("SYNTHETIC_ACCESS_101",LoginLogTypeEnum.LOGOUT_DELETE.getType());
    }
    @Test void delegatedCannotRevokeOtherSessionsThroughEitherFormat(){
        assertThrows(AccessDeniedException.class,()->controller.deleteAccessToken("session:102"));
        assertThrows(AccessDeniedException.class,()->controller.deleteAccessToken("SYNTHETIC_ACCESS_102"));verifyNoInteractions(auth);
    }
    @Test void administratorKeepsOtherUserLogoutButCannotLogoutPlatformAdministrator(){
        administrator=true;assertTrue(controller.deleteAccessToken("session:102").getData());verify(auth).logout("SYNTHETIC_ACCESS_102",LoginLogTypeEnum.LOGOUT_DELETE.getType());
        assertThrows(AccessDeniedException.class,()->controller.deleteAccessToken("session:103"));verify(auth,never()).logout(eq("SYNTHETIC_ACCESS_103"),any());
    }
    @Test void platformAdministratorKeepsPlatformLogoutAndTenantVisitManagement(){
        administrator=true;root=true;controller.deleteAccessToken("session:103");
        verify(auth).logout("SYNTHETIC_ACCESS_103",LoginLogTypeEnum.LOGOUT_DELETE.getType());
        TenantContextHolder.setTenantId(2L);controller.deleteAccessToken("session:104");verify(auth).logout("SYNTHETIC_ACCESS_104",LoginLogTypeEnum.LOGOUT_DELETE.getType());
    }
    @Test void crossTenantSessionIsRejectedEvenIfHandleOrRawTokenIsKnown(){
        administrator=true;root=true;
        assertThrows(AccessDeniedException.class,()->controller.deleteAccessToken("session:104"));
        assertThrows(AccessDeniedException.class,()->controller.deleteAccessToken("SYNTHETIC_ACCESS_104"));verifyNoInteractions(auth);
    }
    @Test void memberSessionWithSameNumericUserIdIsNotOwnedByAdminUser(){
        assertThrows(AccessDeniedException.class,()->controller.deleteAccessToken("session:105"));verifyNoInteractions(auth);
    }
    @Test void invalidHandlesFailWithoutLogout(){
        for(String value:Arrays.asList("session:abc","session:0","session:-1","session:9223372036854775808",""))
            assertEquals(400,assertThrows(ServiceException.class,()->controller.deleteAccessToken(value)).getCode());
        verifyNoInteractions(auth);
    }
    @Test void alreadyOfflineRemainsIdempotent(){
        assertTrue(controller.deleteAccessToken("session:99999").getData());assertTrue(controller.deleteAccessToken("unknown-token").getData());verifyNoInteractions(auth);
    }
    @Test void mixedBatchChecksEveryTargetBeforeAnyLogout(){
        assertThrows(AccessDeniedException.class,()->controller.deleteAccessTokenList(Arrays.asList("session:101","session:102")));verifyNoInteractions(auth);
    }
    @Test void administratorBatchKeepsBothFormatsAndDeduplicates(){
        administrator=true;controller.deleteAccessTokenList(Arrays.asList("session:101","SYNTHETIC_ACCESS_101","session:102"));
        verify(auth).logout("SYNTHETIC_ACCESS_101",LoginLogTypeEnum.LOGOUT_DELETE.getType());verify(auth).logout("SYNTHETIC_ACCESS_102",LoginLogTypeEnum.LOGOUT_DELETE.getType());verifyNoMoreInteractions(auth);
    }
    @Test void delegatedCountsCannotEnumerateOtherAccounts(){
        assertThrows(AccessDeniedException.class,()->controller.batchCountByUserIds(Arrays.asList(99L)));verify(mapper,never()).selectAdminTokenCountsByUserIds(anyCollection());
        controller.batchCountByUserIds(Arrays.asList(42L));verify(mapper).selectAdminTokenCountsByUserIds(new LinkedHashSet<>(Arrays.asList(42L)));
    }
    @Test void ownStatisticsDoNotCallGlobalTopQuery(){
        controller.getUserTokenStats(10);verify(mapper).selectAdminTokenCountsByUserIds(Collections.singleton(42L));verify(mapper,never()).selectTopUsersByTokenCount(anyInt());
    }
    @Test void administratorKeepsStatisticsAndBatchCounts(){
        administrator=true;controller.getUserTokenStats(10);controller.batchCountByUserIds(Arrays.asList(42L,99L));
        verify(mapper).selectTopUsersByTokenCount(10);verify(mapper).selectAdminTokenCountsByUserIds(new LinkedHashSet<>(Arrays.asList(42L,99L)));
    }
    @Test void invalidStatisticsAndOversizedBatchesFailBeforeQueries(){
        assertEquals(400,assertThrows(ServiceException.class,()->controller.getUserTokenStats(0)).getCode());
        assertEquals(400,assertThrows(ServiceException.class,()->controller.getUserTokenStats(101)).getCode());
        assertThrows(ServiceException.class,()->controller.batchCountByUserIds(Collections.nCopies(1001,42L)));
        assertThrows(ServiceException.class,()->controller.deleteAccessTokenList(Collections.nCopies(1001,"session:101")));
        verifyNoInteractions(auth);
    }
    @Test void administrativeBatchCountRetainsTenantInterceptor()throws Exception{
        assertNull(OAuth2AccessTokenMapper.class.getMethod("selectAdminTokenCountsByUserIds",Collection.class)
                .getAnnotation(cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore.class));
    }
    @Test void administrativeBatchCountFiltersUserNamespaceIdsAndExpiry(){
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(new org.apache.ibatis.builder.MapperBuilderAssistant(
                new com.baomidou.mybatisplus.core.MybatisConfiguration(),""),OAuth2AccessTokenDO.class);
        OAuth2AccessTokenMapper scoped=mock(OAuth2AccessTokenMapper.class,org.mockito.Mockito.CALLS_REAL_METHODS);
        org.mockito.ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.Wrapper<OAuth2AccessTokenDO>> query=
                org.mockito.ArgumentCaptor.forClass((Class)com.baomidou.mybatisplus.core.conditions.Wrapper.class);
        doReturn(Arrays.asList(records.get(101L),records.get(101L),records.get(102L))).when(scoped).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        List<UserTokenStatVO> result=scoped.selectAdminTokenCountsByUserIds(Arrays.asList(42L,99L));
        assertEquals(2,result.size());assertEquals(2L,result.get(0).getTokenCount());
        verify(scoped).selectList(query.capture());String sql=query.getValue().getSqlSegment();
        assertTrue(sql.contains("user_id IN"));assertTrue(sql.contains("user_type ="));assertTrue(sql.contains("expires_time >"));
    }
    @Test void temporaryRepairEndpointCannotWriteEvenWhenInvokedDirectly(){
        assertThrows(AccessDeniedException.class,()->new cn.iocoder.yudao.module.system.controller.admin.monitor.FixPermissionController().run());
    }
}
