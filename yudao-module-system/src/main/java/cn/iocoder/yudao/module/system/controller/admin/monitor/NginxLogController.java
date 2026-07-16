package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.service.monitor.NginxLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - Nginx 日志")
@RestController
@RequestMapping("/custom/security/nginx-log")
public class NginxLogController {

    @Resource
    private NginxLogService nginxLogService;

    @GetMapping("/config")
    @Operation(summary = "日志路径与状态")
    @PreAuthorize("@ss.hasPermission('custom:security:nginxlog:query')")
    public CommonResult<Map<String, Object>> config() {
        return success(nginxLogService.config());
    }

    @GetMapping("/tail")
    @Operation(summary = "查看日志尾部（type=access|error，可按 ip 过滤）")
    @PreAuthorize("@ss.hasPermission('custom:security:nginxlog:query')")
    public CommonResult<List<String>> tail(@RequestParam(defaultValue = "access") String type,
                                           @RequestParam(defaultValue = "300") int lines,
                                           @RequestParam(required = false) String ip) {
        return success(nginxLogService.tail(type, lines, ip));
    }

    @GetMapping("/stats")
    @Operation(summary = "基于 access 日志的 IP/接口/成功失败统计（ip 过滤对所有统计生效）")
    @PreAuthorize("@ss.hasPermission('custom:security:nginxlog:query')")
    public CommonResult<Map<String, Object>> stats(@RequestParam(required = false) String ip) {
        return success(nginxLogService.stats(ip));
    }
}
