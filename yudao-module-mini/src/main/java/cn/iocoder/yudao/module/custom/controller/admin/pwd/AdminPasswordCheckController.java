package cn.iocoder.yudao.module.custom.controller.admin.pwd;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.UserRateLimiterKeyResolver;
import cn.iocoder.yudao.module.custom.service.pwd.AdminPasswordCheckService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/custom/admin/password-check")
public class AdminPasswordCheckController {
    @Resource private AdminPasswordCheckService service;
    @Resource private SecurityFrameworkService security;

    @GetMapping("/capability")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> capability() {
        return success(security.hasAnyRoles("super_admin", "tenant_admin"));
    }
    @GetMapping("/users")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    public CommonResult<List<Map<String,String>>> search(@RequestParam(defaultValue="") String keyword) {
        return success(service.search(keyword));
    }
    @PostMapping("/verify")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @RateLimiter(time=60,count=10,keyResolver=UserRateLimiterKeyResolver.class,message="核验过于频繁，请稍后重试")
    @ApiAccessLog(requestEnable=false,responseEnable=false,operateModule="管理员密码核验",operateName="核验密码")
    public CommonResult<Boolean> verify(@RequestBody CheckRequest req) {
        return success(service.verify(req.getUsername(),req.getPassword(),req.getKind()));
    }
    @Getter @Setter
    public static class CheckRequest {
        private String username;
        private String kind;
        @JsonProperty(access=JsonProperty.Access.WRITE_ONLY)
        private String password;
    }
}
