package cn.iocoder.yudao.module.custom.controller.admin.contract;

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

import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.service.contract.ContractService;

@Tag(name = "管理后台 - 合同")
@RestController
@RequestMapping("/custom/contract")
@Validated
public class ContractController {

    @Resource
    private ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('custom:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody ContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody ContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('custom:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除合同")
                @PreAuthorize("@ss.hasPermission('custom:contract:delete')")
    public CommonResult<Boolean> deleteContractList(@RequestParam("ids") List<Long> ids) {
        contractService.deleteContractListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        ContractDO contract = contractService.getContract(id);
        return success(BeanUtils.toBean(contract, ContractRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Valid ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractService.getContractPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ContractRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出合同 Excel")
    @PreAuthorize("@ss.hasPermission('custom:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContractExcel(@Valid ContractPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ContractDO> list = contractService.getContractPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "合同.xls", "数据", ContractRespVO.class,
                        BeanUtils.toBean(list, ContractRespVO.class));
    }

}