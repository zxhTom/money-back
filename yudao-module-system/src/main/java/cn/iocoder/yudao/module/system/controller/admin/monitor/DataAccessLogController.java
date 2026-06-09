package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessLogDO;
import cn.iocoder.yudao.module.system.service.monitor.DataAccessMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据访问日志")
@RestController
@RequestMapping("/system/data-access/log")
@Validated
public class DataAccessLogController {

    @Resource
    private DataAccessMonitorService dataAccessMonitorService;

    @GetMapping("/page")
    @Operation(summary = "分页查询数据访问日志")
    @PreAuthorize("@ss.hasPermission('system:data-access:log:query')")
    public CommonResult<PageResult<DataAccessLogDO>> getLogPage(@Valid DataAccessLogPageReqVO pageReqVO) {
        return success(dataAccessMonitorService.getLogPage(pageReqVO));
    }
}
