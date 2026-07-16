package cn.iocoder.yudao.module.system.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.service.user.WeakPasswordScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 弱密码检测（安全隔离）：
 *   1. 独立权限 system:user:weak-scan（仅超管菜单可见）；
 *   2. 代码层【硬校验当前操作人必须是超级管理员】——即便权限被误配，普通管理员也调不动；
 *   3. 结果只在内存、只返回弱密码【类别】不返回明文/哈希；框架 API 访问日志已记录操作人+IP。
 */
@Tag(name = "管理后台 - 弱密码检测")
@RestController
@RequestMapping("/system/user/weak-password")
@Slf4j
public class WeakPasswordScanController {

    @Resource
    private WeakPasswordScanService weakPasswordScanService;

    private void assertSuperAdmin() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (!weakPasswordScanService.isSuperAdmin(userId)) {
            log.warn("[WeakPwdScan] 非超管尝试调用弱密码检测，已拒绝：userId={}", userId);
            throw exception0(403, "仅超级管理员可执行弱密码检测");
        }
    }

    @PostMapping("/scan")
    @Operation(summary = "触发弱密码扫描（异步，仅超管）")
    @PreAuthorize("@ss.hasPermission('system:user:weak-scan')")
    public CommonResult<Boolean> scan() {
        assertSuperAdmin();
        log.warn("[WeakPwdScan] 超管 userId={} 触发弱密码扫描", SecurityFrameworkUtils.getLoginUserId());
        weakPasswordScanService.startScan();
        return success(true);
    }

    @GetMapping("/result")
    @Operation(summary = "获取扫描结果/进度（仅超管）")
    @PreAuthorize("@ss.hasPermission('system:user:weak-scan')")
    public CommonResult<Map<String, Object>> result() {
        assertSuperAdmin();
        return success(weakPasswordScanService.getResult());
    }

    @PostMapping("/force-reset")
    @Operation(summary = "对选中用户强制改随机强密码+踢下线（仅超管）")
    @PreAuthorize("@ss.hasPermission('system:user:weak-scan')")
    public CommonResult<Integer> forceReset(@RequestBody java.util.List<Long> userIds) {
        assertSuperAdmin();
        log.warn("[WeakPwdScan] 超管 userId={} 对 {} 个用户强制改密+踢下线",
                SecurityFrameworkUtils.getLoginUserId(), userIds == null ? 0 : userIds.size());
        return success(weakPasswordScanService.forceReset(userIds));
    }
}
