package cn.iocoder.yudao.module.custom.controller.admin.changelog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.changelog.vo.VersionChangelogCheckRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;
import cn.iocoder.yudao.module.custom.service.changelog.VersionChangelogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 版本升级说明")
@RestController
@RequestMapping("/custom/version-changelog")
@Validated
public class VersionChangelogController {

    @Resource
    private VersionChangelogService versionChangelogService;

    @GetMapping("/check")
    @Operation(summary = "检查当前用户是否需要弹出该版本的升级说明（只读，不标记已读）")
    public CommonResult<VersionChangelogCheckRespVO> check(@RequestParam("version") String version) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(versionChangelogService.check(userId, version));
    }

    @PostMapping("/ack")
    @Operation(summary = "确认已看到该版本的升级说明弹窗")
    public CommonResult<Boolean> ack(@RequestParam("version") String version) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        versionChangelogService.ack(userId, version);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有版本升级说明")
    @PreAuthorize("@ss.hasPermission('custom:version-changelog:query')")
    public CommonResult<List<VersionChangelogDO>> list() {
        return success(versionChangelogService.listAll());
    }

    @PostMapping("/create")
    @Operation(summary = "新增版本升级说明")
    @PreAuthorize("@ss.hasPermission('custom:version-changelog:handle')")
    public CommonResult<Long> create(@RequestBody VersionChangelogDO changelog) {
        return success(versionChangelogService.create(changelog));
    }

    @PutMapping("/update")
    @Operation(summary = "修改版本升级说明")
    @PreAuthorize("@ss.hasPermission('custom:version-changelog:handle')")
    public CommonResult<Boolean> update(@RequestBody VersionChangelogDO changelog) {
        versionChangelogService.update(changelog);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除版本升级说明")
    @PreAuthorize("@ss.hasPermission('custom:version-changelog:handle')")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        versionChangelogService.delete(id);
        return success(true);
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "发布")
    @PreAuthorize("@ss.hasPermission('custom:version-changelog:handle')")
    public CommonResult<Boolean> enable(@PathVariable Long id) {
        versionChangelogService.setEnabled(id, true);
        return success(true);
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "下线")
    @PreAuthorize("@ss.hasPermission('custom:version-changelog:handle')")
    public CommonResult<Boolean> disable(@PathVariable Long id) {
        versionChangelogService.setEnabled(id, false);
        return success(true);
    }

}
