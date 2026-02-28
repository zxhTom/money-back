package cn.iocoder.yudao.module.custom.service.email;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.module.custom.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码 Service 实现类
 *
 * @author zxhtom
 */
@Service
@Slf4j
public class EmailCodeServiceImpl implements EmailCodeService {

    /**
     * Redis Key 前缀：邮箱验证码
     * KEY 格式：email:code:{email}
     * VALUE 格式：验证码（6位数字）
     * 过期时间：5分钟
     */
    private static final String REDIS_KEY_PREFIX = "email:code:";

    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 5;

    @Resource
    private EmailConfig emailConfig;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean sendCode(String email) {
        // 1. 校验邮箱格式
        if (StrUtil.isBlank(email) || !Validator.isEmail(email)) {
            throw new IllegalArgumentException("邮箱地址格式不正确");
        }

        // 2. 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);
        log.info("[sendCode][发送验证码到邮箱: {}, 验证码: {}]", email, code);

        // 3. 构建邮件内容
        String subject = "验证码通知";
        String content = buildEmailContent(code);

        try {
            // 4. 构建邮件账号
            MailAccount mailAccount = buildMailAccount();

            // 5. 发送邮件（最后一个参数 true 表示内容为HTML格式）
            String messageId = MailUtil.send(mailAccount, email, subject, content, true);
            log.info("[sendCode][邮件发送成功，邮箱: {}, messageId: {}]", email, messageId);

            // 6. 将验证码存储到Redis，设置5分钟过期
            String redisKey = REDIS_KEY_PREFIX + email;
            stringRedisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.info("[sendCode][验证码已存储到Redis，邮箱: {}, 有效期: {}分钟]", email, CODE_EXPIRE_MINUTES);

            return true;
        } catch (Exception e) {
            log.error("[sendCode][发送验证码失败，邮箱: {}]", email, e);
            throw new RuntimeException("发送验证码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean verifyCode(String email, String code) {
        // 1. 校验参数
        if (StrUtil.isBlank(email) || StrUtil.isBlank(code)) {
            return false;
        }

        // 2. 从Redis获取验证码
        String redisKey = REDIS_KEY_PREFIX + email;
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);

        // 3. 验证码不存在或已过期
        if (StrUtil.isBlank(storedCode)) {
            log.warn("[verifyCode][验证码不存在或已过期，邮箱: {}]", email);
            return false;
        }

        // 4. 验证码匹配
        boolean matches = storedCode.equals(code);
        if (matches) {
            // 验证成功后，删除验证码（防止重复使用）
            stringRedisTemplate.delete(redisKey);
            log.info("[verifyCode][验证码验证成功，邮箱: {}]", email);
        } else {
            log.warn("[verifyCode][验证码不匹配，邮箱: {}, 输入: {}, 存储: {}]", email, code, storedCode);
        }

        return matches;
    }

    /**
     * 构建邮件内容
     *
     * @param code 验证码
     * @return 邮件内容（HTML格式）
     */
    private String buildEmailContent(String code) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }\n" +
                "        .content { padding: 20px; background-color: #f9f9f9; }\n" +
                "        .code { font-size: 32px; font-weight: bold; color: #4CAF50; text-align: center; padding: 20px; background-color: white; margin: 20px 0; letter-spacing: 5px; }\n" +
                "        .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }\n" +
                "        .warning { color: #ff6600; font-weight: bold; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>验证码通知</h2>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>尊敬的用户，您好！</p>\n" +
                "            <p>您正在使用邮箱验证功能，您的验证码是：</p>\n" +
                "            <div class=\"code\">" + code + "</div>\n" +
                "            <p class=\"warning\">⚠️ 重要提示：验证码有效期为 <strong>5分钟</strong>，请及时使用，过期后将无法使用。</p>\n" +
                "            <p>如果这不是您的操作，请忽略此邮件。</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>此邮件由系统自动发送，请勿回复。</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 构建邮件账号
     *
     * @return MailAccount
     */
    private MailAccount buildMailAccount() {
        MailAccount account = new MailAccount();
        account.setHost(emailConfig.getHost());
        account.setPort(emailConfig.getPort());
        account.setAuth(true);
        account.setUser(emailConfig.getUsername());
        account.setPass(emailConfig.getPassword());
        account.setSslEnable(emailConfig.getSslEnable());
        account.setStarttlsEnable(emailConfig.getStarttlsEnable());

        // 设置发件人（带昵称）
        String from = StrUtil.isNotEmpty(emailConfig.getNickname())
                ? emailConfig.getNickname() + " <" + emailConfig.getFrom() + ">"
                : emailConfig.getFrom();
        account.setFrom(from);

        return account;
    }

}
