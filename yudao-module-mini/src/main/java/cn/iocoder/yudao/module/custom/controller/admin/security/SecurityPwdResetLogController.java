package cn.iocoder.yudao.module.custom.controller.admin.security;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityPwdResetLogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.SecurityPwdResetLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 风控改密记录")
@RestController
@RequestMapping("/custom/security/pwd-reset-log")
@Validated
public class SecurityPwdResetLogController {

    @Resource
    private SecurityPwdResetLogMapper securityPwdResetLogMapper;

    @GetMapping("/page")
    @Operation(summary = "风控自动改密记录分页（含新密码明文）")
    @Parameter(name = "userId", description = "按用户ID过滤，可空")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:query')")
    public CommonResult<PageResult<SecurityPwdResetLogDO>> getPage(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "userId", required = false) Long userId) {
        PageParam page = new PageParam();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return success(securityPwdResetLogMapper.selectPage(page, userId));
    }

}
