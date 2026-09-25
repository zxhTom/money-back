package cn.iocoder.yudao.module.custom.controller.admin.tenantconfig;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.service.tenantconfig.TenantConfigSeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租户配置初始化")
@RestController
@RequestMapping("/system/tenant-config")
@Validated
public class TenantConfigController {

    @Resource
    private TenantConfigSeedService tenantConfigSeedService;

    @PostMapping("/init")
    @Operation(summary = "初始化新租户配置（复制默认租户配置）")
    @Parameter(name = "tenantId", description = "目标租户编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:tenant:update')")
    public CommonResult<Map<String, Integer>> initTenantConfig(@RequestParam("tenantId") Long tenantId) {
        Map<String, Integer> result = tenantConfigSeedService.seedTenantConfig(tenantId);
        return success(result);
    }

}
