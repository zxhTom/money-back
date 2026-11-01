package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountImpersonationServiceTest {
    AccountImpersonationService service;
    OAuth2TokenService tokens;
    AdminUserService users;
    PermissionService permissions;
    StringRedisTemplate redis;
    ValueOperations<String, String> values;
    OAuth2AccessTokenDO actor;
    OAuth2AccessTokenDO targetToken;
    AdminUserDO actorUser;
    AdminUserDO targetUser;

    static void inject(Object object, String name, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }

    @BeforeEach void setup() throws Exception {
        TenantContextHolder.setTenantId(1L);
        service = new AccountImpersonationService();
        tokens = mock(OAuth2TokenService.class);
        users = mock(AdminUserService.class);
        permissions = mock(PermissionService.class);
        redis = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        inject(service, "tokenService", tokens);
        inject(service, "userService", users);
        inject(service, "permissionService", permissions);
        inject(service, "redis", redis);
        when(redis.opsForValue()).thenReturn(values);
        actor = new OAuth2AccessTokenDO();
        actor.setUserId(42L); actor.setUserType(UserTypeEnum.ADMIN.getValue());
        actor.setAccessToken("actor-token"); actor.setTenantId(1L); actor.setExpiresTime(LocalDateTime.now().plusHours(1));
        targetToken = new OAuth2AccessTokenDO();
        targetToken.setUserId(99L); targetToken.setUserType(UserTypeEnum.ADMIN.getValue());
        targetToken.setAccessToken("target-token"); targetToken.setTenantId(1L);
        targetToken.setScopes(Collections.singletonList(AccountImpersonationService.SCOPE));
        actorUser = new AdminUserDO();
        actorUser.setId(42L); actorUser.setTenantId(1L); actorUser.setStatus(0); actorUser.setPassword("hash");
        targetUser = new AdminUserDO();
        targetUser.setId(99L); targetUser.setTenantId(1L); targetUser.setStatus(0); targetUser.setUsername("target");
        when(tokens.checkAccessToken("actor-token")).thenReturn(actor);
        when(tokens.getAccessToken("actor-token")).thenReturn(actor);
        when(users.getUserSimple(42L)).thenReturn(actorUser);
        when(users.getUserSimple(99L)).thenReturn(targetUser);
        when(users.isPasswordMatch("password", "hash")).thenReturn(true);
        when(permissions.hasAnyRoles(42L, "super_admin", "tenant_admin")).thenReturn(true);
        when(permissions.hasAnyRoles(42L, "super_admin")).thenReturn(true);
    }

    @AfterEach void cleanup() { TenantContextHolder.clear(); }

    @Test void ordinaryUserCannotSearchOrIssueTargetToken() {
        when(permissions.hasAnyRoles(42L, "super_admin", "tenant_admin")).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.search("actor-token", "target"));
        assertThrows(AccessDeniedException.class, () -> service.start("actor-token", 99L, "password"));
        verify(tokens, never()).createAccessToken(anyLong(), anyInt(), anyString(), anyList());
    }

    @Test void passwordAndTenantAreCheckedBeforeIssuingToken() {
        assertThrows(AccessDeniedException.class, () -> service.start("actor-token", 99L, "wrong"));
        targetUser.setTenantId(2L);
        assertThrows(AccessDeniedException.class, () -> service.start("actor-token", 99L, "password"));
        verify(tokens, never()).createAccessToken(anyLong(), anyInt(), anyString(), anyList());
    }

    @Test void targetTokenStopsWorkingWhenSessionOrAdminTokenDisappears() {
        when(values.get("money:account-impersonation:v1:target-token"))
                .thenReturn("42|1|99|actor-token");
        assertDoesNotThrow(() -> service.validate(targetToken, "target-token"));
        assertEquals("42", targetToken.getUserInfo().get("_impersonatorId"));
        when(tokens.getAccessToken("actor-token")).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> service.validate(targetToken, "target-token"));
        when(values.get("money:account-impersonation:v1:target-token")).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> service.validate(targetToken, "target-token"));
    }
}
