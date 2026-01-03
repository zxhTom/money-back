package cn.iocoder.yudao.module.custom.controller.admin.feedback.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 反馈提交响应 VO
 *
 * @author zxhtom
 */
@Schema(description = "管理后台 - 反馈提交响应")
@Data
public class FeedbackSubmitRespVO {

    @Schema(description = "反馈ID", example = "feedback_id_123")
    private String id;

}

