package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpAccessLogDO;
import cn.iocoder.yudao.module.system.service.monitor.IpAccessLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IP 访问尝试统计")
@RestController
@RequestMapping("/custom/security/ip-access")
@Validated
public class IpAccessLogController {

    @Resource
    private IpAccessLogService ipAccessLogService;

    @GetMapping("/daily-trend")
    @Operation(summary = "每日拦截趋势（近 N 天）")
    @PreAuthorize("@ss.hasPermission('custom:security:ipaccess:query')")
    public CommonResult<List<Map<String, Object>>> dailyTrend(@RequestParam(defaultValue = "7") int days) {
        return success(ipAccessLogService.getDailyTrend(days));
    }

    @GetMapping("/top-ip")
    @Operation(summary = "Top 试探/攻击 IP（含是否已封禁）")
    @PreAuthorize("@ss.hasPermission('custom:security:ipaccess:query')")
    public CommonResult<List<Map<String, Object>>> topIp(@RequestParam(defaultValue = "7") int days,
                                                         @RequestParam(defaultValue = "20") int size) {
        return success(ipAccessLogService.getTopIps(days, size));
    }

    @GetMapping("/daily-by-ip")
    @Operation(summary = "指定 IP 的每日访问频率")
    @PreAuthorize("@ss.hasPermission('custom:security:ipaccess:query')")
    public CommonResult<List<Map<String, Object>>> dailyByIp(@RequestParam String ip,
                                                            @RequestParam(defaultValue = "14") int days) {
        return success(ipAccessLogService.getDailyByIp(ip, days));
    }

    @GetMapping("/page")
    @Operation(summary = "IP 访问尝试明细（分页）")
    @PreAuthorize("@ss.hasPermission('custom:security:ipaccess:query')")
    public CommonResult<PageResult<IpAccessLogDO>> page(@Validated IpAccessLogPageReqVO reqVO) {
        return success(ipAccessLogService.getPage(reqVO));
    }
}
