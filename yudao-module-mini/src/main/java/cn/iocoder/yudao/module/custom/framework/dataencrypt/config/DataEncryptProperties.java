package cn.iocoder.yudao.module.custom.framework.dataencrypt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应数据加密配置（yudao.data-encrypt）
 */
@ConfigurationProperties(prefix = "yudao.data-encrypt")
@Validated
@Data
public class DataEncryptProperties {

    /**
     * 是否开启响应 data 加密
     */
    private Boolean enable = false;

    /**
     * 客户端派生盐，需与 money-ui / 小程序代码内的常量一致
     */
    private String clientSalt;

    /**
     * 密钥轮换后，旧版本主密钥的保留秒数
     */
    private Integer graceSeconds = 300;

    /**
     * 不加密的 URL（Ant 风格），主要为第三方交互/登录前接口
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * 调试密钥：请求头 X-Data-Encrypt-Debug 与其一致时该请求跳过加密返回明文；留空禁用
     */
    private String debugSecret;

}
