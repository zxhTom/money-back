package cn.iocoder.yudao.module.system.controller.admin.monitor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Retained only to reject the legacy temporary route; permission changes use audited management APIs. */
@Deprecated
@RestController
@RequestMapping("/admin-api/custom/security/fix")
public class FixPermissionController {

    @GetMapping("/run")
    @PreAuthorize("denyAll()")
    public String run() {
        throw new AccessDeniedException("临时权限修复接口已停用，请使用权限管理功能");
    }
}
