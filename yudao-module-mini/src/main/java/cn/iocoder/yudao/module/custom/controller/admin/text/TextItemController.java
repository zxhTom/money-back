package cn.iocoder.yudao.module.custom.controller.admin.text;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.PageCatalogNodeVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextItemBatchUpdateReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextItemRespVO;
import cn.iocoder.yudao.module.custom.framework.text.PageCatalog;
import cn.iocoder.yudao.module.custom.service.text.TextItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文案条目")
@RestController
@RequestMapping("/custom/text/item")
@Validated
public class TextItemController {

    @Resource
    private TextItemService textItemService;

    @GetMapping("/page-tree")
    @Operation(summary = "获得页面清单树")
    @PreAuthorize("@ss.hasPermission('custom:text:query')")
    public CommonResult<List<PageCatalogNodeVO>> getPageTree() {
        return success(PageCatalog.getCatalogTree());
    }

    @GetMapping("/list")
    @Operation(summary = "获得某文案套下某页面的文案条目列表")
    @Parameter(name = "profileId", description = "文案套编号", required = true, example = "1")
    @Parameter(name = "pageKey", description = "页面标识", required = true, example = "contract.contractDetail")
    @PreAuthorize("@ss.hasPermission('custom:text:query')")
    public CommonResult<List<TextItemRespVO>> getTextItemList(@RequestParam("profileId") Long profileId,
                                                                @RequestParam("pageKey") String pageKey) {
        return success(BeanUtils.toBean(textItemService.listByProfileAndPage(profileId, pageKey), TextItemRespVO.class));
    }

    @PutMapping("/batch-update")
    @Operation(summary = "批量保存文案条目")
    @PreAuthorize("@ss.hasPermission('custom:text:update')")
    public CommonResult<Boolean> batchUpdateTextItem(@Valid @RequestBody TextItemBatchUpdateReqVO updateReqVO) {
        textItemService.batchUpdate(updateReqVO);
        return success(true);
    }

    @GetMapping("/search")
    @Operation(summary = "按关键字搜索文案条目")
    @Parameter(name = "profileId", description = "文案套编号", required = true, example = "1")
    @Parameter(name = "keyword", description = "关键字", required = true, example = "标题")
    @PreAuthorize("@ss.hasPermission('custom:text:query')")
    public CommonResult<List<TextItemRespVO>> searchTextItem(@RequestParam("profileId") Long profileId,
                                                                @RequestParam("keyword") String keyword) {
        return success(BeanUtils.toBean(textItemService.searchByKeyword(profileId, keyword), TextItemRespVO.class));
    }

}
