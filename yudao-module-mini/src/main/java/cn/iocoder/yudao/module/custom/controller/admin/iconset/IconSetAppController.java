package cn.iocoder.yudao.module.custom.controller.admin.iconset;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetAppRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import cn.iocoder.yudao.module.custom.service.iconset.IconSetProfileService;
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

@Tag(name = "小程序 - 图标集")
@RestController
@RequestMapping("/custom/icon-set/app")
@Validated
public class IconSetAppController {

    @Resource
    private IconSetProfileService iconSetProfileService;

    @GetMapping("/current")
    @Operation(summary = "获取当前生效的图标集配置")
    @PermitAll
    public CommonResult<IconSetAppRespVO> getCurrentIconSet() {
        IconSetProfileDO active = iconSetProfileService.getActiveIconSetProfile();
        return success(active != null ? toRespVO(active) : defaultRespVO());
    }

    private static IconSetAppRespVO toRespVO(IconSetProfileDO iconSet) {
        IconSetAppRespVO vo = new IconSetAppRespVO();
        vo.setCode(iconSet.getCode());
        vo.setIcons(iconSet.getIcons());
        return vo;
    }

    /** 找不到当前生效图标集时的兜底默认值；icons 留空，小程序端 iconSetLoader.js 自带的 DEFAULT_ICONS 兜底渲染 */
    private static IconSetAppRespVO defaultRespVO() {
        IconSetAppRespVO vo = new IconSetAppRespVO();
        vo.setIcons(Collections.emptyMap());
        return vo;
    }

}
