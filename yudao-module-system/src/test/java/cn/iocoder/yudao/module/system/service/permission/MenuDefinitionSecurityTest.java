package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuSaveVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.MenuMapper;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuDefinitionSecurityTest {
    private PermissionMutationGuardTest fixture;
    private MenuServiceImpl service;
    private MenuMapper mapper;
    private PermissionService permissions;
    private MenuDO current;
    @BeforeEach void setup() throws Exception {
        fixture=new PermissionMutationGuardTest();fixture.setup();service=new MenuServiceImpl();
        mapper=mock(MenuMapper.class);permissions=mock(PermissionService.class);
        PermissionMutationGuardTest.inject(service,"menuMapper",mapper);
        PermissionMutationGuardTest.inject(service,"permissionService",permissions);
        PermissionMutationGuardTest.inject(service,"mutationGuard",fixture.guard);
        current=new MenuDO().setId(100L).setName("existing").setType(3).setParentId(0L).setPermission("system:menu:update");
        when(mapper.selectById(100L)).thenReturn(current);
    }
    @AfterEach void cleanup(){fixture.cleanup();}
    private MenuSaveVO edit(String permission){MenuSaveVO v=new MenuSaveVO();v.setId(100L);v.setName("updated");v.setParentId(0L);v.setType(3);v.setSort(1);v.setStatus(0);v.setPermission(permission);return v;}
    private void root(){fixture.memberships.put(42L,Arrays.asList(1L));}
    @Test void delegatedEditorCannotRewriteAssignedMenuIntoNewPermission(){
        assertThrows(AccessDeniedException.class,()->service.updateMenu(edit("system:tenant:visit")));
        verify(mapper,never()).updateById(any(MenuDO.class));
    }
    @Test void tenantAdministratorCannotRewritePlatformPermissionDefinitions(){
        fixture.memberships.put(42L,Arrays.asList(11L));
        assertThrows(AccessDeniedException.class,()->service.updateMenu(edit("system:oauth2-token:page")));
        verify(mapper,never()).updateById(any(MenuDO.class));
    }
    @Test void platformAdministratorKeepsPermissionDefinitionManagement(){
        root();assertDoesNotThrow(()->service.updateMenu(edit("system:tenant:visit")));
        verify(mapper).updateById(org.mockito.ArgumentMatchers.<MenuDO>argThat(m->"system:tenant:visit".equals(m.getPermission())));
    }
    @Test void delegatedEditorKeepsNamesSortAndOtherMetadata(){
        assertDoesNotThrow(()->service.updateMenu(edit(current.getPermission())));
        verify(mapper).updateById(org.mockito.ArgumentMatchers.<MenuDO>argThat(m->"updated".equals(m.getName())&&m.getSort().equals(1)));
    }
    @Test void omittedOptionalPermissionKeepsExistingPermission(){
        assertDoesNotThrow(()->service.updateMenu(edit(null)));
        verify(mapper).updateById(org.mockito.ArgumentMatchers.<MenuDO>argThat(m->current.getPermission().equals(m.getPermission())));
    }
    @Test void nullAndEmptyPermissionForDisplayOnlyMenuRemainCompatible(){
        current.setPermission(null);assertDoesNotThrow(()->service.updateMenu(edit("")));
        assertDoesNotThrow(()->fixture.guard.menuDefinition(null,""));
        assertDoesNotThrow(()->fixture.guard.menuDefinition("",null));
    }
    @Test void delegatedEditorCannotRemovePermissionAndRecreateIt(){
        assertThrows(AccessDeniedException.class,()->service.updateMenu(edit("")));
        MenuSaveVO create=edit("system:tenant:visit");create.setId(null);
        assertThrows(AccessDeniedException.class,()->service.createMenu(create));
        verify(mapper,never()).updateById(any(MenuDO.class));verify(mapper,never()).insert(any(MenuDO.class));
    }
    @Test void displayOnlyMenuCreationRemainsAllowed(){
        MenuSaveVO create=edit("");create.setId(null);assertDoesNotThrow(()->service.createMenu(create));
        verify(mapper).insert(any(MenuDO.class));
    }
    @Test void deletingPermissionDefinitionRequiresPlatformAdministrator(){
        assertThrows(AccessDeniedException.class,()->service.deleteMenu(100L));
        verify(mapper,never()).deleteById(anyLong());verifyNoInteractions(permissions);
        root();assertDoesNotThrow(()->service.deleteMenu(100L));verify(permissions).processMenuDeleted(100L);
    }
    @Test void mixedBulkDeleteRejectsBeforeAnyWrite(){
        when(mapper.selectById(200L)).thenReturn(new MenuDO().setId(200L).setPermission(""));
        assertThrows(AccessDeniedException.class,()->service.deleteMenuList(Arrays.asList(200L,100L)));
        verify(mapper,never()).deleteByIds(anyCollection());verifyNoInteractions(permissions);
    }
    @Test void platformAdministratorKeepsBulkDeletionAndCreation(){
        root();assertDoesNotThrow(()->service.deleteMenuList(Arrays.asList(100L)));
        MenuSaveVO create=edit("new:feature:permission");create.setId(null);assertDoesNotThrow(()->service.createMenu(create));
        verify(mapper).deleteByIds(anyCollection());verify(mapper).insert(any(MenuDO.class));
    }
    @Test void anonymousCannotChangeAuthorizationDefinitions(){
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class,()->fixture.guard.menuDefinition("old:permission","new:permission"));
    }
    @Test void springCanWireRealMenuRolePermissionServicesAndGuard(){
        cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper roles=mock(cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper.class);
        when(roles.selectByIds(anyCollection())).thenAnswer(i->{Collection<Long> ids=i.getArgument(0);
            List<cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO> result=new ArrayList<>();
            ids.forEach(id->{if(fixture.catalog.containsKey(id))result.add(fixture.catalog.get(id));});return result;});
        root();
        try(org.springframework.context.annotation.AnnotationConfigApplicationContext context=new org.springframework.context.annotation.AnnotationConfigApplicationContext()){
            context.registerBean(MenuMapper.class,()->mapper);
            context.registerBean(cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper.class,()->roles);
            context.registerBean(cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper.class,()->fixture.userRoles);
            context.registerBean(cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper.class,()->fixture.roleMenus);
            context.registerBean(cn.iocoder.yudao.module.system.service.user.AdminUserService.class,()->fixture.users);
            context.registerBean(cn.iocoder.yudao.module.system.service.dept.DeptService.class,()->mock(cn.iocoder.yudao.module.system.service.dept.DeptService.class));
            context.registerBean(cn.iocoder.yudao.module.system.service.tenant.TenantService.class,()->mock(cn.iocoder.yudao.module.system.service.tenant.TenantService.class));
            context.register(MenuServiceImpl.class,RoleServiceImpl.class,PermissionServiceImpl.class,PermissionMutationGuard.class);context.refresh();
            assertDoesNotThrow(()->context.getBean(MenuService.class).updateMenu(edit("system:tenant:visit")));
            verify(mapper).updateById(any(MenuDO.class));
        }
    }
}
