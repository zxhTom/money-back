package cn.iocoder.yudao.module.system.service.user;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.*;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.service.dept.*;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserImportSecurityTest {
    private AdminUserServiceImpl service;
    private AdminUserMapper mapper;
    private UserRoleMapper memberships;
    private RoleService roles;
    private AdminUserDO existing;
    private UserImportExcelVO row;

    @BeforeEach void setup() throws Exception {
        service=new AdminUserServiceImpl();mapper=mock(AdminUserMapper.class);
        memberships=mock(UserRoleMapper.class);roles=mock(RoleService.class);
        ConfigApi config=mock(ConfigApi.class);PasswordEncoder encoder=mock(PasswordEncoder.class);
        inject("userMapper",mapper);inject("userRoleMapper",memberships);inject("roleService",roles);
        inject("configApi",config);inject("passwordEncoder",encoder);
        inject("deptService",mock(DeptService.class));inject("postService",mock(PostService.class));
        when(config.getConfigValueByKey(AdminUserServiceImpl.USER_INIT_PASSWORD_KEY)).thenReturn("Test_9527");
        when(encoder.encode(anyString())).thenReturn("synthetic-hash");
        when(memberships.selectListByUserId(anyLong())).thenReturn(Collections.emptyList());
        existing=new AdminUserDO();existing.setId(99L);existing.setUsername("fixture_user");existing.setDeptId(10L);
        when(mapper.selectByUsername("fixture_user")).thenReturn(existing);
        when(mapper.selectById(99L)).thenReturn(existing);
        row=UserImportExcelVO.builder().username("fixture_user").nickname("Fixture")
                .deptId(10L).status(0).sex(1).build();
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(42L).setUserType(2),mock(HttpServletRequest.class));
    }
    @AfterEach void cleanup() {SecurityContextHolder.clearContext();}
    private void inject(String name,Object value) throws Exception {
        Field f=AdminUserServiceImpl.class.getDeclaredField(name);f.setAccessible(true);f.set(service,value);
    }
    private void root(long id) {
        when(memberships.selectListByUserId(id)).thenReturn(Collections.singletonList(new UserRoleDO().setUserId(id).setRoleId(1L)));
        when(roles.hasAnySuperAdmin(Collections.singletonList(1L))).thenReturn(true);
    }
    private UserImportRespVO run() {return service.importUserList(Collections.singletonList(row),true);}
    private void noWrites() {verify(mapper,never()).updateById(any(AdminUserDO.class));verify(mapper,never()).insert(any(AdminUserDO.class));}

    @Test void delegatedImporterCannotOverwriteSuperAdminContactsOrStatus() {
        root(99L);row.setEmail("replacement@example.invalid");row.setStatus(1);
        assertTrue(run().getFailureUsernames().containsKey("fixture_user"));noWrites();
    }
    @Test void delegatedImporterCannotMoveExistingAccountAcrossDepartments() {
        row.setDeptId(20L);assertTrue(run().getFailureUsernames().containsKey("fixture_user"));noWrites();
    }
    @Test void sameDepartmentOrdinaryAccountStillUpdates() {
        assertEquals(Collections.singletonList("fixture_user"),run().getUpdateUsernames());
        verify(mapper).updateById(org.mockito.ArgumentMatchers.<AdminUserDO>argThat(u -> u.getId().equals(99L) && u.getDeptId().equals(10L)));
    }
    @Test void superAdminKeepsRootAccountUpdateAndDepartmentChange() {
        root(42L);root(99L);row.setDeptId(20L);
        assertEquals(Collections.singletonList("fixture_user"),run().getUpdateUsernames());
        verify(mapper).updateById(any(AdminUserDO.class));
    }
    @Test void OmittedDepartmentKeepsExistingValueForOrdinaryImport() {
        row.setDeptId(null);assertEquals(Collections.singletonList("fixture_user"),run().getUpdateUsernames());
        verify(mapper).updateById(org.mockito.ArgumentMatchers.<AdminUserDO>argThat(u -> u.getDeptId()==null));
    }
    @Test void UnchangedOwnContactsDoNotFailUniquenessCheck() {
        row.setEmail("fixture@example.invalid");row.setMobile("13800000000");
        when(mapper.selectByEmail(row.getEmail())).thenReturn(existing);
        when(mapper.selectByMobile(row.getMobile())).thenReturn(existing);
        assertEquals(Collections.singletonList("fixture_user"),run().getUpdateUsernames());
        verify(mapper).updateById(any(AdminUserDO.class));
    }
    @Test void newAccountCreationKeepsInitialPasswordEncoding() {
        when(mapper.selectByUsername("fixture_user")).thenReturn(null);
        assertEquals(Collections.singletonList("fixture_user"),run().getCreateUsernames());
        verify(mapper).insert(org.mockito.ArgumentMatchers.<AdminUserDO>argThat(u -> "synthetic-hash".equals(u.getPassword()) && u.getDeptId().equals(10L)));
    }
    @Test void disabledOverwriteNeverMutatesExistingAccount() {
        assertTrue(service.importUserList(Collections.singletonList(row),false).getFailureUsernames().containsKey("fixture_user"));noWrites();
    }
}
