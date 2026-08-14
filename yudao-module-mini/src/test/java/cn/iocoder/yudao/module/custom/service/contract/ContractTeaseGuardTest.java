package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ContractTeaseGuardTest {

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private RoleService roleService;

    @InjectMocks
    private ContractTeaseGuard guard;

    private AdminUserDO user(Boolean teaseEnabled) {
        AdminUserDO u = new AdminUserDO();
        u.setId(1L);
        u.setTeaseEnabled(teaseEnabled);
        return u;
    }

    private RoleDO role(Long id, Boolean teaseEnabled) {
        RoleDO r = new RoleDO();
        r.setId(id);
        r.setTeaseEnabled(teaseEnabled);
        return r;
    }

    private Set<Long> ids(Long... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    @Test
    public void testNullUserIdNotTeasing() {
        assertFalse(guard.isTeasing(null));
    }

    @Test
    public void testUserFlagStillWorks() {
        when(adminUserService.getUser(1L)).thenReturn(user(true));
        assertTrue(guard.isTeasing(1L));
    }

    /** 本次修复的核心：用户自己没开，但角色开了，也必须命中 */
    @Test
    public void testRoleFlagNowTakesEffect() {
        when(adminUserService.getUser(1L)).thenReturn(user(false));
        when(permissionService.getUserRoleIdListByUserIdFromCache(1L)).thenReturn(ids(10L));
        when(roleService.getRoleListFromCache(any())).thenReturn(Collections.singletonList(role(10L, true)));
        assertTrue(guard.isTeasing(1L));
    }

    @Test
    public void testAnyRoleTeasingIsEnough() {
        when(adminUserService.getUser(1L)).thenReturn(user(false));
        when(permissionService.getUserRoleIdListByUserIdFromCache(1L)).thenReturn(ids(10L, 20L));
        when(roleService.getRoleListFromCache(any()))
                .thenReturn(Arrays.asList(role(10L, false), role(20L, true)));
        assertTrue(guard.isTeasing(1L));
    }

    @Test
    public void testNoFlagAnywhereNotTeasing() {
        when(adminUserService.getUser(1L)).thenReturn(user(false));
        when(permissionService.getUserRoleIdListByUserIdFromCache(1L)).thenReturn(ids(10L));
        when(roleService.getRoleListFromCache(any())).thenReturn(Collections.singletonList(role(10L, false)));
        assertFalse(guard.isTeasing(1L));
    }

    @Test
    public void testNoRolesNotTeasing() {
        when(adminUserService.getUser(1L)).thenReturn(user(false));
        when(permissionService.getUserRoleIdListByUserIdFromCache(1L)).thenReturn(Collections.<Long>emptySet());
        assertFalse(guard.isTeasing(1L));
    }

    @Test
    public void testMissingUserNotTeasing() {
        when(adminUserService.getUser(anyLong())).thenReturn(null);
        assertFalse(guard.isTeasing(1L));
    }

    /** 角色查询炸了要按"不戏耍"降级：宁可给真实数据，也不能把正常用户变成假数据 */
    @Test
    public void testRoleLookupFailureDegradesToNotTeasing() {
        when(adminUserService.getUser(1L)).thenReturn(user(false));
        when(permissionService.getUserRoleIdListByUserIdFromCache(1L))
                .thenThrow(new RuntimeException("redis down"));
        assertFalse(guard.isTeasing(1L));
    }

}
