package cn.iocoder.yudao.module.custom.controller.admin.skin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinAppRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import cn.iocoder.yudao.module.custom.service.skin.SkinProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 皮肤")
@RestController
@RequestMapping("/custom/skin/app")
@Validated
public class SkinAppController {

    @Resource
    private SkinProfileService skinProfileService;

    @GetMapping("/current")
    @Operation(summary = "获取当前生效的皮肤配置")
    @PermitAll
    public CommonResult<SkinAppRespVO> getCurrentSkin() {
        SkinProfileDO active = skinProfileService.getActiveSkinProfile();
        return success(active != null ? toRespVO(active) : defaultRespVO());
    }

    private static SkinAppRespVO toRespVO(SkinProfileDO skin) {
        SkinAppRespVO vo = new SkinAppRespVO();
        vo.setCode(skin.getCode());
        vo.setConfigMode(skin.getConfigMode());
        vo.setTokens(skin.getTokens());
        vo.setCustomCssText(skin.getCustomCssText());
        return vo;
    }

    /** 找不到当前生效皮肤时的兜底默认值，避免小程序端拿到 null 报错 */
    private static SkinAppRespVO defaultRespVO() {
        SkinAppRespVO vo = new SkinAppRespVO();
        vo.setConfigMode(SkinProfileDO.CONFIG_MODE_BASIC);
        vo.setTokens(Collections.emptyMap());
        vo.setCustomCssText(null);
        return vo;
    }

}
