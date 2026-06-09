package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessConfigDO;
import cn.iocoder.yudao.module.system.service.monitor.DataAccessMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据访问监控配置")
@RestController
@RequestMapping("/system/data-access/config")
@Validated
public class DataAccessConfigController {

    @Resource
    private DataAccessMonitorService dataAccessMonitorService;

    @PostMapping("/create")
    @Operation(summary = "创建访问监控配置")
    @PreAuthorize("@ss.hasPermission('system:data-access:config:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody DataAccessConfigSaveReqVO reqVO) {
        return success(dataAccessMonitorService.createConfig(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新访问监控配置")
    @PreAuthorize("@ss.hasPermission('system:data-access:config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody DataAccessConfigSaveReqVO reqVO) {
        dataAccessMonitorService.updateConfig(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除访问监控配置")
    @Parameter(name = "id", description = "配置ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:data-access:config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam Long id) {
        dataAccessMonitorService.deleteConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取访问监控配置详情")
    @Parameter(name = "id", description = "配置ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:data-access:config:query')")
    public CommonResult<DataAccessConfigDO> getConfig(@RequestParam Long id) {
        return success(dataAccessMonitorService.getConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询访问监控配置")
    @PreAuthorize("@ss.hasPermission('system:data-access:config:query')")
    public CommonResult<PageResult<DataAccessConfigDO>> getConfigPage(@Valid DataAccessConfigPageReqVO pageReqVO) {
        return success(dataAccessMonitorService.getConfigPage(pageReqVO));
    }
}
