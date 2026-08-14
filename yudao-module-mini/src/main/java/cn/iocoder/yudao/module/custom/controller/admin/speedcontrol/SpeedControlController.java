package cn.iocoder.yudao.module.custom.controller.admin.speedcontrol;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.service.speedcontrol.SpeedControlConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 本 Controller 的路径被 SpeedControlFilter 硬排除，永远不限速——
 * 否则一旦把时长配错，就再也没法进来把它关掉。
 */
@Tag(name = "管理后台 - 访问速度控制")
@RestController
@RequestMapping("/custom/speed-control")
@Validated
public class SpeedControlController {

    @Resource
    private SpeedControlConfigService speedControlConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取限速配置")
    @PreAuthorize("@ss.hasPermission('custom:speed-control:query')")
    public CommonResult<SpeedControlConfigRespVO> get() {
        return success(speedControlConfigService.get());
    }

    @PutMapping("/update")
    @Operation(summary = "保存限速配置")
    @PreAuthorize("@ss.hasPermission('custom:speed-control:handle')")
    public CommonResult<Boolean> update(@Valid @RequestBody SpeedControlConfigSaveReqVO reqVO) {
        speedControlConfigService.update(reqVO);
        return success(true);
    }

}
