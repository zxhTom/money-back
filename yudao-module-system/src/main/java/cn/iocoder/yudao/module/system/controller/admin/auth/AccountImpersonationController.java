package cn.iocoder.yudao.module.system.controller.admin.auth;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.UserRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.service.auth.AccountImpersonationService;
import cn.iocoder.yudao.module.system.service.auth.AccountImpersonationService.Session;
import cn.iocoder.yudao.module.system.service.auth.AccountImpersonationService.UserChoice;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/system/auth/account-switch")
public class AccountImpersonationController {
    @Resource private AccountImpersonationService service;
    @Resource private SecurityProperties securityProperties;

    @GetMapping("/search")
    @RateLimiter(time = 60, count = 20, keyResolver = UserRateLimiterKeyResolver.class)
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<List<UserChoice>> search(@RequestParam("keyword") String keyword, HttpServletRequest request) {
        return success(service.search(token(request), keyword));
    }

    @GetMapping("/capability")
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<Boolean> capability(HttpServletRequest request) {
        return success(service.canStart(token(request)));
    }

    @PostMapping("/start")
    @RateLimiter(time = 60, count = 5, keyResolver = UserRateLimiterKeyResolver.class)
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<Session> start(@RequestBody StartRequest body, HttpServletRequest request) {
        return success(service.start(token(request), body.targetUserId, body.password));
    }

    @GetMapping("/status")
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<Session> status(HttpServletRequest request) {
        return success(service.status(token(request)));
    }

    @PostMapping("/stop")
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<Boolean> stop(HttpServletRequest request) {
        service.stop(token(request));
        return success(true);
    }

    private String token(HttpServletRequest request) {
        return SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
    }

    @Data
    public static class StartRequest {
        private Long targetUserId;
        private String password;
    }
}
