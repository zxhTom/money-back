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
    @Resource
    private cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Resource
    private cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper adminUserMapper;

    private static final java.util.regex.Pattern IPV4 =
            java.util.regex.Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    @GetMapping("/online-lookup")
    @Operation(summary = "在线IP/用户互查：输入IP查在线用户，否则按用户名/昵称/真实姓名/身份证号查用户的在用IP")
    @Parameter(name = "keyword", description = "IP 或 用户关键字", required = true)
    @PreAuthorize("@ss.hasPermission('custom:security:online-lookup')")
    public CommonResult<cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO> onlineLookup(
            @RequestParam("keyword") String keyword) {
        cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO vo =
                new cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO();
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            vo.setType("USER");
            vo.setTotal(0);
            vo.setIps(java.util.Collections.emptyList());
            return success(vo);
        }
        if (isIpv4(kw)) {
            // IP → 该 IP 上的在线用户（每人带其在用 IP 数）
            vo.setType("IP");
            List<cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.UserRow> rows =
                    lookupUsersByIp(kw);
            vo.setUsers(rows);
            vo.setTotal(rows.size());
        } else {
            // 关键字 → 匹配用户，返回他们在用的 IP（每个带该 IP 的在线用户数）
            vo.setType("USER");
            List<cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.IpRow> rows =
                    lookupIpsByKeyword(kw);
            vo.setIps(rows);
            vo.setTotal(rows.size());
        }
        return success(vo);
    }

    /** IP → 在线用户列表（每人带其在用的不同 IP 数），全部在内存聚合 */
    private List<cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.UserRow> lookupUsersByIp(String ip) {
        List<Long> userIds = oauth2AccessTokenMapper.selectDistinctUserIdsByIp(ip);
        if (userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // 每个用户当前在用的不同 IP
        java.util.Map<Long, java.util.Set<String>> ipsByUser = new java.util.HashMap<>();
        for (cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO t
                : oauth2AccessTokenMapper.selectActiveByUserIds(userIds)) {
            ipsByUser.computeIfAbsent(t.getUserId(), k -> new java.util.HashSet<>()).add(t.getIp());
        }
        // 用户名/昵称
        java.util.Map<Long, cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO> userMap = new java.util.HashMap<>();
        for (cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO u
                : adminUserMapper.selectList(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX
                <cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO>()
                .in(cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO::getId, userIds))) {
            userMap.put(u.getId(), u);
        }
        List<cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.UserRow> rows = new java.util.ArrayList<>();
        for (Long uid : userIds) {
            cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.UserRow r =
                    new cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.UserRow();
            cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO u = userMap.get(uid);
            r.setUserId(uid);
            r.setUsername(u == null ? null : u.getUsername());
            r.setNickname(u == null ? null : u.getNickname());
            r.setIpCount(ipsByUser.getOrDefault(uid, java.util.Collections.emptySet()).size());
            rows.add(r);
        }
        rows.sort((a, b) -> Integer.compare(b.getIpCount(), a.getIpCount()));
        return rows;
    }

    /** 关键字匹配用户 → 其在用 IP 列表（每个 IP 带其在线用户数），全部在内存聚合 */
    private List<cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.IpRow> lookupIpsByKeyword(String kw) {
        List<Long> userIds = adminUserMapper.selectIdsByKeyword(kw);
        if (userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // 匹配用户在用的所有 IP
        java.util.Set<String> ips = new java.util.HashSet<>();
        for (cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO t
                : oauth2AccessTokenMapper.selectActiveByUserIds(userIds)) {
            ips.add(t.getIp());
        }
        if (ips.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // 这些 IP 各自的在线用户数（全局，不限于匹配用户）
        java.util.Map<String, java.util.Set<Long>> usersByIp = new java.util.HashMap<>();
        for (cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO t
                : oauth2AccessTokenMapper.selectActiveByIps(ips)) {
            usersByIp.computeIfAbsent(t.getIp(), k -> new java.util.HashSet<>()).add(t.getUserId());
        }
        List<cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.IpRow> rows = new java.util.ArrayList<>();
        for (String ip : ips) {
            cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.IpRow r =
                    new cn.iocoder.yudao.module.system.controller.admin.monitor.vo.OnlineLookupRespVO.IpRow();
            r.setIp(ip);
            r.setUserCount(usersByIp.getOrDefault(ip, java.util.Collections.emptySet()).size());
            rows.add(r);
        }
        rows.sort((a, b) -> Integer.compare(b.getUserCount(), a.getUserCount()));
        return rows;
    }

    static boolean isIpv4(String s) {
        if (!IPV4.matcher(s).matches()) {
            return false;
        }
        for (String part : s.split("\\.")) {
            if (Integer.parseInt(part) > 255) {
                return false;
            }
        }
        return true;
    }

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
    @PreAuthorize("@ss.hasPermission('system:user:ip-history')")
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
