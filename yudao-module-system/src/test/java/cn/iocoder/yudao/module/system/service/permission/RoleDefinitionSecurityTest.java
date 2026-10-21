package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleDefinitionSecurityTest {
    private PermissionMutationGuardTest fixture;
    private RoleServiceImpl service;
    private RoleMapper mapper;
    private PermissionService permissions;

    @BeforeEach void setup() throws Exception {
        fixture = new PermissionMutationGuardTest();fixture.setup();
        service = new RoleServiceImpl();mapper = mock(RoleMapper.class);permissions = mock(PermissionService.class);
        PermissionMutationGuardTest.inject(service,"roleMapper",mapper);
        PermissionMutationGuardTest.inject(service,"mutationGuard",fixture.guard);
        PermissionMutationGuardTest.inject(service,"permissionService",permissions);
        when(mapper.selectById(anyLong())).thenAnswer(i -> fixture.catalog.get(i.getArgument(0)));
        when(mapper.selectByCode(anyString())).thenAnswer(i -> fixture.catalog.values().stream()
                .filter(r -> Objects.equals(r.getCode(),i.getArgument(0))).findFirst().orElse(null));
        when(mapper.insert(any(RoleDO.class))).thenAnswer(i -> {((RoleDO)i.getArgument(0)).setId(200L);return 1;});
        fixture.catalog.values().forEach(r -> r.setType(2));
    }
    @AfterEach void cleanup() {fixture.cleanup();}
    private RoleSaveReqVO change(long id,String code) {
        RoleSaveReqVO request = new RoleSaveReqVO();request.setId(id);request.setCode(code);
        request.setName("updated-name");request.setStatus(0);request.setSort(1);return request;
    }
    private void admin() {fixture.memberships.put(42L,Arrays.asList(11L));}

    @Test void delegatedEditorCannotReleaseManagementCode() {
        fixture.role(30,"contract-manager",1,100L).setType(2);
        assertThrows(AccessDeniedException.class,() -> service.updateRole(change(30,"old-manager")));
        verify(mapper,never()).updateById(any(RoleDO.class));
    }
    @Test void delegatedEditorCannotPromoteExistingOrdinaryRoleEvenWhenCodeIsAvailable() {
        assertThrows(AccessDeniedException.class,() -> service.updateRole(change(20,"contract-manager")));
        verify(mapper,never()).updateById(any(RoleDO.class));
    }
    @Test void delegatedEditorCannotCreateManagementRole() {
        RoleSaveReqVO request = change(200,"contract-manager");request.setId(null);
        assertThrows(AccessDeniedException.class,() -> service.createRole(request,null));
        verify(mapper,never()).insert(any(RoleDO.class));
    }
    @Test void ordinaryRoleCreationAndEditingRemainUsable() {
        RoleSaveReqVO request = change(200,"reader");request.setId(null);
        assertEquals(200L,service.createRole(request,null));
        assertDoesNotThrow(() -> service.updateRole(change(20,"contract-reader")));
        verify(mapper).insert(any(RoleDO.class));verify(mapper).updateById(any(RoleDO.class));
    }
    @Test void administratorKeepsManagementRoleCreationAndMetadataEdits() {
        admin();RoleSaveReqVO request = change(200,"contract-manager");request.setId(null);
        assertEquals(200L,service.createRole(request,null));
        fixture.role(30,"contract-manager",1,100L).setType(2);
        RoleSaveReqVO metadata = change(30,"contract-manager");metadata.setStatus(1);
        assertDoesNotThrow(() -> service.updateRole(metadata));
        verify(mapper).updateById(org.mockito.ArgumentMatchers.<RoleDO>argThat(r -> r.getId().equals(30L)
                && r.getCode().equals("contract-manager") && r.getStatus().equals(1)));
    }
    @Test void administratorUsesAssignmentInsteadOfChangingOrdinaryRoleIntoManagement() {
        admin();assertThrows(AccessDeniedException.class,() -> service.updateRole(change(20,"contract-manager")));
        verify(mapper,never()).updateById(any(RoleDO.class));
    }
    @Test void delegatedEditorCannotDeleteManagementRole() {
        fixture.role(30,"contract-manager",1,100L).setType(2);
        assertThrows(AccessDeniedException.class,() -> service.deleteRole(30L));
        verify(mapper,never()).deleteById(anyLong());verifyNoInteractions(permissions);
    }
    @Test void mixedBulkDeleteRejectsBeforeAnyWrite() {
        fixture.role(30,"contract-manager",1,100L).setType(2);
        assertThrows(AccessDeniedException.class,() -> service.deleteRoleList(Arrays.asList(20L,30L)));
        verify(mapper,never()).deleteByIds(anyCollection());verifyNoInteractions(permissions);
    }
    @Test void administratorKeepsOrdinaryAndManagementRoleDeletion() {
        admin();fixture.role(30,"contract-manager",1,100L).setType(2);
        assertDoesNotThrow(() -> service.deleteRole(20L));assertDoesNotThrow(() -> service.deleteRole(30L));
        verify(permissions).processRoleDeleted(20L);verify(permissions).processRoleDeleted(30L);
    }
    @Test void delegatedEditorCannotUseOtherMetadataEndpointsForManagementRole() {
        fixture.role(30,"contract-manager",1,100L).setType(2);
        assertThrows(AccessDeniedException.class,() -> service.updateRoleInviteEnabled(30L,true));
        assertThrows(AccessDeniedException.class,() -> service.updateRoleTeaseEnabled(30L,true));
        assertThrows(AccessDeniedException.class,() -> service.updateRoleDataScope(30L,1,Collections.emptySet()));
        verify(mapper,never()).updateById(any(RoleDO.class));
    }
    @Test void administratorKeepsFlagsAndDataScopeUpdates() {
        admin();fixture.role(30,"contract-manager",1,100L).setType(2);
        assertDoesNotThrow(() -> service.updateRoleInviteEnabled(30L,true));
        assertDoesNotThrow(() -> service.updateRoleTeaseEnabled(30L,true));
        assertDoesNotThrow(() -> service.updateRoleDataScope(30L,1,Collections.emptySet()));
        verify(mapper,times(3)).updateById(any(RoleDO.class));
    }
    @Test void builtInRolesRemainProtectedEvenForAdministrators() {
        admin();fixture.catalog.get(11L).setType(1);
        assertThrows(cn.iocoder.yudao.framework.common.exception.ServiceException.class,
                () -> service.updateRole(change(11,"tenant_admin")));
        verify(mapper,never()).updateById(any(RoleDO.class));
    }
    @Test void platformTenantBootstrapKeepsSystemRoleDefaultsAndRestoresTenantContext() {
        fixture.memberships.put(42L,Arrays.asList(1L));
        cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.setTenantId(2L);
        when(mapper.selectByCode("tenant_admin")).thenReturn(null);
        RoleSaveReqVO request=change(200,"tenant_admin");request.setId(null);request.setStatus(null);
        assertEquals(200L,service.createRole(request,1));
        verify(mapper).insert(org.mockito.ArgumentMatchers.<RoleDO>argThat(r -> r.getType().equals(1)
                && r.getStatus().equals(0) && r.getDataScope().equals(1)));
        assertEquals(2L,cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId());
        assertFalse(cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.isIgnore());
    }
    @Test void springCanWireRealRoleAndPermissionServicesAndGuard() {
        when(mapper.selectByIds(anyCollection())).thenAnswer(i -> {
            Collection<Long> ids=i.getArgument(0);List<RoleDO> roles=new ArrayList<>();
            ids.forEach(id -> {if(fixture.catalog.containsKey(id)) roles.add(fixture.catalog.get(id));});return roles;
        });
        try(org.springframework.context.annotation.AnnotationConfigApplicationContext context = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            context.registerBean(RoleMapper.class,() -> mapper);
            context.registerBean(cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper.class,() -> fixture.userRoles);
            context.registerBean(cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper.class,() -> fixture.roleMenus);
            context.registerBean(cn.iocoder.yudao.module.system.service.user.AdminUserService.class,() -> fixture.users);
            context.registerBean(MenuService.class,() -> mock(MenuService.class));
            context.registerBean(cn.iocoder.yudao.module.system.service.dept.DeptService.class,() -> mock(cn.iocoder.yudao.module.system.service.dept.DeptService.class));
            context.register(RoleServiceImpl.class,PermissionServiceImpl.class,PermissionMutationGuard.class);context.refresh();
            RoleService real = context.getBean(RoleService.class);
            assertThrows(AccessDeniedException.class,() -> real.updateRole(change(20,"contract-manager")));
            admin();RoleSaveReqVO request=change(200,"contract-manager");request.setId(null);
            assertEquals(200L,real.createRole(request,null));
        }
    }
}
