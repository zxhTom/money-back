package cn.iocoder.yudao.module.custom.controller.admin.text;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextAppRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.service.text.TextItemService;
import cn.iocoder.yudao.module.custom.service.text.TextProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 文案")
@RestController
@RequestMapping("/custom/text/app")
@Validated
public class TextAppController {

    @Resource
    private TextProfileService textProfileService;

    @Resource
    private TextItemService textItemService;

    @GetMapping("/current")
    @Operation(summary = "获取当前生效的文案配置")
    @PermitAll
    public CommonResult<TextAppRespVO> getCurrentText(
            @RequestParam(required = false, defaultValue = "safe") String mode) {
        String textMode = "offcial".equals(mode) ? "offcial" : "safe"; // 非 safe/offcial 的非法值或缺失一律 fail-closed 到 safe
        TextProfileDO active = textProfileService.getActiveTextProfile(textMode);
        if (active == null) {
            TextAppRespVO vo = new TextAppRespVO();
            vo.setProfileCode(null);
            vo.setTexts(Collections.emptyMap());
            return success(vo);
        }

        List<TextItemDO> items = textItemService.listByProfile(active.getId());
        Map<String, String> texts = items.stream()
                .collect(Collectors.toMap(TextItemDO::getItemKey, TextItemDO::getItemValue, (a, b) -> b));
        TextAppRespVO vo = new TextAppRespVO();
        vo.setProfileCode(active.getCode());
        vo.setTexts(texts);
        return success(vo);
    }

}
