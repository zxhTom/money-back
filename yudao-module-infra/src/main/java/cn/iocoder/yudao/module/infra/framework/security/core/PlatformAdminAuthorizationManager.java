package cn.iocoder.yudao.module.infra.framework.security.core;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

/**
 * 平台管理员授权管理器，用于 Spring Boot Admin 监控等端点
 */
public class PlatformAdminAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        try {
            PlatformFileConfigGuard guard = SpringUtil.getBean(PlatformFileConfigGuard.class);
            if (guard != null && guard.isPlatformSuperAdmin()) {
                return new AuthorizationDecision(true);
            }
        } catch (Exception ignored) {
        }
        return new AuthorizationDecision(false);
    }
}
