package cn.iocoder.yudao.module.custom.controller.admin.invite;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.UserRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.invite.vo.InviteCodeRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO;
import cn.iocoder.yudao.module.custom.service.invite.InviteCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 邀请码")
@RestController
@RequestMapping("/custom/invite")
@Validated
@Slf4j
public class InviteController {

    @Resource
    private InviteCodeService inviteCodeService;

    @PostMapping("/generate")
    @Operation(summary = "生成邀请码", description = "仅开启邀请功能的用户可用，同时只有一个有效码，有效期8小时")
    @RateLimiter(time = 60, count = 10, keyResolver = UserRateLimiterKeyResolver.class,
            message = "操作过于频繁，请稍后再试")
    public CommonResult<InviteCodeRespVO> generate() {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        InviteCodeDO code = inviteCodeService.generate(loginUserId);
        return success(toRespVO(code));
    }

    static InviteCodeRespVO toRespVO(InviteCodeDO code) {
        InviteCodeRespVO vo = new InviteCodeRespVO();
        vo.setId(code.getId());
        vo.setCode(code.getCode());
        vo.setExpireTime(code.getExpireTime());
        vo.setStatus(code.getStatus());
        vo.setRegisterCount(code.getRegisterCount());
        vo.setCreateTime(code.getCreateTime());
        vo.setValid(Integer.valueOf(InviteCodeDO.STATUS_ACTIVE).equals(code.getStatus())
                && code.getExpireTime() != null && code.getExpireTime().isAfter(LocalDateTime.now()));
        return vo;
    }

}
