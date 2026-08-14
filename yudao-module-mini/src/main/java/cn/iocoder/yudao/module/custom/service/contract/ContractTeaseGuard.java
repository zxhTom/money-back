package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * "戏耍模式"守卫：判断当前登录用户是否被标记为 teaseEnabled。
 *
 * 用户自身标记，或其任一角色标记了戏耍，都算命中——
 * `system_role.tease_enabled` 有列、有后台开关、有 toggle 接口，此前却没有任何地方读它，
 * 管理员在角色页打开开关界面显示已生效、实际毫无作用（静默失效的假开关）。
 *
 * 只影响合同相关查询接口，绝不影响 {@link AdminUserService#getUser} 等用户 profile 接口——
 * 被戏耍用户查自己的个人信息必须始终是真实数据。
 */
@Component
@Slf4j
public class ContractTeaseGuard {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;

    public boolean isTeasing(Long loginUserId) {
        if (loginUserId == null) {
            return false;
        }
        AdminUserDO user = adminUserService.getUser(loginUserId);
        if (user == null) {
            return false;
        }
        if (Boolean.TRUE.equals(user.getTeaseEnabled())) {
            return true;
        }
        return isAnyRoleTeasing(loginUserId);
    }

    /** 角色维度的戏耍标记，走角色缓存，不额外打库 */
    private boolean isAnyRoleTeasing(Long loginUserId) {
        try {
            Set<Long> roleIds = permissionService.getUserRoleIdListByUserIdFromCache(loginUserId);
            if (roleIds == null || roleIds.isEmpty()) {
                return false;
            }
            List<RoleDO> roles = roleService.getRoleListFromCache(roleIds);
            if (roles == null || roles.isEmpty()) {
                return false;
            }
            for (RoleDO role : roles) {
                if (role != null && Boolean.TRUE.equals(role.getTeaseEnabled())) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 判定失败按"不戏耍"处理：宁可给真实数据，也不能因为角色查询异常把正常用户变成假数据
            log.warn("[Tease] 解析角色戏耍标记失败 userId={}：{}", loginUserId, e.getMessage());
        }
        return false;
    }

}
