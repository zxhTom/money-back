package cn.iocoder.yudao.module.custom.framework.invite;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO;
import cn.iocoder.yudao.module.custom.service.invite.InviteCodeService;
import cn.iocoder.yudao.module.custom.service.invite.RegisterRiskControlGuard;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthRegisterReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.INVITE_CODE_REQUIRED;

/**
 * web 端注册（/system/auth/register）的邀请码 + 风控增强。
 *
 * 因为 web 注册流程在 yudao-module-system，而邀请/风控能力在 yudao-module-mini，
 * system 无法反向依赖 mini，故用本切面在 mini 侧织入 AdminAuthServiceImpl.register。
 */
@Aspect
@Component
@Slf4j
public class WebRegisterInviteAspect {

    @Resource
    private InviteCodeService inviteCodeService;
    @Resource
    private RegisterRiskControlGuard registerRiskControlGuard;
    @Resource
    private AdminUserService adminUserService;

    @Around("execution(* cn.iocoder.yudao.module.system.service.auth.AdminAuthService.register(..)) && args(reqVO)")
    public Object aroundRegister(ProceedingJoinPoint pjp, AuthRegisterReqVO reqVO) throws Throwable {
        String ip = currentClientIp();
        // 1. 注册频率风控（超阈值直接抛异常拒绝本次）——始终生效
        registerRiskControlGuard.beforeRegister(ip);
        // 2. 邀请码校验：仅当全局开关开启时才强制；关闭则不校验、不记录
        InviteCodeDO inviteCode = null;
        if (inviteCodeService.isRegisterInviteRequired()) {
            if (StrUtil.isBlank(reqVO.getInviteCode())) {
                throw exception(INVITE_CODE_REQUIRED);
            }
            inviteCode = inviteCodeService.validate(reqVO.getInviteCode());
        }
        // 3. 放行注册
        Object result = pjp.proceed();
        // 4. 记录邀请注册（仅当开关开启且有有效邀请码时）
        try {
            if (inviteCode != null
                    && result instanceof AuthLoginRespVO && ((AuthLoginRespVO) result).getUserId() != null) {
                Long newUserId = ((AuthLoginRespVO) result).getUserId();
                AdminUserDO invitee = adminUserService.getUser(newUserId);
                if (invitee != null) {
                    inviteCodeService.recordRegistration(inviteCode, invitee, ip);
                }
            }
        } catch (Exception e) {
            log.warn("[WebRegisterInvite] 记录邀请注册失败 code={} err={}", inviteCode.getCode(), e.getMessage());
        }
        return result;
    }

    private String currentClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return cn.hutool.extra.servlet.ServletUtil.getClientIP(request);
        } catch (Exception e) {
            return null;
        }
    }

}
