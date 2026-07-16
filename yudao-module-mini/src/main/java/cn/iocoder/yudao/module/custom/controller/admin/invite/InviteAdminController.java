package cn.iocoder.yudao.module.custom.controller.admin.invite;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.invite.vo.InviteCodeRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.invite.vo.InviteRegisterLogRespVO;
import cn.iocoder.yudao.module.custom.service.invite.InviteCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 邀请码记录")
@RestController
@RequestMapping("/system/user/invite")
@Validated
public class InviteAdminController {

    @Resource
    private InviteCodeService inviteCodeService;

    @GetMapping("/codes")
    @Operation(summary = "查看某用户生成的邀请码记录")
    @Parameter(name = "userId", description = "邀请人用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:invite')")
    public CommonResult<List<InviteCodeRespVO>> getCodes(@RequestParam("userId") Long userId) {
        List<InviteCodeRespVO> list = inviteCodeService.listCodesByInviter(userId).stream()
                .map(InviteController::toRespVO).collect(Collectors.toList());
        return success(list);
    }

    @GetMapping("/registrations")
    @Operation(summary = "查看某邀请码下的注册名单")
    @Parameter(name = "codeId", description = "邀请码ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:invite')")
    public CommonResult<List<InviteRegisterLogRespVO>> getRegistrations(@RequestParam("codeId") Long codeId) {
        List<InviteRegisterLogRespVO> list = BeanUtils.toBean(
                inviteCodeService.listRegistrations(codeId), InviteRegisterLogRespVO.class);
        return success(list);
    }

}
