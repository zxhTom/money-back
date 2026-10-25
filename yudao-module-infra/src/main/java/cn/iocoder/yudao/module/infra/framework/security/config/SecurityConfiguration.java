package cn.iocoder.yudao.module.infra.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Infra 模块的 Security 配置
 */
@Configuration(proxyBeanMethods = false, value = "infraSecurityConfiguration")
public class SecurityConfiguration {

    @Value("${spring.boot.admin.context-path:''}")
    private String adminSeverContextPath;

    @Bean("infraAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                // Swagger 接口文档
                registry.requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll();
                // Spring Boot Actuator 的安全配置
                registry.requestMatchers("/actuator").permitAll()
                        .requestMatchers("/actuator/**").permitAll();
                // Druid 监控
                registry.requestMatchers("/druid/**").permitAll();
                // Spring Boot Admin Server 的安全配置：仅合法固定的固定绝对 path 且仅平台管理员可访问
                if (isValidAdminPath(adminSeverContextPath)) {
                    String cleanPath = adminSeverContextPath.trim();
                    if (cleanPath.endsWith("/")) {
                        cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
                    }
                    registry.requestMatchers(cleanPath).access(new cn.iocoder.yudao.module.infra.framework.security.core.PlatformAdminAuthorizationManager())
                            .requestMatchers(cleanPath + "/**").access(new cn.iocoder.yudao.module.infra.framework.security.core.PlatformAdminAuthorizationManager());
                }
                // 文件读取
                registry.requestMatchers(buildAdminApi("/infra/file/*/get/**")).permitAll();
            }

            private boolean isValidAdminPath(String path) {
                if (!org.springframework.util.StringUtils.hasText(path)) return false;
                String p = path.trim();
                if (!p.startsWith("/") || "/".equals(p)) return false;
                if (p.contains("*") || p.contains("?") || p.contains("{") || p.contains("}")
                        || p.contains("..") || p.contains("//") || p.contains("\\")) {
                    return false;
                }
                return true;
            }

        };
    }

}
