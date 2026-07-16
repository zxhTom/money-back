package cn.iocoder.yudao.module.custom.service.invite;

import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteRegisterLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;

import java.util.List;

public interface InviteCodeService {

    /**
     * 全局开关：注册是否必须邀请码。
     * 读配置 system.user.invite-register-required（true 必须；其它/缺省 不必须）。
     */
    boolean isRegisterInviteRequired();

    /**
     * 为当前用户生成邀请码（同时只有一个有效码：旧码全部置为失效）。
     * 未开启邀请功能会抛 INVITE_NOT_ENABLED。
     */
    InviteCodeDO generate(Long inviterUserId);

    /**
     * 校验邀请码有效性（存在/未过期/未失效/邀请人已开启邀请功能）。
     * 无效抛 INVITE_CODE_INVALID；返回对应的有效邀请码。
     */
    InviteCodeDO validate(String code);

    /**
     * 记录一次成功注册：写 register_log + 邀请码 register_count+1。
     */
    void recordRegistration(InviteCodeDO inviteCode, AdminUserDO invitee, String registerIp);

    /** 管理端：查看某用户生成的邀请码记录 */
    List<InviteCodeDO> listCodesByInviter(Long inviterUserId);

    /** 管理端：查看某邀请码下的注册名单 */
    List<InviteRegisterLogDO> listRegistrations(Long inviteCodeId);

}
