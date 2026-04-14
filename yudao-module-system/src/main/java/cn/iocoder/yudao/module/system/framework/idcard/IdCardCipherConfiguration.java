package cn.iocoder.yudao.module.system.framework.idcard;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(IdCardCipherProperties.class)
public class IdCardCipherConfiguration {
}
