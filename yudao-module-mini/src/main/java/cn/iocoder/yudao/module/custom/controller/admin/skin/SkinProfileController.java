package cn.iocoder.yudao.module.custom.controller.admin.skin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfileRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import cn.iocoder.yudao.module.custom.service.skin.SkinProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 皮肤配置")
@RestController
@RequestMapping("/custom/skin")
@Validated
public class SkinProfileController {

    @Resource
    private SkinProfileService skinProfileService;

    @GetMapping("/page")
    @Operation(summary = "获得皮肤配置分页")
    @PreAuthorize("@ss.hasPermission('custom:skin:query')")
    public CommonResult<PageResult<SkinProfileRespVO>> getSkinProfilePage(@Valid SkinProfilePageReqVO pageReqVO) {
        PageResult<SkinProfileDO> pageResult = skinProfileService.getSkinProfilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SkinProfileRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得皮肤配置详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:skin:query')")
    public CommonResult<SkinProfileRespVO> getSkinProfile(@RequestParam("id") Long id) {
        SkinProfileDO skinProfile = skinProfileService.getSkinProfile(id);
        return success(BeanUtils.toBean(skinProfile, SkinProfileRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建皮肤配置")
    @PreAuthorize("@ss.hasPermission('custom:skin:create')")
    public CommonResult<Long> createSkinProfile(@Valid @RequestBody SkinProfileSaveReqVO createReqVO) {
        return success(skinProfileService.createSkinProfile(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新皮肤配置")
    @PreAuthorize("@ss.hasPermission('custom:skin:update')")
    public CommonResult<Boolean> updateSkinProfile(@Valid @RequestBody SkinProfileSaveReqVO updateReqVO) {
        skinProfileService.updateSkinProfile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除皮肤配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:skin:delete')")
    public CommonResult<Boolean> deleteSkinProfile(@RequestParam("id") Long id) {
        skinProfileService.deleteSkinProfile(id);
        return success(true);
    }

    @PutMapping("/use")
    @Operation(summary = "切换生效皮肤")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:skin:use')")
    public CommonResult<Boolean> useSkinProfile(@RequestParam("id") Long id) {
        skinProfileService.useSkinProfile(id);
        return success(true);
    }

    @PostMapping("/clone")
    @Operation(summary = "基于预设皮肤克隆一份自定义皮肤")
    @PreAuthorize("@ss.hasPermission('custom:skin:create')")
    public CommonResult<Long> cloneSkinProfile(@Valid @RequestBody SkinCloneReqVO cloneReqVO) {
        return success(skinProfileService.cloneAsCustom(cloneReqVO));
    }

}
