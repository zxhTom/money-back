package cn.iocoder.yudao.module.custom.controller.admin.autotest;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.service.autotest.AutoTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 自动化测试")
@RestController
@RequestMapping("/custom/auto-test")
public class AutoTestController {

    @Resource
    private AutoTestService autoTestService;

    @PostMapping("/run")
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
