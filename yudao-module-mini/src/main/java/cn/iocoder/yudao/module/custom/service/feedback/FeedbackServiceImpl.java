package cn.iocoder.yudao.module.custom.service.feedback;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.feedback.vo.FeedbackSubmitRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.feedback.FeedbackDO;
import cn.iocoder.yudao.module.custom.dal.mysql.feedback.FeedbackMapper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 反馈 Service 实现类
 *
 * @author zxhtom
 */
@Service
@Validated
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    @Resource
    private FeedbackMapper feedbackMapper;

    @Override
    public FeedbackSubmitRespVO submitFeedback(FeedbackSubmitReqVO reqVO) {
        // 0. 文本内容安全检测 (FileContentAuditor unavailable)

        // 1. 验证反馈类型
        String[] validTypes = {"功能问题", "体验建议", "内容问题", "其他反馈"};
        boolean isValidType = false;
        for (String validType : validTypes) {
            if (validType.equals(reqVO.getType())) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) {
            throw new IllegalArgumentException("反馈类型必须是：功能问题、体验建议、内容问题或其他反馈");
        }

        // 2. 验证图片数量
        if (reqVO.getImageUrls() != null && reqVO.getImageUrls().size() > 4) {
            throw new IllegalArgumentException("最多只能上传4张图片");
        }

        // 3. 转换为 DO
        FeedbackDO feedback = BeanUtils.toBean(reqVO, FeedbackDO.class);

        // 4. 处理图片URL数组（转换为JSON字符串存储）
        if (reqVO.getImageUrls() != null && !reqVO.getImageUrls().isEmpty()) {
            feedback.setImageUrls(JSON.toJSONString(reqVO.getImageUrls()));
        }

        // 5. 获取当前登录用户ID（如果已登录）
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId != null) {
            feedback.setUserId(userId);
        }

        // 6. 保存到数据库
        feedbackMapper.insert(feedback);

        // 7. 返回结果
        FeedbackSubmitRespVO respVO = new FeedbackSubmitRespVO();
        respVO.setId(String.valueOf(feedback.getId()));
        return respVO;
    }

}

