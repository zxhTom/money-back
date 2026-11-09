package cn.iocoder.yudao.framework.security.core.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Token 过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link LoginUser} 信息，并加入到 Spring Security 上下文
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final OAuth2TokenCommonApi oauth2TokenApi;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotEmpty(token)) {
            Integer userType = WebFrameworkUtils.getLoginUserType(request);
            try {
                // 1.1 基于 token 构建登录用户
                LoginUser loginUser = buildLoginUserByToken(token, userType, request);
                // 1.2 模拟 Login 功能，方便日常开发调试
                if (loginUser == null) {
                    loginUser = mockLoginUser(request, token, userType);
                }

                // 2. 设置当前用户
                if (loginUser != null) {
                    SecurityFrameworkUtils.setLoginUser(loginUser, request);
                }
            } catch (Throwable ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }

        // 继续过滤链
        chain.doFilter(request, response);
    }

    private LoginUser buildLoginUserByToken(String token, Integer userType, HttpServletRequest request) {
        try {
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);
            if (accessToken == null) {
                return null;
            }
            // 用户类型不匹配，无权限
            // 注意：只有 /admin-api/* 和 /app-api/* 有 userType，才需要比对用户类型
            // 类似 WebSocket 的 /ws/* 连接地址，是不需要比对用户类型的
            if (userType != null
                    && ObjectUtil.notEqual(accessToken.getUserType(), userType)) {
                throw new AccessDeniedException("错误的用户类型");
            }
            if (accessToken.getScopes() != null
                    && accessToken.getScopes().contains(OAuth2TokenCommonApi.ACCOUNT_IMPERSONATION_SCOPE)
                    && restrictedImpersonationPath(org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request))) {
                throw new AccessDeniedException("代入会话不能修改登录凭据、微信绑定或身份认证");
            }
            if (accessToken.getScopes() != null
                    && accessToken.getScopes().contains(OAuth2TokenCommonApi.ACCOUNT_IMPERSONATION_SCOPE)) {
                String actorId = accessToken.getUserInfo() == null ? "unknown"
                        : accessToken.getUserInfo().getOrDefault("_impersonatorId", "unknown");
                log.info("[accountSwitch][request] actor={} target={} method={} path={}",
                        actorId, accessToken.getUserId(), request.getMethod(), request.getRequestURI());
            }
            // 构建登录用户
            return new LoginUser().setId(accessToken.getUserId()).setUserType(accessToken.getUserType())
                    .setInfo(accessToken.getUserInfo()) // 额外的用户信息
                    .setTenantId(accessToken.getTenantId()).setScopes(accessToken.getScopes())
                    .setExpiresTime(accessToken.getExpiresTime());
        } catch (ServiceException serviceException) {
            // 切换会话失效专用错误需要透传，不能静默当做未登录游客处理
            if (ObjectUtil.equal(serviceException.getCode(), cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.ACCOUNT_SWITCH_SESSION_INVALID.getCode())) {
                throw serviceException;
            }
            // 校验 Token 不通过时，考虑到一些接口是无需登录的，所以直接返回 null 即可
            return null;
        }
    }

    private boolean restrictedImpersonationPath(String path) {
        return path.startsWith("/admin-api/system/auth/")
                && !path.equals("/admin-api/system/auth/get-permission-info")
                && !path.startsWith("/admin-api/system/auth/account-switch/")
                || path.startsWith("/admin-api/system/user/profile/update-password")
                || path.startsWith("/admin-api/system/user/profile/update-pay-password")
                || path.startsWith("/admin-api/custom/contract/dashboard/bindmini2user")
                || path.startsWith("/admin-api/api/faceAuth/");
    }

    /**
     * 模拟登录用户，方便日常开发调试
     *
     * 注意，在线上环境下，一定要关闭该功能！！！
     *
     * @param request 请求
     * @param token 模拟的 token，格式为 {@link SecurityProperties#getMockSecret()} + 用户编号
     * @param userType 用户类型
     * @return 模拟的 LoginUser
     */
    private LoginUser mockLoginUser(HttpServletRequest request, String token, Integer userType) {
        if (!securityProperties.getMockEnable()) {
            return null;
        }
        // 必须以 mockSecret 开头
        if (!token.startsWith(securityProperties.getMockSecret())) {
            return null;
        }
        // 构建模拟用户
        Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        return new LoginUser().setId(userId).setUserType(userType)
                .setTenantId(WebFrameworkUtils.getTenantId(request));
    }

}
