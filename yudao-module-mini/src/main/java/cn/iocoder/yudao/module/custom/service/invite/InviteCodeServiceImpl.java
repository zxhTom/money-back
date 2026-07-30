package cn.iocoder.yudao.module.custom.service.invite;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteRegisterLogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.invite.InviteCodeMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.invite.InviteRegisterLogMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.INVITE_CODE_INVALID;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.INVITE_NOT_ENABLED;

@Service
@Slf4j
public class InviteCodeServiceImpl implements InviteCodeService {

    /** 邀请码有效期：8 小时 */
    private static final long VALID_HOURS = 8;
    /** 邀请码字符集（去掉易混淆的 0/O/1/I/l） */
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LEN = 8;

    /** 全局开关：注册是否必须邀请码，读配置文件 system.user.invite-register-required，缺省 false（不必须） */
    @Value("${system.user.invite-register-required:false}")
    private boolean registerInviteRequired;

    /** 全局开关：注册页是否展示邀请码功能，读配置文件 system.user.invite-register-enabled，缺省 true（展示，保持现有行为） */
    @Value("${system.user.invite-register-enabled:true}")
    private boolean registerInviteEnabled;

    @Resource
    private InviteCodeMapper inviteCodeMapper;
    @Resource
    private InviteRegisterLogMapper inviteRegisterLogMapper;
    @Resource
    private AdminUserService adminUserService;

    @Override
    public boolean isRegisterInviteRequired() {
        // 功能整体关闭时，即使 required 配成 true 也不生效，避免"隐藏了输入框又要求必填"的死锁
        return registerInviteEnabled && registerInviteRequired;
    }

    @Override
    public boolean isRegisterInviteEnabled() {
        return registerInviteEnabled;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteCodeDO generate(Long inviterUserId) {
        AdminUserDO user = adminUserService.getUser(inviterUserId);
        if (user == null || !Boolean.TRUE.equals(user.getInviteEnabled())) {
            throw exception(INVITE_NOT_ENABLED);
        }
        // 同一用户同一时间段内只应有一个邀请码：如果已有一条有效且未过期的，直接复用，
        // 不要每次点击都生成新码（会让用户之前分享出去的码作废）。
        InviteCodeDO existing = inviteCodeMapper.selectActiveByInviter(inviterUserId);
        if (existing != null && existing.getExpireTime() != null && existing.getExpireTime().isAfter(LocalDateTime.now())) {
            return existing;
        }
        // 同时只有一个有效码：先把该用户旧的有效码全部失效（含上面查到的已过期但状态还是"有效"的脏数据）
        inviteCodeMapper.update(null, new LambdaUpdateWrapper<InviteCodeDO>()
                .eq(InviteCodeDO::getInviterUserId, inviterUserId)
                .eq(InviteCodeDO::getStatus, InviteCodeDO.STATUS_ACTIVE)
                .set(InviteCodeDO::getStatus, InviteCodeDO.STATUS_INVALID));

        InviteCodeDO code = new InviteCodeDO();
        code.setInviterUserId(inviterUserId);
        code.setCode(generateUniqueCode());
        code.setStatus(InviteCodeDO.STATUS_ACTIVE);
        code.setExpireTime(LocalDateTime.now().plusHours(VALID_HOURS));
        code.setRegisterCount(0);
        inviteCodeMapper.insert(code);
        return code;
    }

    @Override
    public InviteCodeDO validate(String code) {
        if (StrUtil.isBlank(code)) {
            throw exception(INVITE_CODE_INVALID);
        }
        InviteCodeDO inviteCode = inviteCodeMapper.selectByCode(code.trim());
        if (inviteCode == null
                || !Integer.valueOf(InviteCodeDO.STATUS_ACTIVE).equals(inviteCode.getStatus())
                || inviteCode.getExpireTime() == null
                || inviteCode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw exception(INVITE_CODE_INVALID);
        }
        // 邀请人必须仍处于开启邀请功能状态
        AdminUserDO inviter = adminUserService.getUser(inviteCode.getInviterUserId());
        if (inviter == null || !Boolean.TRUE.equals(inviter.getInviteEnabled())) {
            throw exception(INVITE_CODE_INVALID);
        }
        return inviteCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordRegistration(InviteCodeDO inviteCode, AdminUserDO invitee, String registerIp) {
        InviteRegisterLogDO logDO = new InviteRegisterLogDO();
        logDO.setInviteCodeId(inviteCode.getId());
        logDO.setCode(inviteCode.getCode());
        logDO.setInviterUserId(inviteCode.getInviterUserId());
        logDO.setInviteeUserId(invitee.getId());
        logDO.setInviteeUsername(invitee.getUsername());
        logDO.setInviteeRealname(invitee.getRealname());
        logDO.setRegisterIp(registerIp);
        logDO.setCreateTime(LocalDateTime.now());
        inviteRegisterLogMapper.insert(logDO);

        inviteCodeMapper.update(null, new LambdaUpdateWrapper<InviteCodeDO>()
                .eq(InviteCodeDO::getId, inviteCode.getId())
                .setSql("register_count = register_count + 1"));
    }

    @Override
    public List<InviteCodeDO> listCodesByInviter(Long inviterUserId) {
        return inviteCodeMapper.selectListByInviter(inviterUserId);
    }

    @Override
    public List<InviteRegisterLogDO> listRegistrations(Long inviteCodeId) {
        return inviteRegisterLogMapper.selectListByCodeId(inviteCodeId);
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 10; i++) {
            String code = RandomUtil.randomString(CODE_CHARS, CODE_LEN);
            if (inviteCodeMapper.selectByCode(code) == null) {
                return code;
            }
        }
        // 极小概率连续冲突，兜底加时间戳后缀
        return RandomUtil.randomString(CODE_CHARS, CODE_LEN - 2) + RandomUtil.randomString(CODE_CHARS, 2);
    }

}
