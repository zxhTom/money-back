package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.*;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.*;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/** Bounds delegated role management at the service boundary, including alternate assignment APIs. */
@Component
public class PermissionMutationGuard {
    @Resource private UserRoleMapper userRoles;
    @Resource private RoleMenuMapper roleMenus;
    @Resource private RoleService roles;
    @Resource private AdminUserService users;

    private static class Actor {
        Long id; List<RoleDO> roles; boolean root;
    }
    private Actor actor() {
        Actor a = new Actor(); a.id = SecurityFrameworkUtils.getLoginUserId();
        if (a.id == null) throw denied("请先登录");
        // Fresh actor roles; ignore the selected tenant only for this authenticated user's lookup.
        // Platform administrators keep creating tenants while TenantUtils temporarily selects the new tenant.
        a.roles = TenantUtils.executeIgnore(() -> {
            Set<Long> ids = userRoles.selectListByUserId(a.id).stream().map(UserRoleDO::getRoleId).collect(Collectors.toSet());
            return roles.getRoleList(ids).stream().filter(r -> Integer.valueOf(0).equals(r.getStatus())).collect(Collectors.toList());
        });
        if (a.roles.isEmpty()) throw denied("当前账号没有有效角色");
        a.root = a.roles.stream().anyMatch(r -> "super_admin".equals(r.getCode()));
        if (!a.root) {
            AdminUserDO user = users.getUser(a.id);
            if (user == null || !Integer.valueOf(0).equals(user.getStatus()) || !Objects.equals(user.getTenantId(), TenantContextHolder.getRequiredTenantId())) throw denied("当前账号不可授权或租户不匹配");
        }
        return a;
    }
    private AccessDeniedException denied(String text) { return new AccessDeniedException(text); }
    private RoleDO role(Long id, Actor a) {
        RoleDO r = roles.getRole(id);
        if (r == null || !Objects.equals(r.getTenantId(), TenantContextHolder.getRequiredTenantId())) throw denied("角色不存在或不属于当前租户");
        if (!a.root && "super_admin".equals(r.getCode())) throw denied("仅超级管理员可以管理超级管理员授权");
        return r;
    }
    private Set<Long> assigned(Long userId) {
        return userRoles.selectListByUserId(userId).stream().map(UserRoleDO::getRoleId).collect(Collectors.toSet());
    }
    private void target(Long id, Actor a) {
        AdminUserDO user = users.getUser(id);
        if (user == null || !Objects.equals(user.getTenantId(), TenantContextHolder.getRequiredTenantId())) throw denied("用户不存在或不属于当前租户");
        if (!a.root) for (Long roleId : assigned(id)) role(roleId, a);
    }
    private Set<Long> menus(Collection<Long> roleIds) {
        if (roleIds.isEmpty()) return Collections.emptySet();
        return roleMenus.selectListByRoleId(roleIds).stream().map(RoleMenuDO::getMenuId).collect(Collectors.toSet());
    }
    private Set<Long> ownRoleIds(Actor a) { return a.roles.stream().map(RoleDO::getId).collect(Collectors.toSet()); }
    private boolean management(Actor a) {
        return a.roles.stream().anyMatch(r -> "tenant_admin".equals(r.getCode()) || "contract-manager".equals(r.getCode()));
    }
    private boolean managementCode(String code) {
        return "super_admin".equals(code) || "tenant_admin".equals(code) || "contract-manager".equals(code);
    }
    /** Role codes determine authorization; changing an existing role must not promote its members. */
    public void roleDefinition(RoleDO current, String requestedCode) {
        if (current != null && !Objects.equals(current.getCode(), requestedCode)
                && (managementCode(current.getCode()) || managementCode(requestedCode))) {
            throw denied("不能修改管理员角色标识或将普通角色改为管理员，请通过角色授权操作");
        }
        if (!managementCode(requestedCode)) return;
        Actor a = actor();
        if (current != null) role(current.getId(), a);
        if (!a.root && !management(a)) throw denied("仅管理员可以管理管理员角色");
        if (!a.root && "super_admin".equals(requestedCode)) throw denied("仅超级管理员可以管理超级管理员角色");
    }
    private void grant(RoleDO r, Actor a) {
        if (a.root) return;
        if (!management(a)
                && ("tenant_admin".equals(r.getCode()) || "contract-manager".equals(r.getCode()))) throw denied("不能授予管理员角色");
        if (!menus(ownRoleIds(a)).containsAll(menus(Collections.singleton(r.getId())))) throw denied("不能授予超出自身权限范围的角色");
        // A delegated operator must not distribute broader row access than it currently owns.
        if (a.roles.stream().noneMatch(x -> Integer.valueOf(1).equals(x.getDataScope()))
                && !Integer.valueOf(5).equals(r.getDataScope()) && !ownRoleIds(a).contains(r.getId())) {
            boolean sameBound = a.roles.stream().anyMatch(x -> Objects.equals(x.getDataScope(), r.getDataScope())
                    && Objects.equals(x.getDataScopeDeptIds(), r.getDataScopeDeptIds()));
            if (!sameBound) throw denied("不能授予超出自身数据范围的角色");
        }
    }
    public void userRoles(Long userId, Set<Long> requested) {
        Actor a = actor(); target(userId, a);
        Set<Long> old = assigned(userId), next = requested == null ? Collections.emptySet() : requested;
        for (Long id : old) role(id, a);
        for (Long id : next) {
            RoleDO r = role(id, a);
            if (!old.contains(id)) {
                if (!a.root && !management(a) && a.id.equals(userId)) throw denied("不能为自己新增角色，请由上级管理员授权");
                if (!Integer.valueOf(0).equals(r.getStatus())) throw denied("角色已禁用");
                grant(r, a);
            }
        }
    }
    public void roleUsers(Long roleId, Set<Long> requested) {
        Actor a = actor(); RoleDO r = role(roleId, a);
        Set<Long> old = userRoles.selectListByRoleIds(Collections.singleton(roleId)).stream().map(UserRoleDO::getUserId).collect(Collectors.toSet());
        Set<Long> next = requested == null ? Collections.emptySet() : requested;
        Set<Long> changed = new HashSet<>(old); changed.addAll(next);
        changed.removeIf(id -> old.contains(id) == next.contains(id));
        for (Long id : changed) {
            target(id, a);
            if (next.contains(id)) {
                if (!a.root && !management(a) && a.id.equals(id)) throw denied("不能为自己新增角色，请由上级管理员授权");
                if (!Integer.valueOf(0).equals(r.getStatus())) throw denied("角色已禁用");
                grant(r, a);
            }
        }
    }
    public void roleMenus(Long roleId, Set<Long> requested) {
        Actor a = actor(); RoleDO r = role(roleId, a);
        if (a.root) return;
        Set<Long> next = requested == null ? Collections.emptySet() : requested;
        if (menus(Collections.singleton(roleId)).equals(next)) return;
        if (!menus(ownRoleIds(a)).containsAll(next)) throw denied("不能授予超出自身范围的菜单权限");
    }
    public void roleDataScope(Long roleId, Integer scope, Set<Long> departments) {
        Actor a = actor(); RoleDO r = role(roleId, a);
        boolean unchanged = Objects.equals(scope, r.getDataScope())
                && (!Integer.valueOf(2).equals(scope) || Objects.equals(departments, r.getDataScopeDeptIds()));
        if (a.root || unchanged || a.roles.stream().anyMatch(x -> Integer.valueOf(1).equals(x.getDataScope())) || Integer.valueOf(5).equals(scope)) return;
        if (ownRoleIds(a).contains(roleId)) throw denied("不能扩大自己所属角色的数据范围");
        if (Integer.valueOf(2).equals(scope) && departments != null && a.roles.stream().anyMatch(x -> Integer.valueOf(2).equals(x.getDataScope())
                && x.getDataScopeDeptIds() != null && x.getDataScopeDeptIds().containsAll(departments))) return;
        throw denied("不能授予超出自身数据范围的权限");
    }
}
