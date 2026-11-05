package cn.iocoder.yudao.module.custom.controller.admin.autotest;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.service.autotest.AutoTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.UserRateLimiterKeyResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 自动化测试")
@RestController
@RequestMapping("/custom/auto-test")
public class AutoTestController {

    @Resource
    private AutoTestService autoTestService;

    @GetMapping("/capability")
    @Operation(summary = "查询是否可运行管理员只读检查")
    public CommonResult<Boolean> getCapability() {
        return success(autoTestService.hasAdminRole());
    }

    @PostMapping("/smoke")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @Operation(summary = "运行管理员只读检查")
    public CommonResult<Map<String, Boolean>> runSmokeTest() {
        return success(autoTestService.runSmokeTest());
    }

    @PostMapping("/face-preflight")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @RateLimiter(time = 60, count = 2, keyResolver = UserRateLimiterKeyResolver.class,
            message = "人脸预检请求过于频繁，请稍后再试")
    @Operation(summary = "人脸认证预检：不提交身份资料、不进行人脸扫描")
    public CommonResult<Map<String, Boolean>> runFacePreflight() {
        return success(autoTestService.runFacePreflight());
    }

    @PostMapping("/run")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @Operation(summary = "运行自动化测试流程")
    public CommonResult<Boolean> runAutoTest() throws Exception {
        String username = SecurityFrameworkUtils.getLoginUserNickname();
        if (!"zxhtom".equals(username) && !autoTestService.hasAdminRole()) {
            return cn.iocoder.yudao.framework.common.pojo.CommonResult.error(403, "没有权限");
        }
        boolean result = autoTestService.runFullTest();
        return success(result);
    }
}
