package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpBlacklistAddReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertHandleReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 安全监控")
@RestController
@RequestMapping("/custom/security")
@Validated
public class SecurityMonitorController {

    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private IpBlacklistService ipBlacklistService;

    // ─── 安全告警 ────────────────────────────────────────────

    @GetMapping("/alert/page")
    @Operation(summary = "分页查询安全告警")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:query')")
    public CommonResult<PageResult<SecurityAlertDO>> getAlertPage(@Valid SecurityAlertPageReqVO pageReqVO) {
        return success(securityAlertService.getPage(pageReqVO));
    }

    @PutMapping("/alert/handle")
    @Operation(summary = "处理安全告警")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:handle')")
    public CommonResult<Boolean> handleAlert(@Valid @RequestBody SecurityAlertHandleReqVO reqVO) {
        securityAlertService.handle(reqVO, SecurityFrameworkUtils.getLoginUserId());
        return success(true);
    }

    @GetMapping("/stats")
    @Operation(summary = "安全统计概览")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:query')")
    public CommonResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayUnhandled", securityAlertService.countTodayUnhandled());
        stats.put("alertTypeStats", securityAlertService.getTodayAlertTypeStats());
        stats.put("topAttackIps", securityAlertService.getTopAttackIps());
        return success(stats);
    }

    // ─── IP 黑名单 ────────────────────────────────────────────

    @GetMapping("/blacklist/page")
    @Operation(summary = "分页查询IP黑名单")
    @PreAuthorize("@ss.hasPermission('custom:security:blacklist:query')")
    public CommonResult<PageResult<IpBlacklistDO>> getBlacklistPage(@Valid PageParam pageParam) {
        return success(ipBlacklistService.getPage(pageParam));
    }

    @PostMapping("/blacklist/add")
    @Operation(summary = "手动添加IP黑名单")
    @PreAuthorize("@ss.hasPermission('custom:security:blacklist:add')")
    public CommonResult<Boolean> addBlacklist(@Valid @RequestBody IpBlacklistAddReqVO reqVO) {
        ipBlacklistService.addToBlacklist(reqVO);
        return success(true);
    }

    @DeleteMapping("/blacklist/remove")
    @Operation(summary = "从黑名单移除IP")
    @Parameter(name = "id", description = "黑名单记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('custom:security:blacklist:remove')")
    public CommonResult<Boolean> removeBlacklist(@RequestParam Long id) {
        ipBlacklistService.removeFromBlacklist(id);
        return success(true);
    }
}
