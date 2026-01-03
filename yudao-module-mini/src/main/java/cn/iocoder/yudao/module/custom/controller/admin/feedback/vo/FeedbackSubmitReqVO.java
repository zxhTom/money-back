package cn.iocoder.yudao.module.custom.controller.admin.feedback.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 反馈提交请求 VO
 *
 * @author zxhtom
 */
@Schema(description = "管理后台 - 反馈提交请求")
@Data
public class FeedbackSubmitReqVO {

    @Schema(description = "小程序AppID", required = true, example = "wx7641cb883b4fa670")
    @NotBlank(message = "小程序AppID不能为空")
    private String appId;

    @Schema(description = "小程序版本号", required = true, example = "1.0.0")
    @NotBlank(message = "小程序版本号不能为空")
    private String appVersion;

    @Schema(description = "反馈类型", required = true, example = "功能问题", 
            allowableValues = {"功能问题", "体验建议", "内容问题", "其他反馈"})
    @NotBlank(message = "反馈类型不能为空")
    @javax.validation.constraints.Pattern(regexp = "^(功能问题|体验建议|内容问题|其他反馈)$", message = "反馈类型必须是：功能问题、体验建议、内容问题或其他反馈")
    private String type;

    @Schema(description = "问题描述", required = true, example = "在使用合同创建功能时，发现日期选择器无法正常工作，请修复。")
    @NotBlank(message = "问题描述不能为空")
    @Size(min = 10, max = 500, message = "问题描述长度必须在10-500个字符之间")
    private String content;

    @Schema(description = "联系方式（手机号或邮箱）", example = "13800138000")
    private String contactInfo;

    @Schema(description = "图片URL数组", example = "[\"https://example.com/uploads/feedback/image1.jpg\"]")
    @Size(max = 4, message = "最多只能上传4张图片")
    private List<String> imageUrls;

}

