package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.service.monitor.GlobalDiagnoseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/custom/global-diagnose")
@Validated
public class GlobalDiagnoseController {

    @Resource
    private GlobalDiagnoseService globalDiagnoseService;

    @GetMapping("/scan-ips")
    @PreAuthorize("@ss.hasPermission('custom:security:global-diagnose')")
    public CommonResult<Map<String, Object>> triggerScanAllIps(
            @RequestParam(defaultValue = "7") int days) {
        return success(globalDiagnoseService.triggerScanAllIps(days));
    }

    @GetMapping("/user-behavior")
    @PreAuthorize("@ss.hasPermission('custom:security:global-diagnose')")
    public CommonResult<Map<String, Object>> triggerAnalyzeUserBehavior() {
        return success(globalDiagnoseService.triggerAnalyzeUserBehavior());
    }
}
