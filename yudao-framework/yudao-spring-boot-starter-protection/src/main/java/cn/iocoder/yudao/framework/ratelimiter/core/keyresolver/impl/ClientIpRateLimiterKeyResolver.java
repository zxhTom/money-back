package cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl;

import cn.hutool.crypto.SecureUtil;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import org.aspectj.lang.JoinPoint;

/**
 * IP 级别的限流 Key 解析器，按接口和可信真实 IP 共用计数器。
 * 请求参数不能参与计数键，否则改变密码等参数就能绕过同一 IP 的限流。
 *
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 *
 * @author 芋道源码
 */
public class ClientIpRateLimiterKeyResolver implements RateLimiterKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, RateLimiter rateLimiter) {
        String methodName = joinPoint.getSignature().toString();
        String clientIp = ServletUtils.getClientRealIp(ServletUtils.getRequest());
        return SecureUtil.md5(methodName + ":" + clientIp);
    }

}
