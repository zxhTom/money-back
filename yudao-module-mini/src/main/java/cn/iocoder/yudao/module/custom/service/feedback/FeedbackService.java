package cn.iocoder.yudao.module.custom.service.feedback;

import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitRespVO;

/**
 * 反馈 Service 接口
 *
 * @author zxhtom
 */
public interface FeedbackService {

    /**
     * 提交反馈
     *
     * @param reqVO 反馈提交请求
     * @return 反馈ID
     */
    FeedbackSubmitRespVO submitFeedback(FeedbackSubmitReqVO reqVO);

}

