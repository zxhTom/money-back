package cn.iocoder.yudao.module.custom.dal.dataobject.invite;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请注册记录 DO（仅追加，不做逻辑删除）
 */
@TableName("custom_invite_register_log")
@Data
public class InviteRegisterLogDO {

    private Long id;
    private Long inviteCodeId;
    private String code;
    private Long inviterUserId;
    private Long inviteeUserId;
    private String inviteeUsername;
    private String inviteeRealname;
    private String registerIp;
    private LocalDateTime createTime;

}
