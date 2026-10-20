package cn.iocoder.yudao.module.system.controller.admin.risk;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.service.risk.RiskLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.validation.constraints.Size;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 风控锁定")
@RestController
@RequestMapping("/system/risk-lock")
@Validated
public class RiskLockController {

    @Resource
    private RiskLockService riskLockService;

    @GetMapping("/login-security")
    @Operation(summary = "查看用户登录安全状态及近期记录")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<Map<String,Object>> getLoginSecurity(@RequestParam("userId") Long userId) {
        return success(riskLockService.getLoginSecurity(userId));
    }

    @PutMapping("/unlock")
    @Operation(summary = "手动解锁用户")
    @Parameter(name = "userId", description = "用户编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:risk-lock:update')")
    @LogRecord(type="用户登录安全", subType="手动解锁", bizNo="{{#userId}}", success="解除风控锁定并清除密码错误计数，原因：{{#reason}}")
    public CommonResult<Boolean> unlockUser(@RequestParam("userId") Long userId,
            @RequestParam(value="reason", defaultValue="管理员手动处理") @Size(min=2,max=200) String reason) {
        riskLockService.unlockUser(SecurityFrameworkUtils.getLoginUserId(), userId);
        return success(true);
    }

}
