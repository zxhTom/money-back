package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.service.contract.ContractTeaseGuard;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.USER_PAY_PASSWORD_NOT_CHANGED;

/**
 * 强制修改支付密码网关（运行在 Spring Security 之后，能拿到登录用户）。
 *
 * 注册时支付密码默认等于登录密码，未单独改过的用户不允许使用业务功能。
 * 判定完全照搬 {@link RealNameAuthInterceptor} 的模式：默认关闭、豁免路径、HTTP 200 + 自定义 code。
 *
 * 规则（优先级从高到低）：
 *  1. 开关关闭 → 放行；
 *  2. 未登录接口 → 放行；
 *  3. 豁免端点（改支付密码本身、登录登出、个人资料）→ 放行，否则用户没法去把密码改掉，会死锁；
 *  4. 戏耍模式 → 放行；
 *  5. payPasswordChanged=true → 放行（存量用户由迁移一次性回填为 true，不受影响）；
 *  6. 未改过：仅"终端用户"（持 contract 角色且非超管）拦截，其余放行，避免锁死后台。
 */
@Component
@Slf4j
public class PayPasswordInterceptor implements HandlerInterceptor {

    private static final String ADMIN_API_PREFIX = "/admin-api";
    private static final String END_USER_ROLE = "contract";

    /**
     * 未改支付密码也允许访问的端点（归一化后 startsWith 匹配）。
     * 第一条最关键：不放行改支付密码接口本身，用户就永远改不掉，等于把自己锁死。
     */
    private static final List<String> EXEMPT_PREFIXES = Arrays.asList(
            "/custom/contract/dashboard/update-pay-password", // 改支付密码本身
            "/system/auth",                                    // 登录/登出/刷新令牌
            "/system/user/profile",                            // 个人资料
            "/custom/client-ip"                                // 我的IP（纯只读工具）
    );

    // 默认关闭：保证未升级的旧版小程序不受影响；发新版小程序并验证后，配置 true 开启
    @Value("${yudao.pay-password.force-change-enabled:false}")
    private boolean enabled;

    /** 额外豁免端点（逗号分隔），运营期临时补充用，无需改代码 */
    @Value("${yudao.pay-password.exempt-urls:}")
    private String extraExemptUrls;

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleService roleService;
    @Resource
    private ContractTeaseGuard contractTeaseGuard;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enabled) {
            return true;
        }
        if (isAllowed(tryGetUserId(), RealNameAuthInterceptor.normalize(request.getRequestURI()))) {
            return true;
        }
        writeBlocked(response);
        return false;
    }

    /** 可测试的判定逻辑：是否放行。 */
    boolean isAllowed(Long userId, String path) {
        if (userId == null) {
            return true; // 未登录接口
        }
        if (isExempt(path)) {
            return true;
        }
        AdminUserDO user = adminUserService.getUser(userId);
        if (user == null) {
            return true;
        }
        if (contractTeaseGuard.isTeasing(userId)) {
            return true;
        }
        if (Boolean.TRUE.equals(user.getPayPasswordChanged())) {
            return true;
        }
        return !isEndUserSubjectToCheck(userId);
    }

    /** 仅"持 contract 角色且不是超管"的终端用户需要强制修改 */
    private boolean isEndUserSubjectToCheck(Long userId) {
        List<Long> roleIds = userRoleMapper.selectListByUserId(userId).stream()
                .map(UserRoleDO::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return false;
        }
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return false;
        }
        return roleService.getRoleList(roleIds).stream()
                .map(RoleDO::getCode).anyMatch(END_USER_ROLE::equals);
    }

    private boolean isExempt(String path) {
        for (String p : EXEMPT_PREFIXES) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        if (StrUtil.isNotBlank(extraExemptUrls)) {
            for (String p : extraExemptUrls.split(",")) {
                String prefix = RealNameAuthInterceptor.normalize(p.trim());
                if (!prefix.isEmpty() && path.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void writeBlocked(HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.OK.value()); // 按 yudao 约定：业务错误用 HTTP 200 + code 承载
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter out = response.getWriter()) {
            out.write("{\"code\":" + USER_PAY_PASSWORD_NOT_CHANGED.getCode()
                    + ",\"msg\":\"" + USER_PAY_PASSWORD_NOT_CHANGED.getMsg() + "\",\"data\":null}");
        }
    }

    private Long tryGetUserId() {
        try {
            return SecurityFrameworkUtils.getLoginUserId();
        } catch (Exception e) {
            return null;
        }
    }

}
