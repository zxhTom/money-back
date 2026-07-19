package cn.iocoder.yudao.module.custom.controller.admin.text;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfileCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfileRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.service.text.TextProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文案配置")
@RestController
@RequestMapping("/custom/text/profile")
@Validated
public class TextProfileController {

    @Resource
    private TextProfileService textProfileService;

    @GetMapping("/page")
    @Operation(summary = "获得文案配置分页")
    @PreAuthorize("@ss.hasPermission('custom:text:query')")
    public CommonResult<PageResult<TextProfileRespVO>> getTextProfilePage(@Valid TextProfilePageReqVO pageReqVO) {
        PageResult<TextProfileDO> pageResult = textProfileService.getTextProfilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TextProfileRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得文案配置详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:text:query')")
    public CommonResult<TextProfileRespVO> getTextProfile(@RequestParam("id") Long id) {
        TextProfileDO textProfile = textProfileService.getTextProfile(id);
        return success(BeanUtils.toBean(textProfile, TextProfileRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建文案配置")
    @PreAuthorize("@ss.hasPermission('custom:text:create')")
    public CommonResult<Long> createTextProfile(@Valid @RequestBody TextProfileSaveReqVO createReqVO) {
        return success(textProfileService.createTextProfile(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文案配置")
    @PreAuthorize("@ss.hasPermission('custom:text:update')")
    public CommonResult<Boolean> updateTextProfile(@Valid @RequestBody TextProfileSaveReqVO updateReqVO) {
        textProfileService.updateTextProfile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文案配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:text:delete')")
    public CommonResult<Boolean> deleteTextProfile(@RequestParam("id") Long id) {
        textProfileService.deleteTextProfile(id);
        return success(true);
    }

    @PutMapping("/use")
    @Operation(summary = "切换生效文案套")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:text:use')")
    public CommonResult<Boolean> useTextProfile(@RequestParam("id") Long id) {
        textProfileService.useTextProfile(id);
        return success(true);
    }

    @PostMapping("/clone")
    @Operation(summary = "克隆一份文案套")
    @PreAuthorize("@ss.hasPermission('custom:text:create')")
    public CommonResult<Long> cloneTextProfile(@Valid @RequestBody TextProfileCloneReqVO cloneReqVO) {
        return success(textProfileService.cloneProfile(cloneReqVO.getSourceId(), cloneReqVO.getName()));
    }

}
