package cn.iocoder.yudao.module.custom.controller.admin.timewindow;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.timewindow.TimeWindowDO;
import cn.iocoder.yudao.module.custom.service.timewindow.TimeWindowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 时间窗口")
@RestController
@RequestMapping("/custom/time-window")
@Validated
public class TimeWindowController {

    @Resource
    private TimeWindowService timeWindowService;

    @PostMapping("/create")
    @Operation(summary = "创建时间窗口")
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:create')")
    public CommonResult<Long> createTimeWindow(@Valid @RequestBody TimeWindowCreateReqVO createReqVO) {
        return success(timeWindowService.createTimeWindow(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新时间窗口")
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:update')")
    public CommonResult<Boolean> updateTimeWindow(@Valid @RequestBody TimeWindowUpdateReqVO updateReqVO) {
        timeWindowService.updateTimeWindow(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除时间窗口")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:delete')")
    public CommonResult<Boolean> deleteTimeWindow(@RequestParam("id") Long id) {
        timeWindowService.deleteTimeWindow(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除时间窗口")
    @Parameter(name = "ids", description = "编号列表，逗号分隔如 1,2,3", required = true)
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:delete')")
    public CommonResult<Boolean> deleteTimeWindowList(@RequestParam("ids") List<Long> ids) {
        timeWindowService.deleteTimeWindowList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得时间窗口详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:query')")
    public CommonResult<TimeWindowRespVO> getTimeWindow(@RequestParam("id") Long id) {
        TimeWindowDO window = timeWindowService.getTimeWindow(id);
        return success(BeanUtils.toBean(window, TimeWindowRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得时间窗口分页")
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:query')")
    public CommonResult<PageResult<TimeWindowRespVO>> getTimeWindowPage(@Valid TimeWindowPageReqVO pageReqVO) {
        PageResult<TimeWindowDO> pageResult = timeWindowService.getTimeWindowPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TimeWindowRespVO.class));
    }

    @PostMapping("/check-overlap")
    @Operation(summary = "重合校验（提交前调用）")
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:query')")
    public CommonResult<TimeWindowCheckOverlapRespVO> checkOverlap(@Valid @RequestBody TimeWindowCheckOverlapReqVO reqVO) {
        return success(timeWindowService.checkOverlap(reqVO));
    }

    @GetMapping("/config")
    @Operation(summary = "获取重合阈值配置")
    @PreAuthorize("@ss.hasPermission('custom:timeWindow:query')")
    public CommonResult<TimeWindowConfigRespVO> getConfig() {
        return success(timeWindowService.getConfig());
    }

    @GetMapping("/check-selected")
    @Operation(summary = "查询当前时间命中且将用户判定为选中的已激活时间窗口")
    @Parameter(name = "userId", description = "用户ID，不传则取当前登录用户", example = "1")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<TimeWindowRespVO> getActiveWindowSelectingUserNow(
            @RequestParam(value = "userId", required = false) Long userId) {
        TimeWindowDO window = timeWindowService.getActiveWindowSelectingUserNow(userId);
        return success(window != null ? BeanUtils.toBean(window, TimeWindowRespVO.class) : null);
    }
}
