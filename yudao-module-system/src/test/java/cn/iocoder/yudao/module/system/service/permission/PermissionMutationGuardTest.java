package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.*;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.*;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionMutationGuardTest {
    PermissionMutationGuard guard;UserRoleMapper userRoles;RoleMenuMapper roleMenus;RoleService roles;AdminUserService users;
    Map<Long,RoleDO> catalog;Map<Long,List<Long>> memberships;Map<Long,AdminUserDO> accounts;Map<Long,Set<Long>> menuMap;
    static void inject(Object o,String n,Object v)throws Exception {Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
    Set<Long> set(Long... ids){return new HashSet<>(Arrays.asList(ids));}
    RoleDO role(long id,String code,int scope,Long... menus){RoleDO r=new RoleDO();r.setId(id);r.setCode(code);r.setStatus(0);r.setTenantId(1L);r.setDataScope(scope);r.setDataScopeDeptIds(set(100L));catalog.put(id,r);menuMap.put(id,set(menus));return r;}
    AdminUserDO account(long id,long tenant){AdminUserDO u=new AdminUserDO();u.setId(id);u.setTenantId(tenant);u.setStatus(0);accounts.put(id,u);return u;}
    List<UserRoleDO> rows(long id){return memberships.getOrDefault(id,Collections.emptyList()).stream().map(role->{UserRoleDO x=new UserRoleDO();x.setUserId(id);x.setRoleId(role);return x;}).collect(Collectors.toList());}
    @BeforeEach void setup()throws Exception {
        LoginUser user=new LoginUser();user.setId(42L);user.setUserType(2);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));TenantContextHolder.setTenantId(1L);
        catalog=new HashMap<>();memberships=new HashMap<>();accounts=new HashMap<>();menuMap=new HashMap<>();
        role(1,"super_admin",1,100L,101L,999L);role(10,"common",2,100L,101L);role(11,"tenant_admin",1,100L,101L);role(20,"contract",5,100L);role(21,"extended",1,100L,999L);
        memberships.put(42L,Arrays.asList(10L));account(42,1);account(99,1);account(77,1);memberships.put(77L,Arrays.asList(1L));
        guard=new PermissionMutationGuard();userRoles=mock(UserRoleMapper.class);roleMenus=mock(RoleMenuMapper.class);roles=mock(RoleService.class);users=mock(AdminUserService.class);
        inject(guard,"userRoles",userRoles);inject(guard,"roleMenus",roleMenus);inject(guard,"roles",roles);inject(guard,"users",users);
        when(userRoles.selectListByUserId(anyLong())).thenAnswer(i->rows(i.getArgument(0)));
        when(userRoles.selectListByRoleIds(anyCollection())).thenAnswer(i->{Collection<Long> ids=i.getArgument(0);List<UserRoleDO> out=new ArrayList<>();memberships.keySet().forEach(uid->rows(uid).stream().filter(x->ids.contains(x.getRoleId())).forEach(out::add));return out;});
        when(roles.getRole(anyLong())).thenAnswer(i->catalog.get(i.getArgument(0)));
        when(roles.getRoleList(anyCollection())).thenAnswer(i->{Collection<Long> ids=i.getArgument(0);return ids.stream().map(catalog::get).filter(Objects::nonNull).collect(Collectors.toList());});
        when(users.getUser(anyLong())).thenAnswer(i->accounts.get(i.getArgument(0)));
        when(roleMenus.selectListByRoleId(anyCollection())).thenAnswer(i->{Collection<Long> ids=i.getArgument(0);List<RoleMenuDO> out=new ArrayList<>();for(Long id:ids)for(Long menu:menuMap.getOrDefault(id,Collections.emptySet())){RoleMenuDO row=new RoleMenuDO();row.setRoleId(id);row.setMenuId(menu);out.add(row);}return out;});
    }
    @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
    @Test void delegatedUserCannotGrantSuperAdministrator(){assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(1L)));}
    @Test void tenantAdministratorCannotGrantSuperAdministrator(){memberships.put(42L,Arrays.asList(11L));assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(1L)));}
    @Test void platformAdministratorCanGrantAndRemoveSuperAdministrator(){memberships.put(42L,Arrays.asList(1L));assertDoesNotThrow(()->guard.userRoles(99L,set(1L)));assertDoesNotThrow(()->guard.userRoles(77L,set()));}
    @Test void administratorCanContinueAssigningOrdinaryRoles(){memberships.put(42L,Arrays.asList(11L));assertDoesNotThrow(()->guard.userRoles(99L,set(20L)));}
    @Test void delegatedOperatorKeepsBoundedOrdinaryAssignments(){assertDoesNotThrow(()->guard.userRoles(99L,set(20L)));}
    @Test void noSelfPromotionForNonSuperAdministrator(){assertThrows(AccessDeniedException.class,()->guard.userRoles(42L,set(10L,20L)));}
    @Test void unchangedSelfRolesRemainUsable(){assertDoesNotThrow(()->guard.userRoles(42L,set(10L)));}
    @Test void delegatedOperatorCannotGrantManagementRole(){assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(11L)));}
    @Test void delegatedOperatorCannotGrantBroaderMenuPermissions(){assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(21L)));}
    @Test void cannotAssignForeignTenantRole(){catalog.get(20L).setTenantId(2L);assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(20L)));}
    @Test void cannotAssignRolesToForeignTenantUser(){account(99,2);assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(20L)));}
    @Test void cannotModifyExistingSuperAdministrator(){assertThrows(AccessDeniedException.class,()->guard.userRoles(77L,set(20L)));}
    @Test void bulkRoleUsersCannotBypassSuperAdministratorProtection(){assertThrows(AccessDeniedException.class,()->guard.roleUsers(1L,set(99L)));}
    @Test void bulkRoleUsersCannotPromoteTheCaller(){assertThrows(AccessDeniedException.class,()->guard.roleUsers(20L,set(42L)));}
    @Test void administratorKeepsBulkOrdinaryAssignments(){memberships.put(42L,Arrays.asList(11L));assertDoesNotThrow(()->guard.roleUsers(20L,set(99L)));}
    @Test void roleMenuEndpointCannotEscalateOwnRole(){assertThrows(AccessDeniedException.class,()->guard.roleMenus(10L,set(100L,101L,999L)));}
    @Test void roleMenuEndpointCannotGrantUnownedMenus(){assertThrows(AccessDeniedException.class,()->guard.roleMenus(20L,set(100L,999L)));}
    @Test void administratorCanConfigureOtherOrdinaryRoleMenus(){memberships.put(42L,Arrays.asList(11L));assertDoesNotThrow(()->guard.roleMenus(20L,set(100L,101L)));}
    @Test void roleMenuNoOpRemainsAllowed(){assertDoesNotThrow(()->guard.roleMenus(10L,set(100L,101L)));}
    @Test void roleDataScopeCannotExpandOwnDepartmentScopeToAll(){assertThrows(AccessDeniedException.class,()->guard.roleDataScope(10L,1,set()));}
    @Test void administratorWithAllDataScopeCanManageOtherRoleScope(){memberships.put(42L,Arrays.asList(11L));assertDoesNotThrow(()->guard.roleDataScope(20L,1,set()));}
    @Test void delegatedUserCannotExpandAnotherRoleBeyondOwnDataScope(){assertThrows(AccessDeniedException.class,()->guard.roleDataScope(20L,1,set()));}
    @Test void missingOrDisabledActorFailsClosed(){catalog.get(10L).setStatus(1);assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(20L)));}
    @Test void anonymousAllMutationPathsFailClosed(){SecurityContextHolder.clearContext();assertThrows(AccessDeniedException.class,()->guard.userRoles(99L,set(20L)));assertThrows(AccessDeniedException.class,()->guard.roleUsers(20L,set(99L)));assertThrows(AccessDeniedException.class,()->guard.roleMenus(20L,set(100L)));assertThrows(AccessDeniedException.class,()->guard.roleDataScope(20L,5,set()));}
    @Test void platformTenantBootstrapStillWorksUnderNewTenantContext(){
        memberships.put(42L,Arrays.asList(1L));TenantContextHolder.setTenantId(2L);RoleDO tenantRole=role(30,"tenant_admin",1,999L);tenantRole.setTenantId(2L);account(99,2);
        assertDoesNotThrow(()->guard.roleMenus(30L,set(999L)));assertDoesNotThrow(()->guard.userRoles(99L,set(30L)));assertFalse(TenantContextHolder.isIgnore());
    }
    @Test void administratorCanKeepAddingBoundedOrdinaryRolesToSelf(){memberships.put(42L,Arrays.asList(11L));assertDoesNotThrow(()->guard.userRoles(42L,set(11L,20L)));}
    @Test void administratorCanKeepSelfInBulkOrdinaryAssignments(){memberships.put(42L,Arrays.asList(11L));assertDoesNotThrow(()->guard.roleUsers(20L,set(42L)));}
    @Test void ownRoleMenuReductionsRemainAllowed(){assertDoesNotThrow(()->guard.roleMenus(10L,set(100L)));}
    @Test void administratorDataScopeNoOpIgnoresIrrelevantDepartmentList(){memberships.put(42L,Arrays.asList(11L));assertDoesNotThrow(()->guard.roleDataScope(11L,1,set()));}
    @Test void springCanWireLazyGuardAndKeepDelegatedAssignments() {
        try (org.springframework.context.annotation.AnnotationConfigApplicationContext context = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            context.registerBean(UserRoleMapper.class, () -> userRoles);
            context.registerBean(RoleMenuMapper.class, () -> roleMenus);
            context.registerBean(RoleService.class, () -> roles);
            context.registerBean(AdminUserService.class, () -> users);
            context.registerBean(MenuService.class, () -> mock(MenuService.class));
            context.registerBean(cn.iocoder.yudao.module.system.service.dept.DeptService.class, () -> mock(cn.iocoder.yudao.module.system.service.dept.DeptService.class));
            context.register(PermissionServiceImpl.class, PermissionMutationGuard.class);
            context.refresh();
            assertDoesNotThrow(() -> context.getBean(PermissionServiceImpl.class).assignUserRole(99L, set(20L)));
            verify(userRoles).insertBatch(anyCollection());
        }
    }
    @Test void allPublicAssignmentMethodsCallGuardBeforeWriting()throws Exception {
        PermissionServiceImpl service=new PermissionServiceImpl();PermissionMutationGuard denied=mock(PermissionMutationGuard.class);inject(service,"mutationGuard",denied);
        doThrow(new AccessDeniedException("blocked")).when(denied).userRoles(anyLong(),anySet());doThrow(new AccessDeniedException("blocked")).when(denied).roleUsers(anyLong(),anySet());doThrow(new AccessDeniedException("blocked")).when(denied).roleMenus(anyLong(),anySet());doThrow(new AccessDeniedException("blocked")).when(denied).roleDataScope(anyLong(),anyInt(),anySet());
        assertThrows(AccessDeniedException.class,()->service.assignUserRole(99L,set(20L)));assertThrows(AccessDeniedException.class,()->service.assignRoleUsers(20L,set(99L)));assertThrows(AccessDeniedException.class,()->service.assignRoleMenu(20L,set(100L)));assertThrows(AccessDeniedException.class,()->service.assignRoleDataScope(20L,1,set()));
    }
}
