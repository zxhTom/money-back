package cn.iocoder.yudao.module.custom.controller.admin.feedback;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitRespVO;
import cn.iocoder.yudao.module.custom.service.feedback.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 反馈 Controller
 *
 * @author zxhtom
 */
@Tag(name = "管理后台 - 反馈")
@RestController
@RequestMapping("/system/feedback")
@Validated
@Slf4j
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @PostMapping("/submit")
    @Operation(summary = "提交反馈")
    public CommonResult<FeedbackSubmitRespVO> submitFeedback(@Valid @RequestBody FeedbackSubmitReqVO reqVO) {
        FeedbackSubmitRespVO respVO = feedbackService.submitFeedback(reqVO);
        return success(respVO);
    }

}

