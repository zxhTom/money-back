package cn.iocoder.yudao.module.custom.dal.dataobject.invite;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 邀请码 DO
 *
 * 同一用户同时只有一个有效码（status=0）；生成新码会把旧码置为 status=1。
 */
@TableName("custom_invite_code")
@Data
@EqualsAndHashCode(callSuper = true)
public class InviteCodeDO extends BaseDO {

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_INVALID = 1;

    private Long id;
    private Long inviterUserId;
    private String code;
    private Integer status;
    private LocalDateTime expireTime;
    private Integer registerCount;

}
