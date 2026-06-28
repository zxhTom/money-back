package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpBlacklistAddReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpDiagnoseResultVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertHandleReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.UserIpStatVO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.UserIpHistoryMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpHistoryDO;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.IpRiskCheckService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import cn.iocoder.yudao.module.system.service.monitor.UserIpHistoryService;
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
    private UserIpHistoryMapper userIpHistoryMapper;
    @Resource
    private IpBlacklistService ipBlacklistService;
    @Resource
    private IpRiskCheckService ipRiskCheckService;
    @Resource
    private UserIpHistoryService userIpHistoryService;

    // ─── 安全告警 ────────────────────────────────────────────

    @GetMapping("/alert/page")
    @Operation(summary = "分页查询安全告警")
    @PreAuthorize("@ss.hasAnyPermissions('custom:security:alert:query','mini:admin:security:alert')")
    public CommonResult<PageResult<SecurityAlertDO>> getAlertPage(@Valid SecurityAlertPageReqVO pageReqVO) {
        return success(securityAlertService.getPage(pageReqVO));
    }

    @PutMapping("/alert/handle")
    @Operation(summary = "处理安全告警")
    @PreAuthorize("@ss.hasAnyPermissions('custom:security:alert:handle','mini:admin:security:alert')")
    public CommonResult<Boolean> handleAlert(@Valid @RequestBody SecurityAlertHandleReqVO reqVO) {
        securityAlertService.handle(reqVO, SecurityFrameworkUtils.getLoginUserId());
        return success(true);
    }

    @GetMapping("/stats")
    @Operation(summary = "安全统计概览")
    @PreAuthorize("@ss.hasAnyPermissions('custom:security:alert:query','mini:admin:security:alert')")
    public CommonResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayUnhandled", securityAlertService.countTodayUnhandled());
        stats.put("alertTypeStats", securityAlertService.getTodayAlertTypeStats());
        stats.put("topAttackIps", securityAlertService.getTopAttackIps());
        return success(stats);
    }

    // ─── IP 诊断 ──────────────────────────────────────────────

    @GetMapping("/ip-diagnose")
    @Operation(summary = "诊断用户所有历史IP风险")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('custom:security:ip-diagnose')")
    public CommonResult<List<IpDiagnoseResultVO>> diagnoseUserIp(@RequestParam Long userId) {
        return success(ipRiskCheckService.diagnoseUser(userId));
    }

    // ─── IP 黑名单 ────────────────────────────────────────────

    @GetMapping("/blacklist/page")
    @Operation(summary = "分页查询IP黑名单")
    @PreAuthorize("@ss.hasAnyPermissions('custom:security:blacklist:query','mini:admin:security:blacklist')")
    public CommonResult<PageResult<IpBlacklistDO>> getBlacklistPage(@Valid PageParam pageParam) {
        return success(ipBlacklistService.getPage(pageParam));
    }

    @PostMapping("/blacklist/add")
    @Operation(summary = "手动添加IP黑名单")
    @PreAuthorize("@ss.hasAnyPermissions('custom:security:blacklist:add','mini:admin:security:blacklist')")
    public CommonResult<Boolean> addBlacklist(@Valid @RequestBody IpBlacklistAddReqVO reqVO) {
        ipBlacklistService.addToBlacklist(reqVO);
        return success(true);
    }

    @DeleteMapping("/blacklist/remove")
    @Operation(summary = "从黑名单移除IP")
    @Parameter(name = "id", description = "黑名单记录ID", required = true)
    @PreAuthorize("@ss.hasAnyPermissions('custom:security:blacklist:remove','mini:admin:security:blacklist')")
    public CommonResult<Boolean> removeBlacklist(@RequestParam Long id) {
        ipBlacklistService.removeFromBlacklist(id);
        return success(true);
    }

    @PostMapping("/blacklist/refresh-cache")
    @Operation(summary = "刷新IP黑名单缓存")
    @PreAuthorize("@ss.hasAnyPermissions('custom:security:blacklist:add','mini:admin:security:blacklist')")
    public CommonResult<Boolean> refreshBlacklistCache() {
        ipBlacklistService.refreshCache();
        return success(true);
    }

    @GetMapping("/user/ip-count-stats")
    @Operation(summary = "按用户统计使用过的 IP 数（Top N）")
    @Parameter(name = "limit", description = "返回条数", example = "10")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<List<UserIpStatVO>> getUserIpCountStats(
            @RequestParam(defaultValue = "10") int limit) {
        return success(userIpHistoryMapper.selectTopUsersByIpCount(limit));
    }

    @GetMapping("/user/ip-history")
    @Operation(summary = "查询用户历史IP")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<List<UserIpHistoryDO>> getUserIpHistory(@RequestParam Long userId) {
        return success(userIpHistoryService.getByUserId(userId));
    }

    @GetMapping("/blacklist/logs")
    @Operation(summary = "查询IP封禁历史")
    @Parameter(name = "ip", description = "IP地址", required = true)
    @PreAuthorize("@ss.hasPermission('custom:security:blacklist:query')")
    public CommonResult<List<IpBlacklistLogDO>> getBanLogs(@RequestParam String ip) {
        return success(ipBlacklistService.getBanLogs(ip));
    }
}
