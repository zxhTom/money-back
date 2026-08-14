package cn.iocoder.yudao.module.custom.framework.speedcontrol;

import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.module.custom.service.speedcontrol.SpeedControlConfigService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
public class SpeedControlConfiguration {

    /**
     * -97：必须排在 Spring Security（-100）之后，过滤器进入时才拿得到登录用户做豁免判断。
     * 是否真正生效由数据库里的 enabled 开关决定，这里不加 @ConditionalOnProperty，
     * 否则改开关就得重启。
     */
    @Bean
    public FilterRegistrationBean<SpeedControlFilter> speedControlFilter(
            WebProperties webProperties,
            SpeedControlConfigService speedControlConfigService,
            StringRedisTemplate stringRedisTemplate) {
        SpeedControlFilter filter = new SpeedControlFilter(webProperties, speedControlConfigService, stringRedisTemplate);
        FilterRegistrationBean<SpeedControlFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(-97);
        return bean;
    }

}
