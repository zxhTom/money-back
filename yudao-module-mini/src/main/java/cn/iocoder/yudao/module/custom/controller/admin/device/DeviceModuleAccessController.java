package cn.iocoder.yudao.module.custom.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessCheckRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessPageItemRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessSetReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessUpdateReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.device.DeviceModuleAccessDO;
import cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants;
import cn.iocoder.yudao.module.custom.service.device.DeviceModuleAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 设备模块开放控制")
@RestController
@RequestMapping("/custom/device-module-access")
@Validated
public class DeviceModuleAccessController {

    @Resource
    private DeviceModuleAccessService deviceModuleAccessService;

    @PostMapping("/set")
    @Operation(summary = "设置指定模块在指定设备上是否开放")
    @PreAuthorize("@ss.hasPermission('custom:deviceModuleAccess:set')")
    public CommonResult<Boolean> setAccess(@Valid @RequestBody DeviceModuleAccessSetReqVO reqVO) {
        deviceModuleAccessService.setAccess(reqVO);
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "按主键更新模块名、设备 ID、是否开放（弹窗编辑）")
    @PreAuthorize("@ss.hasPermission('custom:deviceModuleAccess:update')")
    public CommonResult<Boolean> updateAccess(@Valid @RequestBody DeviceModuleAccessUpdateReqVO reqVO) {
        deviceModuleAccessService.updateAccess(reqVO);
        return success(true);
    }

    @GetMapping("/enabled")
    @Operation(summary = "查询指定模块在指定设备上是否开放")
    @PermitAll
    public CommonResult<DeviceModuleAccessCheckRespVO> checkEnabled(
            @RequestParam("moduleName") @NotBlank(message = "模块名不能为空") String moduleName,
            @RequestParam("deviceId") @NotBlank(message = "设备ID不能为空") String deviceId) {
        boolean enabled = deviceModuleAccessService.isEnabled(moduleName, deviceId);
        return success(new DeviceModuleAccessCheckRespVO(moduleName.trim(), deviceId.trim(), enabled));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询模块-设备开放配置")
    @PreAuthorize("@ss.hasPermission('custom:deviceModuleAccess:set')")
    public CommonResult<PageResult<DeviceModuleAccessPageItemRespVO>> page(@Valid DeviceModuleAccessPageReqVO reqVO) {
        PageResult<DeviceModuleAccessDO> page = deviceModuleAccessService.getPage(reqVO);
        return success(BeanUtils.toBean(page, DeviceModuleAccessPageItemRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "按主键查询单条配置（编辑前拉最新数据等场景，可选）")
    @PreAuthorize("@ss.hasPermission('custom:deviceModuleAccess:set')")
    public CommonResult<DeviceModuleAccessPageItemRespVO> get(@RequestParam("id") @NotNull(message = "id 不能为空") Long id) {
        DeviceModuleAccessDO row = deviceModuleAccessService.getById(id);
        if (row == null) {
            return error(CustomErrorCodeConstants.DEVICE_MODULE_ACCESS_NOT_EXISTS);
        }
        return success(BeanUtils.toBean(row, DeviceModuleAccessPageItemRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除指定模块-设备开放配置")
    @PreAuthorize("@ss.hasPermission('custom:deviceModuleAccess:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") @NotNull(message = "id 不能为空") Long id) {
        deviceModuleAccessService.deleteById(id);
        return success(true);
    }
}

