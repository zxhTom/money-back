package cn.iocoder.yudao.module.custom.controller.admin.audit;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.custom.controller.admin.audit.vo.AuditLogPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.audit.vo.AuditLogRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import cn.iocoder.yudao.module.custom.service.audit.AuditLogService;
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

@Tag(name = "管理后台 - 用户行为审计日志")
@RestController
@RequestMapping("/custom/audit-log")
@Validated
public class AuditLogController {

    @Resource
    private AuditLogService auditLogService;

    @GetMapping("/page")
    @Operation(summary = "分页查询审计日志")
    @PreAuthorize("@ss.hasPermission('custom:audit-log:query')")
    public CommonResult<PageResult<AuditLogRespVO>> getAuditLogPage(@Valid AuditLogPageReqVO pageReqVO) {
        PageResult<AuditLogDO> page = auditLogService.getPage(pageReqVO);
        return success(BeanUtils.toBean(page, AuditLogRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "查询单条审计日志详情")
    @Parameter(name = "id", description = "日志ID", required = true)
    @PreAuthorize("@ss.hasPermission('custom:audit-log:query')")
    public CommonResult<AuditLogRespVO> getAuditLog(@RequestParam("id") Long id) {
        AuditLogDO log = auditLogService.get(id);
        return success(BeanUtils.toBean(log, AuditLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出审计日志 Excel")
    @PreAuthorize("@ss.hasPermission('custom:audit-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAuditLogExcel(@Valid AuditLogPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE);
        List<AuditLogDO> list = auditLogService.getPage(pageReqVO).getList();
        ExcelUtils.write(response, "审计日志.xls", "数据", AuditLogRespVO.class,
                BeanUtils.toBean(list, AuditLogRespVO.class));
    }
}
