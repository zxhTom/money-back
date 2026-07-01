package cn.iocoder.yudao.module.custom.job.pwd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.custom.dal.dataobject.pwd.AutoResetPwdUserDO;
import cn.iocoder.yudao.module.custom.dal.mysql.pwd.AutoResetPwdUserMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.List;

/**
 * 定时自动重置密码 Job
 *
 * 每晚 22:00 对配置列表中的用户执行：
 *  1. 生成随机复杂密码（12位，含大小写+数字+特殊符号）
 *  2. BCrypt 加密后更新到数据库
 *  3. 强制用户下线
 *  4. 通过 SMTP 直发邮件（不经过 mail_log 存储，明文不落库）
 *
 * Job 参数（handlerParam）格式：notifyEmail=xxx@example.com
 */
@Component
@Slf4j
public class AutoResetPwdJob implements JobHandler {

    private static final String UPPER  = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER  = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPEC   = "!@#$%^&*";
    private static final String ALL    = UPPER + LOWER + DIGITS + SPEC;

    @Resource
    private AutoResetPwdUserMapper autoResetPwdUserMapper;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private MailAccountService mailAccountService;
    @Override
    @TenantIgnore
    public String execute(String param) {
        // 解析通知邮箱
        String notifyEmail = parseNotifyEmail(param);
        if (StrUtil.isBlank(notifyEmail)) {
            log.warn("[AutoResetPwdJob] 未配置 notifyEmail，跳过执行。参数格式: notifyEmail=xxx@example.com");
            return "未配置 notifyEmail，跳过";
        }

        List<AutoResetPwdUserDO> targets = autoResetPwdUserMapper.selectAll();
        if (CollUtil.isEmpty(targets)) {
            return "自动重置名单为空，跳过";
        }

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("以下用户密码已于 ")
                 .append(java.time.LocalDateTime.now().toString().substring(0, 19))
                 .append(" 被自动重置：\n\n");

        int successCount = 0;
        SecureRandom random = new SecureRandom();

        for (AutoResetPwdUserDO target : targets) {
            AdminUserDO user = adminUserService.getUser(target.getUserId());
            if (user == null) {
                log.warn("[AutoResetPwdJob] userId={} 用户不存在，跳过", target.getUserId());
                continue;
            }

            char[] plainChars = generatePassword(random);
            try {
                adminUserService.updateUserPassword(user.getId(), new String(plainChars));

                // 强制下线
                oauth2TokenService.removeAllTokensByUserId(user.getId(), UserTypeEnum.ADMIN.getValue());

                emailBody.append("用户名：").append(user.getUsername())
                         .append("  昵称：").append(StrUtil.nullToDefault(user.getNickname(), "-"))
                         .append("  新密码：").append(new String(plainChars))
                         .append("\n");
                successCount++;
            } catch (Exception e) {
                log.error("[AutoResetPwdJob] 重置密码失败 userId={}", target.getUserId(), e);
            } finally {
                // 明文密码用完即清零，不在堆上长期驻留
                java.util.Arrays.fill(plainChars, '\0');
            }
        }

        emailBody.append("\n共重置 ").append(successCount).append(" 个用户的密码。\n")
                 .append("请妥善保管本邮件，获知密码后立即删除此邮件。");

        // 直发邮件，绕过 mail_log，明文不落库
        sendDirectMail(notifyEmail, "【安全通知】用户密码已自动重置", emailBody.toString());

        // 清空 StringBuilder 内容（GC 友好）
        emailBody.delete(0, emailBody.length());

        String result = String.format("自动重置密码完成，成功 %d / %d 个用户", successCount, targets.size());
        log.info("[AutoResetPwdJob] {}", result);
        return result;
    }

    private char[] generatePassword(SecureRandom random) {
        char[] pwd = new char[12];
        pwd[0] = UPPER.charAt(random.nextInt(UPPER.length()));
        pwd[1] = UPPER.charAt(random.nextInt(UPPER.length()));
        pwd[2] = LOWER.charAt(random.nextInt(LOWER.length()));
        pwd[3] = LOWER.charAt(random.nextInt(LOWER.length()));
        pwd[4] = DIGITS.charAt(random.nextInt(DIGITS.length()));
        pwd[5] = DIGITS.charAt(random.nextInt(DIGITS.length()));
        pwd[6] = SPEC.charAt(random.nextInt(SPEC.length()));
        pwd[7] = SPEC.charAt(random.nextInt(SPEC.length()));
        for (int i = 8; i < 12; i++) {
            pwd[i] = ALL.charAt(random.nextInt(ALL.length()));
        }
        // Fisher-Yates 洗牌
        for (int i = 11; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = pwd[i]; pwd[i] = pwd[j]; pwd[j] = tmp;
        }
        return pwd;
    }

    /**
     * 直接通过第一个可用邮件账号发送，不写 mail_log，明文不落库。
     */
    private void sendDirectMail(String to, String subject, String content) {
        List<MailAccountDO> accounts = mailAccountService.getMailAccountList();
        if (CollUtil.isEmpty(accounts)) {
            log.error("[AutoResetPwdJob] 无可用邮件账号，无法发送通知邮件");
            return;
        }
        MailAccountDO accountDO = accounts.get(0);
        MailAccount mailAccount = new MailAccount()
                .setFrom(accountDO.getMail())
                .setAuth(true)
                .setUser(accountDO.getUsername())
                .setPass(accountDO.getPassword())
                .setHost(accountDO.getHost())
                .setPort(accountDO.getPort())
                .setSslEnable(Boolean.TRUE.equals(accountDO.getSslEnable()))
                .setStarttlsEnable(Boolean.TRUE.equals(accountDO.getStarttlsEnable()));
        try {
            MailUtil.send(mailAccount, to, subject, content, false);
            log.info("[AutoResetPwdJob] 通知邮件已发送至 {}", to);
        } catch (Exception e) {
            log.error("[AutoResetPwdJob] 通知邮件发送失败", e);
        }
    }

    private String parseNotifyEmail(String param) {
        if (StrUtil.isBlank(param)) return null;
        for (String kv : param.split("[,;&\n]")) {
            String[] parts = kv.trim().split("=", 2);
            if (parts.length == 2 && "notifyEmail".equalsIgnoreCase(parts[0].trim())) {
                return parts[1].trim();
            }
        }
        // 兼容直接填邮箱地址的情况
        if (param.trim().contains("@")) return param.trim();
        return null;
    }

}
