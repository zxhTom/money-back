package cn.iocoder.yudao.module.custom.controller.admin.contract;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.service.contract.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 客户端")
@RestController
@RequestMapping("/custom/contract")
@Validated
public class ContractController {

    @Resource
    private ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "创建客户端")
    @PreAuthorize("@ss.hasPermission('custom:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody ContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户端")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody ContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户端")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('custom:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除客户端")
                @PreAuthorize("@ss.hasPermission('custom:contract:delete')")
    public CommonResult<Boolean> deleteContractList(@RequestParam("ids") List<Long> ids) {
        contractService.deleteContractListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户端")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        ContractDO contract = contractService.getContract(id);
        return success(BeanUtils.toBean(contract, ContractRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户端分页")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Valid ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractService.getContractPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ContractRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户端 Excel")
    @PreAuthorize("@ss.hasPermission('custom:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContractExcel(@Valid ContractPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ContractDO> list = contractService.getContractPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "客户端.xls", "数据", ContractRespVO.class,
                        BeanUtils.toBean(list, ContractRespVO.class));
    }

}