package cn.iocoder.yudao.module.custom.controller.admin.text.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 页面清单树节点 VO")
@Data
public class PageCatalogNodeVO {

    @Schema(description = "页面/分包标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "contract.contractDetail")
    private String key;

    @Schema(description = "展示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "合同详情")
    private String label;

    @Schema(description = "子节点")
    private List<PageCatalogNodeVO> children;

}
