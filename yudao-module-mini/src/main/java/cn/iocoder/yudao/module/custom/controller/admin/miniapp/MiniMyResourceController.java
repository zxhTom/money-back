package cn.iocoder.yudao.module.custom.controller.admin.miniapp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 我的功能权限")
@RestController
@RequestMapping("/custom/mini")
@Validated
public class MiniMyResourceController {

    @Resource
    private PermissionService permissionService;
    @Resource
    private MenuService menuService;

    @GetMapping("/my-resources")
    @Operation(summary = "获取当前用户在小程序中有权限的功能 key 列表")
    public CommonResult<List<String>> myResources() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(userId);
        Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(roleIds);
        List<MenuDO> miniMenus = menuService.getMenuListByType(MenuTypeEnum.MINI_APP.getType());
        List<String> permissions = miniMenus.stream()
                .filter(m -> menuIds.contains(m.getId()))
                .map(MenuDO::getPermission)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        return success(permissions);
    }
}
