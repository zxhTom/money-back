package cn.iocoder.yudao.module.custom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件配置类（163邮箱）
 *
 * @author zxhtom
 */
@Configuration
@ConfigurationProperties(prefix = "custom.email")
@Data
public class EmailConfig {

    /**
     * 163邮箱SMTP服务器地址
     */
    private String host = "smtp.163.com";

    /**
     * 163邮箱SMTP端口
     */
    private Integer port = 465;

    /**
     * 163邮箱账号（发送方邮箱地址）
     */
    private String from;

    /**
     * 163邮箱账号用户名（通常是邮箱地址）
     */
    private String username;

    /**
     * 163邮箱授权码（不是登录密码，需要在163邮箱设置中获取）
     */
    private String password;

    /**
     * 是否启用SSL
     */
    private Boolean sslEnable = true;

    /**
     * 是否启用STARTTLS
     */
    private Boolean starttlsEnable = false;

    /**
     * 发送方昵称
     */
    private String nickname = "系统通知";

}
