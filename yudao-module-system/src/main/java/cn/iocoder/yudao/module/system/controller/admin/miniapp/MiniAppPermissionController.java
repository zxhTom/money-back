package cn.iocoder.yudao.module.system.controller.admin.miniapp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 小程序功能权限")
@RestController
@RequestMapping("/system/miniapp/permission")
@Validated
public class MiniAppPermissionController {

    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;

    @GetMapping("/resources")
    @Operation(summary = "获取所有小程序功能资源列表")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<List<Map<String, Object>>> listResources() {
        List<MenuDO> menus = menuService.getMenuListByType(MenuTypeEnum.MINI_APP.getType());
        List<Map<String, Object>> result = menus.stream().map(m -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("name", m.getName());
            item.put("permission", m.getPermission());
            item.put("sort", m.getSort());
            return item;
        }).collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/role-resources")
    @Operation(summary = "获取角色已授权的小程序资源ID列表")
    @Parameter(name = "roleId", description = "角色ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<Set<Long>> getRoleResources(@RequestParam Long roleId) {
        Set<Long> allMenuIds = permissionService.getRoleMenuListByRoleId(roleId);
        List<MenuDO> miniMenus = menuService.getMenuListByType(MenuTypeEnum.MINI_APP.getType());
        Set<Long> miniMenuIds = miniMenus.stream().map(MenuDO::getId).collect(Collectors.toSet());
        allMenuIds.retainAll(miniMenuIds);
        return success(allMenuIds);
    }

    @PutMapping("/assign-role-resources")
    @Operation(summary = "给角色授权小程序资源（不影响已有Web菜单权限）")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    public CommonResult<Boolean> assignRoleResources(@RequestParam Long roleId,
                                                      @RequestBody(required = false) Set<Long> miniMenuIds) {
        // 取该角色现有的全部菜单（含Web 1/2/3类型）
        Set<Long> existingAll = new HashSet<>(permissionService.getRoleMenuListByRoleId(roleId));
        // 从现有中剔除所有旧的小程序资源
        List<MenuDO> allMiniMenus = menuService.getMenuListByType(MenuTypeEnum.MINI_APP.getType());
        Set<Long> allMiniIds = allMiniMenus.stream().map(MenuDO::getId).collect(Collectors.toSet());
        existingAll.removeAll(allMiniIds);
        // 合入新选的小程序资源
        if (miniMenuIds != null) {
            existingAll.addAll(miniMenuIds);
        }
        permissionService.assignRoleMenu(roleId, existingAll);
        return success(true);
    }
}
