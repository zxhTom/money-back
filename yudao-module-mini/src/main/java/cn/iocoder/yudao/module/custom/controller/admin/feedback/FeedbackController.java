package cn.iocoder.yudao.module.custom.controller.admin.feedback;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitRespVO;
import cn.iocoder.yudao.module.custom.service.feedback.FeedbackService;
import cn.iocoder.yudao.module.custom.service.feedback.FeedbackNotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

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

    @Resource
    private FeedbackNotifyService feedbackNotifyService;

    @PostMapping("/submit")
    @Operation(summary = "提交反馈")
    public CommonResult<FeedbackSubmitRespVO> submitFeedback(@Valid @RequestBody FeedbackSubmitReqVO reqVO) {
        FeedbackSubmitRespVO respVO = feedbackService.submitFeedback(reqVO);
        return success(respVO);
    }


    @GetMapping("/page")
    @Operation(summary = "获得反馈分页")
    public CommonResult<cn.iocoder.yudao.framework.common.pojo.PageResult<cn.iocoder.yudao.module.custom.dal.dataobject.feedback.FeedbackDO>> getFeedbackPage(@Valid cn.iocoder.yudao.framework.common.pojo.PageParam pageReqVO) {
        return success(feedbackService.getFeedbackPage(pageReqVO));
    }

    @GetMapping("/notify-config")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @Operation(summary = "获取反馈通知配置")
    public CommonResult<FeedbackNotifyService.Config> getNotifyConfig() {
        return success(feedbackNotifyService.getConfig());
    }

    @GetMapping("/notify-admins")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @Operation(summary = "获取可接收反馈通知的管理员")
    public CommonResult<List<Long>> getNotifyAdmins() {
        return success(feedbackNotifyService.getAdminIds());
    }

    @GetMapping("/mini/notices")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "小程序查看当前管理员的反馈提醒")
    public CommonResult<List<FeedbackNotifyService.MiniNotice>> getMiniNotices() {
        return success(feedbackNotifyService.getMiniNotices());
    }

    @PutMapping("/notify-config")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @Operation(summary = "保存反馈通知配置")
    public CommonResult<Boolean> saveNotifyConfig(@RequestBody FeedbackNotifyService.Config config) {
        feedbackNotifyService.saveConfig(config);
        return success(true);
    }

}
