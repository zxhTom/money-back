package cn.iocoder.yudao.module.custom.controller.admin.custom;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
    @Operation(summary = "借用统计")
    @PreAuthorize("@ss.hasPermission('custom:contract:credit')")
    public CommonResult<Page<CreditSearchVO>> creditSearch(CreditPageReqVO creditPageReqVO) {
        return success(customDefineService.creditSearch(creditPageReqVO));
    }
    @PutMapping("/edit")
    @Operation(summary = "补合约")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Integer> edit(@RequestBody ContractSaveReqVO contractSaveReqVO) {
        return success(customDefineService.edit(contractSaveReqVO));
    }

    @GetMapping("/totalInfos")
    @Operation(summary = "合同总数数据")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<TotalInfosRespVO> totalInfos(@Valid ContractPageReqVO pageReqVO) {
        TotalInfosRespVO totalInfosRespVO = customDefineService.totalInfos(pageReqVO);
        return success(totalInfosRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "不分页数据")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<List<ContractDO>> page(@Valid ContractPageReqVO pageReqVO) {
        List<ContractDO> respVO = customDefineService.page(pageReqVO);
        return success(respVO);
    }
    @PostMapping("/checkUserInfo")
    @Operation(summary = "校验用户是否匹配")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<Boolean> checkUserInfo(@Valid UserReqVO userReqVO) {
        Boolean valid = customDefineService.checkUserInfo(userReqVO);
        return success(valid);
    }

    @PostMapping("/getPayOrder")
    @Operation(summary = "获取支付订单")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<PayOrderVO> getPayOrder(@Valid @RequestBody PayOrderVO payOrderVO) {
        PayOrderVO res = customDefineService.getPayOrder(payOrderVO);
        return success(res);
    }
}
