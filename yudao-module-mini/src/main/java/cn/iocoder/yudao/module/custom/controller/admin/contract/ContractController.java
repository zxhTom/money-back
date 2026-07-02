package cn.iocoder.yudao.module.custom.controller.admin.contract;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.custom.framework.audit.annotation.AuditLog;
import cn.iocoder.yudao.module.custom.framework.audit.annotation.AuditOperationType;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.service.contract.ConfirmPdfService;
import cn.iocoder.yudao.module.custom.service.contract.ContractHoneypotFactory;
import cn.iocoder.yudao.module.custom.service.contract.ContractService;
import cn.iocoder.yudao.module.custom.util.ContractRespIdCardEnricher;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.monitor.DataAccessMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 合同")
@RestController
@RequestMapping("/custom/contract")
@Validated
@Slf4j
public class ContractController {

    @Resource
    private ConfirmPdfService confirmPdfService;
    @Resource
    private ContractService contractService;
    @Resource
    private IdCardCipherService idCardCipherService;
    @Resource
    private DataAccessMonitorService dataAccessMonitorService;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('custom:contract:create')")
    @AuditLog(module = "合同管理", type = AuditOperationType.CREATE, entityType = "contract",
            operation = "创建合同-{{#createReqVO.indebtedName}}->{{#createReqVO.creditorName}}",
            entityIdExpression = "#result.data")
    public CommonResult<Long> createContract(@Valid @RequestBody ContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    @AuditLog(module = "合同管理", type = AuditOperationType.UPDATE, entityType = "contract",
            operation = "更新合同ID={{#updateReqVO.id}}", entityIdExpression = "#updateReqVO.id")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody ContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('custom:contract:delete')")
    @AuditLog(module = "合同管理", type = AuditOperationType.DELETE, entityType = "contract",
            operation = "删除合同ID={{#id}}", entityIdExpression = "#id")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除合同")
    @PreAuthorize("@ss.hasPermission('custom:contract:delete')")
    @AuditLog(module = "合同管理", type = AuditOperationType.DELETE, entityType = "contract",
            operation = "批量删除合同IDs={{#ids}}")
    public CommonResult<Boolean> deleteContractList(@RequestParam("ids") List<Long> ids) {
        contractService.deleteContractListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    @AuditLog(module = "合同管理", type = AuditOperationType.READ, entityType = "contract",
            operation = "查看合同详情ID={{#id}}", entityIdExpression = "#id")
    @ApiAccessLog(responseEnable = false)
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id, HttpServletRequest request) {
        ContractDO contract = contractService.getContract(id);
        if (contract == null) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil
                    .exception(cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.CONTRACT_NOT_EXISTS);
        }
        // 蜜罐命中：写数据访问日志 + 清除内部标记
        if (ContractHoneypotFactory.HONEYPOT_CREATOR_MARKER.equals(contract.getCreator())) {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            String ip = cn.hutool.extra.servlet.ServletUtil.getClientIP(request);
            log.warn("[SECURITY][HONEYPOT] userId={} ip={} 合同查询ID={} 命中蜜罐", userId, ip, id);
            String username = SecurityFrameworkUtils.getLoginUserNickname();
            dataAccessMonitorService.recordAccess(userId, username, "合同管理", "contract",
                    "{\"id\":" + id + ",\"honeypot\":true}",
                    Collections.singletonList(id), 1,
                    request.getRequestURI(), ip);
            contract.setCreator(null);
        }
        ContractRespVO vo = BeanUtils.toBean(contract, ContractRespVO.class);
        ContractRespIdCardEnricher.enrich(vo, contract, idCardCipherService);
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    @cn.iocoder.yudao.module.system.framework.monitor.annotation.DataAccessMonitor(module = "合同管理", entityType = "contract")
    @ApiAccessLog(responseEnable = false)
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Valid ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractService.getContractPage(pageReqVO);
        PageResult<ContractRespVO> voPage = BeanUtils.toBean(pageResult, ContractRespVO.class);
        for (int i = 0; i < voPage.getList().size(); i++) {
            ContractRespIdCardEnricher.enrich(voPage.getList().get(i), pageResult.getList().get(i), idCardCipherService);
        }
        return success(voPage);
    }

    @GetMapping("/selfPage")
    @Operation(summary = "获得当前登录用户创建的合同分页")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    @cn.iocoder.yudao.module.system.framework.monitor.annotation.DataAccessMonitor(module = "合同管理", entityType = "contract")
    @ApiAccessLog(responseEnable = false)
    public CommonResult<PageResult<ContractRespVO>> getSelfContractPage(@Valid ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractService.getSelfContractPage(pageReqVO);
        PageResult<ContractRespVO> voPage = BeanUtils.toBean(pageResult, ContractRespVO.class);
        for (int i = 0; i < voPage.getList().size(); i++) {
            ContractRespIdCardEnricher.enrich(voPage.getList().get(i), pageResult.getList().get(i), idCardCipherService);
        }
        return success(voPage);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出合同 Excel")
    @PreAuthorize("@ss.hasPermission('custom:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    @AuditLog(module = "合同管理", type = AuditOperationType.EXPORT, operation = "导出合同Excel")
    public void exportContractExcel(@Valid ContractPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ContractDO> list = contractService.getContractPage(pageReqVO).getList();
        List<ContractRespVO> voList = BeanUtils.toBean(list, ContractRespVO.class);
        for (int i = 0; i < voList.size(); i++) {
            ContractRespIdCardEnricher.enrich(voList.get(i), list.get(i), idCardCipherService);
        }
        ExcelUtils.write(response, "合同.xls", "数据", ContractRespVO.class, voList);
    }

    @GetMapping("/export-protocol-pdf")
    @Operation(summary = "导出合同协议 PDF")
    @Parameter(name = "id", description = "合同编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('custom:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContractProtocolPdf(@RequestParam("id") Long id,
                                          HttpServletResponse response) throws IOException {
        contractService.exportContractProtocolPdf(id, response);
    }
    @PostMapping("/export-protocol-contract-pdf")
    @Operation(summary = "导出合同协议 PDF")
    @Parameter(name = "id", description = "合同编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('custom:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContractProtocolContractPdf(@RequestBody ContractDO contractDO,
                                          HttpServletResponse response) throws IOException {
        contractService.exportContractProtocolContractPdf(contractDO, response);
    }

    @GetMapping("/export-confirm-pdf")
    @Operation(summary = "导出确认合同 PDF")
    @Parameter(name = "id", description = "合同编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('custom:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public ResponseEntity<byte[]> exportPdf() {
        try {
            byte[] pdfBytes = confirmPdfService.generateAuthorizationConfirmationPdf();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // 设置文件名，让浏览器下载
            String filename = "借款协议_可编辑.pdf";
            // 为了兼容性，建议使用 ASCII 编码的文件名，或者对中文进行编码
            // 这里使用简单的中文文件名，现代浏览器通常支持
            headers.setContentDispositionFormData(filename, filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            // 实际应用中应该返回更友好的错误信息
            return new ResponseEntity<>(("PDF生成失败: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}