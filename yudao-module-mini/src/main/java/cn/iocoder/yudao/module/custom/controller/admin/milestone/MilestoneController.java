package cn.iocoder.yudao.module.custom.controller.admin.milestone;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.milestone.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.milestone.MilestoneDO;
import cn.iocoder.yudao.module.custom.service.milestone.MilestoneService;
import cn.iocoder.yudao.module.custom.service.milestone.MilestoneServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 系统关键时间节点预警")
@RestController
@RequestMapping("/custom/milestone")
@Validated
public class MilestoneController {

    @Resource
    private MilestoneService milestoneService;

    @PostMapping("/create")
    @Operation(summary = "创建时间节点")
    @PreAuthorize("@ss.hasPermission('custom:milestone:create')")
    public CommonResult<Long> createMilestone(@Valid @RequestBody MilestoneCreateReqVO createReqVO) {
        return success(milestoneService.createMilestone(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新时间节点")
    @PreAuthorize("@ss.hasPermission('custom:milestone:update')")
    public CommonResult<Boolean> updateMilestone(@Valid @RequestBody MilestoneUpdateReqVO updateReqVO) {
        milestoneService.updateMilestone(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除时间节点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('custom:milestone:delete')")
    public CommonResult<Boolean> deleteMilestone(@RequestParam("id") Long id) {
        milestoneService.deleteMilestone(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得时间节点详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:milestone:query')")
    public CommonResult<MilestoneRespVO> getMilestone(@RequestParam("id") Long id) {
        MilestoneDO milestone = milestoneService.getMilestone(id);
        return success(MilestoneServiceImpl.convertToResp(milestone, LocalDate.now()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得时间节点分页")
    @PreAuthorize("@ss.hasPermission('custom:milestone:query')")
    public CommonResult<PageResult<MilestoneRespVO>> getMilestonePage(@Valid MilestonePageReqVO pageReqVO) {
        PageResult<MilestoneDO> pageResult = milestoneService.getMilestonePage(pageReqVO);
        LocalDate today = LocalDate.now();
        PageResult<MilestoneRespVO> respPage = new PageResult<>(
                pageResult.getList().stream()
                        .map(m -> MilestoneServiceImpl.convertToResp(m, today))
                        .collect(Collectors.toList()),
                pageResult.getTotal()
        );
        return success(respPage);
    }

    @GetMapping("/summary")
    @Operation(summary = "获得时间节点看板概览")
    @PreAuthorize("@ss.hasPermission('custom:milestone:query')")
    public CommonResult<MilestoneSummaryRespVO> getMilestoneSummary() {
        return success(milestoneService.getMilestoneSummary());
    }

    @PostMapping("/sync-cert")
    @Operation(summary = "自动探测与同步域名SSL证书到期节点")
    @PreAuthorize("@ss.hasPermission('custom:milestone:update')")
    public CommonResult<String> syncCertMilestone() {
        return success(milestoneService.syncCertMilestones());
    }
}
