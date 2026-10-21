package cn.iocoder.yudao.module.system.service.oauth2;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.*;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.*;
import cn.iocoder.yudao.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenLifecycleSecurityTest {
    private OAuth2TokenServiceImpl service;
    private OAuth2AccessTokenMapper access;
    private OAuth2RefreshTokenMapper refresh;
    private OAuth2AccessTokenRedisDAO cache;
    private AdminUserService users;
    private AdminUserDO user;
    private OAuth2AccessTokenDO token;
    private OAuth2RefreshTokenDO renewal;

    @BeforeEach void setup() throws Exception {
        service=new OAuth2TokenServiceImpl();access=mock(OAuth2AccessTokenMapper.class);
        refresh=mock(OAuth2RefreshTokenMapper.class);cache=mock(OAuth2AccessTokenRedisDAO.class);
        users=mock(AdminUserService.class);OAuth2ClientService clients=mock(OAuth2ClientService.class);
        inject("oauth2AccessTokenMapper",access);inject("oauth2RefreshTokenMapper",refresh);
        inject("oauth2AccessTokenRedisDAO",cache);inject("adminUserService",users);inject("oauth2ClientService",clients);
        TenantContextHolder.setTenantId(1L);
        user=new AdminUserDO();user.setId(42L);user.setTenantId(1L);user.setStatus(0);user.setNickname("fixture");
        when(users.getUserSimple(42L)).thenReturn(user);when(users.getUser(42L)).thenReturn(user);
        token=new OAuth2AccessTokenDO().setId(10L).setUserId(42L).setUserType(2).setClientId("default")
                .setAccessToken("synthetic-access").setRefreshToken("synthetic-refresh").setExpiresTime(LocalDateTime.now().plusHours(1));token.setTenantId(1L);
        renewal=new OAuth2RefreshTokenDO().setId(20L).setUserId(42L).setUserType(2).setClientId("default")
                .setRefreshToken("synthetic-refresh").setExpiresTime(LocalDateTime.now().plusDays(1));renewal.setTenantId(1L);
        when(cache.get("synthetic-access")).thenReturn(token);
        when(refresh.selectByRefreshToken("synthetic-refresh")).thenReturn(renewal);
        when(clients.validOAuthClientFromCache("default")).thenReturn(new OAuth2ClientDO().setClientId("default")
                .setAccessTokenValiditySeconds(3600).setRefreshTokenValiditySeconds(7200));
    }
    @AfterEach void cleanup() {TenantContextHolder.clear();}
    private void inject(String name,Object value) throws Exception {Field f=OAuth2TokenServiceImpl.class.getDeclaredField(name);f.setAccessible(true);f.set(service,value);}
    private void noTokenIssued() {verify(access,never()).insert(any(OAuth2AccessTokenDO.class));verify(refresh,never()).insert(any(OAuth2RefreshTokenDO.class));}

    @Test void disabledAccountCannotReceiveTokenFromAlternateLogin() {
        user.setStatus(1);assertThrows(ServiceException.class,() -> service.createAccessToken(42L,2,"default",null));noTokenIssued();
    }
    @Test void disabledAccountCannotRefresh() {
        user.setStatus(1);assertThrows(ServiceException.class,() -> service.refreshAccessToken("synthetic-refresh","default"));noTokenIssued();
        verify(access,never()).deleteByIds(anyCollection());
    }
    @Test void cachedAccessTokenCannotBypassDisabledStatus() {
        user.setStatus(1);assertThrows(ServiceException.class,() -> service.checkAccessToken("synthetic-access"));
    }
    @Test void deletedAccountCannotUseCachedAccessToken() {
        when(users.getUserSimple(42L)).thenReturn(null);assertThrows(ServiceException.class,() -> service.checkAccessToken("synthetic-access"));
    }
    @Test void enabledAccountKeepsLoginAndRefresh() {
        assertEquals(42L,service.createAccessToken(42L,2,"default",Collections.singletonList("read")).getUserId());
        assertEquals(42L,service.refreshAccessToken("synthetic-refresh","default").getUserId());
        assertSame(token,service.checkAccessToken("synthetic-access"));
    }
    @Test void accountLookupUsesTokenTenantAndRestoresRequestTenant() {
        TenantContextHolder.setTenantId(2L);
        when(users.getUserSimple(42L)).thenAnswer(i -> {assertEquals(1L,TenantContextHolder.getTenantId());return user;});
        assertSame(token,service.checkAccessToken("synthetic-access"));assertEquals(2L,TenantContextHolder.getTenantId());
    }
    @Test void tenantMismatchFailsClosed() {
        user.setTenantId(2L);assertThrows(ServiceException.class,() -> service.checkAccessToken("synthetic-access"));
    }
    @Test void machineAndMemberTokensKeepTheirExistingAuthenticationPath() {
        assertEquals(0L,service.createAccessToken(0L,2,"default",null).getUserId());
        token.setUserType(1);assertSame(token,service.checkAccessToken("synthetic-access"));
        verify(users,never()).getUserSimple(anyLong());
    }
    @Test void validRefreshBearerStillWorksWithoutCachingTheAlias() {
        OAuth2AccessTokenDO alias=service.checkAccessToken("synthetic-refresh");
        assertEquals(42L,alias.getUserId());assertEquals("synthetic-refresh",alias.getAccessToken());
        verify(cache,never()).set(any(OAuth2AccessTokenDO.class));
    }
    @Test void previouslyCachedRefreshCannotAuthenticateAfterRevocation() {
        token.setAccessToken("synthetic-refresh");when(cache.get("synthetic-refresh")).thenReturn(token);
        when(refresh.selectByRefreshToken("synthetic-refresh")).thenReturn(null);
        assertThrows(ServiceException.class,() -> service.checkAccessToken("synthetic-refresh"));
        verify(cache).delete("synthetic-refresh");
    }
    @Test void resettingAccountRevokesRefreshRecordsEvenWithoutAnyAccessRows() {
        when(access.selectListByUserId(42L,2)).thenReturn(Collections.emptyList());
        service.removeAllTokensByUserId(42L,2);verify(refresh).deleteByUserId(42L,2);
    }
    @Test void resettingAccountEvictsAccessAndRefreshAliases() {
        when(access.selectListByUserId(42L,2)).thenReturn(Collections.singletonList(token));
        service.removeAllTokensByUserId(42L,2);
        verify(cache).deleteList(org.mockito.ArgumentMatchers.argThat(c -> c.contains("synthetic-access") && c.contains("synthetic-refresh")));
        verify(refresh).deleteByUserId(42L,2);
    }
    @Test void logoutUsingRefreshBearerRevokesItsAccessSession() {
        when(access.selectListByRefreshToken("synthetic-refresh")).thenReturn(Collections.singletonList(token));
        assertEquals(42L,service.removeAccessToken("synthetic-refresh").getUserId());
        verify(access).deleteByIds(Collections.singleton(10L));verify(refresh).deleteByRefreshToken("synthetic-refresh");
        verify(cache).deleteList(org.mockito.ArgumentMatchers.argThat(c -> c.contains("synthetic-access") && c.contains("synthetic-refresh")));
    }
    @Test void logoutUsingAccessBearerAlsoEvictsRefreshAlias() {
        when(access.selectByAccessToken("synthetic-access")).thenReturn(token);
        when(access.selectListByRefreshToken("synthetic-refresh")).thenReturn(Collections.singletonList(token));
        service.removeAccessToken("synthetic-access");verify(refresh).deleteByRefreshToken("synthetic-refresh");
        verify(cache).deleteList(org.mockito.ArgumentMatchers.argThat(c -> c.size()==2));
    }
    @Test void refreshedTokenCannotAdoptCallerSuppliedTenant() {
        TenantContextHolder.setTenantId(2L);
        assertEquals(1L,service.refreshAccessToken("synthetic-refresh","default").getTenantId());
        assertEquals(2L,TenantContextHolder.getTenantId());
    }

}
