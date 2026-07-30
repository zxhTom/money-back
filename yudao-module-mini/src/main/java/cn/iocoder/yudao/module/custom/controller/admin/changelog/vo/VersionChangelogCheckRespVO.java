package cn.iocoder.yudao.module.custom.controller.admin.changelog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "版本升级说明检查 Response VO")
@Data
public class VersionChangelogCheckRespVO {

    @Schema(description = "是否需要弹出")
    private Boolean shouldShow;

    @Schema(description = "公告标题（shouldShow=false 时为 null）")
    private String title;

    @Schema(description = "公告正文（shouldShow=false 时为 null）")
    private String content;

    public static VersionChangelogCheckRespVO notShow() {
        VersionChangelogCheckRespVO vo = new VersionChangelogCheckRespVO();
        vo.setShouldShow(false);
        return vo;
    }

    public static VersionChangelogCheckRespVO show(String title, String content) {
        VersionChangelogCheckRespVO vo = new VersionChangelogCheckRespVO();
        vo.setShouldShow(true);
        vo.setTitle(title);
        vo.setContent(content);
        return vo;
    }

}
