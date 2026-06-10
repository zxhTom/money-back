package cn.iocoder.yudao.module.custom.config;

import cn.iocoder.yudao.module.custom.framework.security.filter.SecurityDetectFilter;
import cn.iocoder.yudao.module.system.service.monitor.IpBlacklistService;
import cn.iocoder.yudao.module.system.service.monitor.IpRiskCheckService;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import javax.annotation.Resource;

@Configuration(proxyBeanMethods = false)
public class SecurityMonitorConfiguration {

    @Resource
    private IpBlacklistService ipBlacklistService;
    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IpRiskCheckService ipRiskCheckService;

    @Bean
    public FilterRegistrationBean<SecurityDetectFilter> securityDetectFilter() {
        FilterRegistrationBean<SecurityDetectFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new SecurityDetectFilter(ipBlacklistService, securityAlertService,
                stringRedisTemplate, ipRiskCheckService));
        bean.addUrlPatterns("/*");
        // 在 Spring Security 过滤链之前执行（order=-200 是 Spring Security，这里用 -300）
        bean.setOrder(-300);
        bean.setName("securityDetectFilter");
        return bean;
    }
}
