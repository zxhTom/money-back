package cn.iocoder.yudao.module.custom.framework.security.config;

import cn.iocoder.yudao.module.custom.framework.security.interceptor.RealNameAuthInterceptor;
import cn.iocoder.yudao.module.custom.framework.security.interceptor.SimulatedRequestInterceptor;
import cn.iocoder.yudao.module.custom.framework.security.interceptor.UrlMonitorInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 注册 custom 模块的 MVC 拦截器（运行在 Spring Security 之后，能拿到登录用户）。
 * - SimulatedRequestInterceptor：伪造来源IP / 非法UA 检测；
 * - UrlMonitorInterceptor：按配置的 URL + 频率监控，超阈值立即封IP；
 * - RealNameAuthInterceptor：未实名认证的终端用户禁止访问业务接口（戏耍模式优先放行）。
 */
@Configuration(proxyBeanMethods = false)
public class CustomWebMvcConfig implements WebMvcConfigurer {

    @Resource
    private SimulatedRequestInterceptor simulatedRequestInterceptor;
    @Resource
    private UrlMonitorInterceptor urlMonitorInterceptor;
    @Resource
    private RealNameAuthInterceptor realNameAuthInterceptor;

    /**
     * 服务端到服务端的回调/webhook：调用方是微信/支付网关服务器，UA 常为空或非浏览器型，
     * 绝不能用"浏览器 UA 启发式"去判它们（否则会把微信/支付服务器 IP 误封，打断支付/消息回调）。
     * 用 /**（前缀无关）匹配，兼容 admin-api 前缀。
     */
    private static final String[] WEBHOOK_EXCLUDES = {
            "/**/pay/notify/**",                    // 微信/支付宝 支付结果通知
            "/**/offcial/*/callback",               // 微信公众号 消息/事件推送
            "/**/api/mini/callback",                // 人脸核身结果回跳落地页
            "/**/infra/file/sec-check-callback"     // 微信小程序 图片内容安全 mediaCheckAsync 回调
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 模拟/伪造请求检测：排除服务端回调，避免误伤微信/支付网关
        registry.addInterceptor(simulatedRequestInterceptor)
                .addPathPatterns("/**").excludePathPatterns(WEBHOOK_EXCLUDES);
        registry.addInterceptor(urlMonitorInterceptor)
                .addPathPatterns("/**").excludePathPatterns(WEBHOOK_EXCLUDES);
        registry.addInterceptor(realNameAuthInterceptor).addPathPatterns("/**");
    }

}
