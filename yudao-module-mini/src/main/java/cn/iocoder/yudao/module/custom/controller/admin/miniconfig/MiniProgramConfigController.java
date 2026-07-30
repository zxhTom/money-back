package cn.iocoder.yudao.module.custom.controller.admin.miniconfig;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;
import cn.iocoder.yudao.module.custom.service.miniconfig.MiniProgramConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 小程序静态信息配置")
@RestController
@RequestMapping("/custom/miniprogram-config")
@Validated
public class MiniProgramConfigController {

    @Resource
    private MiniProgramConfigService miniProgramConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取小程序静态信息（小程序端调用，公开）")
    @PermitAll
    public CommonResult<MiniProgramConfigRespVO> get() {
        return success(miniProgramConfigService.getPublic());
    }

    @GetMapping("/get-admin")
    @Operation(summary = "获取完整配置（管理端调用，含绑定用户ID）")
    @PreAuthorize("@ss.hasPermission('custom:miniprogram-config:query')")
    public CommonResult<MiniProgramConfigDO> getAdmin() {
        return success(miniProgramConfigService.getAdmin());
    }

    @PutMapping("/update")
    @Operation(summary = "保存配置（可能触发姓名联动）")
    @PreAuthorize("@ss.hasPermission('custom:miniprogram-config:handle')")
    public CommonResult<Boolean> update(@RequestBody MiniProgramConfigSaveReqVO reqVO) {
        miniProgramConfigService.update(reqVO);
        return success(true);
    }

    @GetMapping("/preview-name-change-impact")
    @Operation(summary = "预览：当前绑定用户名下有多少条合同会被改名联动影响")
    @PreAuthorize("@ss.hasPermission('custom:miniprogram-config:query')")
    public CommonResult<Integer> previewNameChangeImpact() {
        return success(miniProgramConfigService.previewNameChangeImpact());
    }

}
