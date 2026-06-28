package cn.iocoder.yudao.module.custom.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo.*;
import cn.iocoder.yudao.module.custom.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据大盘")
@RestController
@RequestMapping("/custom/dashboard/v2")
@Validated
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "核心指标汇总")
    @PreAuthorize("@ss.hasAnyPermissions('custom:dashboard:query','mini:admin:dashboard')")
    public CommonResult<DashboardOverviewVO> overview() {
        return success(dashboardService.getOverview());
    }

    @GetMapping("/user-trend")
    @Operation(summary = "用户趋势（新增/DAU/登录时段）")
    @PreAuthorize("@ss.hasAnyPermissions('custom:dashboard:query','mini:admin:dashboard')")
    public CommonResult<UserTrendVO> userTrend() {
        return success(dashboardService.getUserTrend());
    }

    @GetMapping("/payment")
    @Operation(summary = "支付情况统计")
    @PreAuthorize("@ss.hasAnyPermissions('custom:dashboard:query','mini:admin:dashboard')")
    public CommonResult<PaymentStatsVO> payment() {
        return success(dashboardService.getPaymentStats());
    }

    @GetMapping("/face-auth")
    @Operation(summary = "人脸识别统计")
    @PreAuthorize("@ss.hasAnyPermissions('custom:dashboard:query','mini:admin:dashboard')")
    public CommonResult<FaceAuthStatsVO> faceAuth() {
        return success(dashboardService.getFaceAuthStats());
    }

    @GetMapping("/contract")
    @Operation(summary = "合同统计（状态分布/金额趋势/逾期）")
    @PreAuthorize("@ss.hasAnyPermissions('custom:dashboard:query','mini:admin:dashboard')")
    public CommonResult<ContractStatsVO> contract() {
        return success(dashboardService.getContractStats());
    }
}
