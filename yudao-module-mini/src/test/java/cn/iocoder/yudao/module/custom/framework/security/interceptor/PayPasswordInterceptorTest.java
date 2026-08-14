package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.iocoder.yudao.module.custom.service.contract.ContractTeaseGuard;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PayPasswordInterceptorTest {

    private static final long USER_ID = 1L;
    private static final String BUSINESS_PATH = "/custom/contract/page";

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RoleService roleService;
    @Mock
    private ContractTeaseGuard contractTeaseGuard;

    @InjectMocks
    private PayPasswordInterceptor interceptor;

    private AdminUserDO user(Boolean changed) {
        AdminUserDO u = new AdminUserDO();
        u.setId(USER_ID);
        u.setPayPasswordChanged(changed);
        return u;
    }

    private void givenEndUser() {
        UserRoleDO ur = new UserRoleDO();
        ur.setUserId(USER_ID);
        ur.setRoleId(9L);
        when(userRoleMapper.selectListByUserId(USER_ID)).thenReturn(Collections.singletonList(ur));
        when(roleService.hasAnySuperAdmin(any())).thenReturn(false);
        RoleDO role = new RoleDO();
        role.setId(9L);
        role.setCode("contract");
        when(roleService.getRoleList(any())).thenReturn(Collections.singletonList(role));
    }

    @Test
    public void testNotLoggedInAllowed() {
        assertTrue(interceptor.isAllowed(null, BUSINESS_PATH));
    }

    @Test
    public void testChangedUserAllowed() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(true));
        assertTrue(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    /** 核心行为：没改过支付密码的终端用户被拦 */
    @Test
    public void testUnchangedEndUserBlocked() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(false));
        givenEndUser();
        assertFalse(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    /**
     * 死锁保护：改支付密码接口本身必须永远放行，
     * 否则被拦的用户没有任何途径去把密码改掉。
     */
    @Test
    public void testUpdatePayPasswordEndpointAlwaysAllowed() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(false));
        givenEndUser();
        assertTrue(interceptor.isAllowed(USER_ID, "/custom/contract/dashboard/update-pay-password"));
        assertTrue(interceptor.isAllowed(USER_ID, "/system/auth/logout"));
        assertTrue(interceptor.isAllowed(USER_ID, "/system/user/profile/get"));
    }

    /** 后台管理员不能被锁死 */
    @Test
    public void testSuperAdminAllowed() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(false));
        UserRoleDO ur = new UserRoleDO();
        ur.setUserId(USER_ID);
        ur.setRoleId(1L);
        when(userRoleMapper.selectListByUserId(USER_ID)).thenReturn(Collections.singletonList(ur));
        when(roleService.hasAnySuperAdmin(any())).thenReturn(true);
        assertTrue(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    @Test
    public void testNonEndUserRoleAllowed() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(false));
        UserRoleDO ur = new UserRoleDO();
        ur.setUserId(USER_ID);
        ur.setRoleId(9L);
        when(userRoleMapper.selectListByUserId(USER_ID)).thenReturn(Collections.singletonList(ur));
        when(roleService.hasAnySuperAdmin(any())).thenReturn(false);
        RoleDO role = new RoleDO();
        role.setId(9L);
        role.setCode("operator");
        when(roleService.getRoleList(any())).thenReturn(Collections.singletonList(role));
        assertTrue(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    @Test
    public void testNoRolesAllowed() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(false));
        when(userRoleMapper.selectListByUserId(USER_ID)).thenReturn(Collections.<UserRoleDO>emptyList());
        assertTrue(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    @Test
    public void testTeasingUserAllowed() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(false));
        when(contractTeaseGuard.isTeasing(USER_ID)).thenReturn(true);
        assertTrue(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    @Test
    public void testMissingUserAllowed() {
        when(adminUserService.getUser(anyLong())).thenReturn(null);
        assertTrue(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    /** null 视同未修改（存量用户由迁移回填为 true，不会落到这里） */
    @Test
    public void testNullFlagTreatedAsUnchanged() {
        when(adminUserService.getUser(USER_ID)).thenReturn(user(null));
        givenEndUser();
        assertFalse(interceptor.isAllowed(USER_ID, BUSINESS_PATH));
    }

    /** admin-api 前缀要能被正确剥掉，否则豁免路径全部失效 */
    @Test
    public void testNormalizeStripsAdminApiPrefix() {
        List<String> paths = Arrays.asList(
                "/admin-api/custom/contract/dashboard/update-pay-password",
                "/admin-api/custom/contract/dashboard/update-pay-password?x=1");
        for (String raw : paths) {
            assertTrue(interceptor.isAllowed(USER_ID, RealNameAuthInterceptor.normalize(raw)), raw);
        }
    }

}
