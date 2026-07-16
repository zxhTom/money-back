package cn.iocoder.yudao.module.custom.framework.dataencrypt.config;

import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.core.DataEncryptFilter;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.core.DataKeyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DataEncryptProperties.class)
public class DataEncryptConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "yudao.data-encrypt", name = "enable", havingValue = "true")
    public FilterRegistrationBean<DataEncryptFilter> dataEncryptFilter(WebProperties webProperties,
                                                                       DataEncryptProperties properties,
                                                                       DataKeyService dataKeyService,
                                                                       RequestMappingHandlerMapping requestMappingHandlerMapping) {
        DataEncryptFilter filter = new DataEncryptFilter(webProperties, properties, dataKeyService, requestMappingHandlerMapping);
        FilterRegistrationBean<DataEncryptFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(WebFilterOrderEnum.API_ENCRYPT_FILTER + 1);
        return bean;
    }

}
