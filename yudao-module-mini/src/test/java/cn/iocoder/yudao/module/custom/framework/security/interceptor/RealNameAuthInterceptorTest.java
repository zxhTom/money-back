package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RealNameAuthInterceptorTest {

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RoleService roleService;

    @InjectMocks
    private RealNameAuthInterceptor interceptor;

    private AdminUserDO user(Integer verified, Boolean tease) {
        AdminUserDO u = new AdminUserDO();
        u.setId(9L);
        u.setVerified(verified);
        u.setTeaseEnabled(tease);
        return u;
    }

    private void givenRoles(long... roleIds) {
        List<UserRoleDO> urs = Arrays.stream(roleIds).mapToObj(id -> {
            UserRoleDO ur = new UserRoleDO();
            ur.setRoleId(id);
            return ur;
        }).collect(java.util.stream.Collectors.toList());
        when(userRoleMapper.selectListByUserId(9L)).thenReturn(urs);
    }

    private RoleDO role(String code) {
        RoleDO r = new RoleDO();
        r.setCode(code);
        return r;
    }

    // ── 放行分支 ────────────────────────────────────────────────
    @Test
    public void testNoLogin_allowed() {
        assertTrue(interceptor.isAllowed(null, "/custom/contract/get"));
    }

    @Test
    public void testExemptEndpoint_allowed() {
        assertTrue(interceptor.isAllowed(9L, "/api/faceAuth/start"));
        assertTrue(interceptor.isAllowed(9L, "/system/user/profile/get"));
        assertTrue(interceptor.isAllowed(9L, "/system/auth/logout"));
    }

    @Test
    public void testTeasePriority_allowedEvenIfNotVerified() {
        when(adminUserService.getUser(9L)).thenReturn(user(0, true));
        assertTrue(interceptor.isAllowed(9L, "/custom/contract/get"));
    }

    @Test
    public void testVerified_allowed() {
        when(adminUserService.getUser(9L)).thenReturn(user(1, false));
        assertTrue(interceptor.isAllowed(9L, "/custom/contract/get"));
    }

    @Test
    public void testUnverifiedSuperAdmin_allowed() {
        when(adminUserService.getUser(9L)).thenReturn(user(0, false));
        givenRoles(1L);
        when(roleService.hasAnySuperAdmin(any())).thenReturn(true);
        assertTrue(interceptor.isAllowed(9L, "/custom/contract/get"));
    }

    @Test
    public void testUnverifiedNonEndUser_allowed() {
        when(adminUserService.getUser(9L)).thenReturn(user(0, false));
        givenRoles(2L);
        when(roleService.hasAnySuperAdmin(any())).thenReturn(false);
        when(roleService.getRoleList(any())).thenReturn(Collections.singletonList(role("common")));
        assertTrue(interceptor.isAllowed(9L, "/custom/contract/get"));
    }

    // ── 拦截分支 ────────────────────────────────────────────────
    @Test
    public void testUnverifiedEndUser_blocked() {
        when(adminUserService.getUser(9L)).thenReturn(user(0, false));
        givenRoles(159L);
        when(roleService.hasAnySuperAdmin(any())).thenReturn(false);
        when(roleService.getRoleList(any())).thenReturn(Collections.singletonList(role("contract")));
        assertFalse(interceptor.isAllowed(9L, "/custom/contract/get"));
    }

    @Test
    public void testNormalize() {
        assertEquals("/custom/contract/get",
                RealNameAuthInterceptor.normalize("/admin-api/custom/contract/get?id=1"));
        assertEquals("/custom/contract/get",
                RealNameAuthInterceptor.normalize("/custom/contract/get"));
    }
}
