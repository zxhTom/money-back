package cn.iocoder.yudao.module.custom.controller.admin.faceauth;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.module.custom.service.faceauth.AdminFaceResetService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/custom/admin/face-auth")
public class AdminFaceResetController {
    @Resource private AdminFaceResetService service;
    @PostMapping("/reset")
    @PreAuthorize("@ss.hasAnyRoles('super_admin','tenant_admin')")
    @ApiAccessLog(operateModule="管理员人脸认证",operateName="要求重新人脸认证")
    public CommonResult<Boolean> reset(@RequestBody ResetRequest request) {
        service.reset(request.getUsername());
        return success(true);
    }
    @Getter @Setter
    public static class ResetRequest { private String username; }
}
