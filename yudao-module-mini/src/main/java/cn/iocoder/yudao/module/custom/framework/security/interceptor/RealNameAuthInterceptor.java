package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
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
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.USER_NOT_REAL_NAME_VERIFIED;

/**
 * 全局实名认证网关（运行在 Spring Security 之后，能拿到登录用户）。
 *
 * 规则（优先级从高到低）：
 *  1. 未登录接口（拿不到登录用户）→ 放行；
 *  2. 豁免端点（人脸认证流程、登录/登出、个人资料）→ 放行，否则未认证用户无法去完成认证会死锁；
 *  3. 戏耍模式 teaseEnabled → 放行（戏耍优先，密码对了就能登录调接口，只是数据是 mock 的）；
 *  4. 已实名 verified=1 → 放行；
 *  5. 未实名：仅"终端用户"（持 contract 角色且非超管）拦截，其余（管理端/超管/其它角色）放行，避免锁死后台。
 */
@Component
@Slf4j
public class RealNameAuthInterceptor implements HandlerInterceptor {

    private static final String ADMIN_API_PREFIX = "/admin-api";
    private static final String END_USER_ROLE = "contract";

    /** 未实名也允许访问的端点（归一化后 startsWith 匹配）：完成认证/查看资料/登录登出所需 */
    private static final List<String> EXEMPT_PREFIXES = java.util.Arrays.asList(
            "/api/faceAuth",          // 人脸核身发起/上报/查询
            "/api/mini/callback",     // 人脸回调
            "/system/auth",           // 登录/登出/刷新令牌
            "/system/user/profile"    // 个人资料（查看是否已认证、完善身份证）
    );

    // 默认关闭：保证未升级的旧版小程序不受影响；发新版小程序并验证后，配置 true 开启
    @Value("${yudao.face-auth.required-check-enabled:false}")
    private boolean enabled;

    /** 额外豁免端点（逗号分隔，归一化后 startsWith 匹配），用于运营期临时补充，无需改代码 */
    @Value("${yudao.face-auth.exempt-urls:}")
    private String extraExemptUrls;

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleService roleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enabled) {
            return true;
        }
        if (isAllowed(tryGetUserId(), normalize(request.getRequestURI()))) {
            return true;
        }
        writeForbidden(response);
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
        // 戏耍优先：开启戏耍的用户即便没认证也放行（返回的是 mock 数据）
        if (Boolean.TRUE.equals(user.getTeaseEnabled())) {
            return true;
        }
        // 已实名放行（最常见路径，只有一次 getUser 查询）
        if (Integer.valueOf(1).equals(user.getVerified())) {
            return true;
        }
        // 未实名：只拦"终端用户"，管理端/超管等放行，避免误锁后台
        return !isEndUserSubjectToCheck(userId);
    }

    /** 仅"持 contract 角色且不是超管"的终端用户需要强制实名 */
    private boolean isEndUserSubjectToCheck(Long userId) {
        List<Long> roleIds = userRoleMapper.selectListByUserId(userId).stream()
                .map(UserRoleDO::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return false; // 没有任何角色，不强制（安全侧放行）
        }
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return false; // 超管放行
        }
        boolean isEndUser = roleService.getRoleList(roleIds).stream()
                .map(RoleDO::getCode).anyMatch(END_USER_ROLE::equals);
        return isEndUser;
    }

    private boolean isExempt(String path) {
        for (String p : EXEMPT_PREFIXES) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        if (StrUtil.isNotBlank(extraExemptUrls)) {
            for (String p : extraExemptUrls.split(",")) {
                String prefix = normalize(p.trim());
                if (!prefix.isEmpty() && path.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    static String normalize(String uri) {
        if (StrUtil.isBlank(uri)) {
            return "";
        }
        String u = uri.trim();
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }
        if (u.startsWith(ADMIN_API_PREFIX)) {
            u = u.substring(ADMIN_API_PREFIX.length());
        }
        return u;
    }

    private void writeForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.OK.value()); // 按 yudao 约定：业务错误用 HTTP 200 + code 承载
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter out = response.getWriter()) {
            out.write("{\"code\":" + USER_NOT_REAL_NAME_VERIFIED.getCode()
                    + ",\"msg\":\"" + USER_NOT_REAL_NAME_VERIFIED.getMsg() + "\",\"data\":null}");
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
