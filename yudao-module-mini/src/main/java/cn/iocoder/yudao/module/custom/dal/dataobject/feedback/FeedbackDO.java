package cn.iocoder.yudao.module.custom.dal.dataobject.feedback;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 反馈 DO
 *
 * @author zxhtom
 */
@TableName("system_feedback")
@KeySequence("system_feedback_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 小程序AppID
     */
    private String appId;

    /**
     * 小程序版本号
     */
    private String appVersion;

    /**
     * 反馈类型
     * "功能问题" / "体验建议" / "内容问题" / "其他反馈"
     */
    private String type;

    /**
     * 问题描述
     */
    private String content;

    /**
     * 联系方式（手机号或邮箱）
     */
    private String contactInfo;

    /**
     * 图片URL数组（JSON格式存储）
     */
    private String imageUrls;

    /**
     * 用户ID（如果已登录）
     */
    private Long userId;

}

