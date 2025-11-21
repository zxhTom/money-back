package cn.iocoder.yudao.module.fee.controller.admin.strategy;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.*;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import cn.iocoder.yudao.module.fee.service.strategy.StrategyService;

@Tag(name = "管理后台 - 费用策略")
@RestController
@RequestMapping("/fee/strategy")
@Validated
public class StrategyController {

    @Resource
    private StrategyService strategyService;

    @PostMapping("/create")
    @Operation(summary = "创建费用策略")
    @PreAuthorize("@ss.hasPermission('fee:strategy:create')")
    public CommonResult<Long> createStrategy(@Valid @RequestBody StrategySaveReqVO createReqVO) {
        return success(strategyService.createStrategy(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新费用策略")
    @PreAuthorize("@ss.hasPermission('fee:strategy:update')")
    public CommonResult<Boolean> updateStrategy(@Valid @RequestBody StrategySaveReqVO updateReqVO) {
        strategyService.updateStrategy(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除费用策略")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('fee:strategy:delete')")
    public CommonResult<Boolean> deleteStrategy(@RequestParam("id") Long id) {
        strategyService.deleteStrategy(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除费用策略")
                @PreAuthorize("@ss.hasPermission('fee:strategy:delete')")
    public CommonResult<Boolean> deleteStrategyList(@RequestParam("ids") List<Long> ids) {
        strategyService.deleteStrategyListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得费用策略")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fee:strategy:query')")
    public CommonResult<StrategyRespVO> getStrategy(@RequestParam("id") Long id) {
        StrategyDO strategy = strategyService.getStrategy(id);
        return success(BeanUtils.toBean(strategy, StrategyRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得费用策略分页")
    @PreAuthorize("@ss.hasPermission('fee:strategy:query')")
    public CommonResult<PageResult<StrategyRespVO>> getStrategyPage(@Valid StrategyPageReqVO pageReqVO) {
        PageResult<StrategyDO> pageResult = strategyService.getStrategyPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StrategyRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出费用策略 Excel")
    @PreAuthorize("@ss.hasPermission('fee:strategy:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStrategyExcel(@Valid StrategyPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StrategyDO> list = strategyService.getStrategyPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "费用策略.xls", "数据", StrategyRespVO.class,
                        BeanUtils.toBean(list, StrategyRespVO.class));
    }

}