package cn.iocoder.yudao.module.custom.controller.admin.iconset;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfileRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import cn.iocoder.yudao.module.custom.service.iconset.IconSetProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 图标集配置")
@RestController
@RequestMapping("/custom/icon-set")
@Validated
public class IconSetProfileController {

    @Resource
    private IconSetProfileService iconSetProfileService;

    @GetMapping("/page")
    @Operation(summary = "获得图标集配置分页")
    @PreAuthorize("@ss.hasPermission('custom:icon-set:query')")
    public CommonResult<PageResult<IconSetProfileRespVO>> getIconSetProfilePage(@Valid IconSetProfilePageReqVO pageReqVO) {
        PageResult<IconSetProfileDO> pageResult = iconSetProfileService.getIconSetProfilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IconSetProfileRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得图标集配置详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:icon-set:query')")
    public CommonResult<IconSetProfileRespVO> getIconSetProfile(@RequestParam("id") Long id) {
        IconSetProfileDO iconSetProfile = iconSetProfileService.getIconSetProfile(id);
        return success(BeanUtils.toBean(iconSetProfile, IconSetProfileRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建图标集配置")
    @PreAuthorize("@ss.hasPermission('custom:icon-set:create')")
    public CommonResult<Long> createIconSetProfile(@Valid @RequestBody IconSetProfileSaveReqVO createReqVO) {
        return success(iconSetProfileService.createIconSetProfile(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新图标集配置")
    @PreAuthorize("@ss.hasPermission('custom:icon-set:update')")
    public CommonResult<Boolean> updateIconSetProfile(@Valid @RequestBody IconSetProfileSaveReqVO updateReqVO) {
        iconSetProfileService.updateIconSetProfile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除图标集配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:icon-set:delete')")
    public CommonResult<Boolean> deleteIconSetProfile(@RequestParam("id") Long id) {
        iconSetProfileService.deleteIconSetProfile(id);
        return success(true);
    }

    @PutMapping("/use")
    @Operation(summary = "切换生效图标集")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:icon-set:use')")
    public CommonResult<Boolean> useIconSetProfile(@RequestParam("id") Long id) {
        iconSetProfileService.useIconSetProfile(id);
        return success(true);
    }

    @PostMapping("/clone")
    @Operation(summary = "基于预设图标集克隆一份自定义图标集")
    @PreAuthorize("@ss.hasPermission('custom:icon-set:create')")
    public CommonResult<Long> cloneIconSetProfile(@Valid @RequestBody IconSetCloneReqVO cloneReqVO) {
        return success(iconSetProfileService.cloneAsCustom(cloneReqVO));
    }

}
