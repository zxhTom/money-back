package cn.iocoder.yudao.module.custom.controller.admin.custom;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 自定义客户端")
@RestController
@RequestMapping("/custom/contract/dashboard")
@Validated
public class CustomDefineController {

    @Resource
    private CustomDefineService customDefineService;

    @GetMapping("/staticsContractByTimePeriod")
    @Operation(summary = "按照时间窗口统计数据")
    @PreAuthorize("@ss.hasPermission('custom:contract:statics')")
    public CommonResult<StaticsContractPeriodRespVO> staticsContractByTimePeriod() {
        return success(customDefineService.staticsContractByTimePeriod());
    }
    @GetMapping("/user-dimension")
    @Operation(summary = "统计用户状态下数量")
    @PreAuthorize("@ss.hasPermission('custom:contract:dimension')")
    public CommonResult<DimensionCombineRespVo> userDimension() {
        return success(customDefineService.userDimension());
    }

    @GetMapping("/rencentContractList")
    @Operation(summary = "最近联系")
    @PreAuthorize("@ss.hasPermission('custom:contract:recent')")
    public CommonResult<List<RecentContractVO>> rencentContractList() {
        return success(customDefineService.rencentContractList());
    }

    @GetMapping("/creditSearch")
    @Operation(summary = "最近联系")
    @PreAuthorize("@ss.hasPermission('custom:contract:credit')")
    public CommonResult<Page<CreditSearchVO>> creditSearch(CreditPageReqVO creditPageReqVO) {
        return success(customDefineService.creditSearch(creditPageReqVO));
    }
}
