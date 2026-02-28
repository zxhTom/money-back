package cn.iocoder.yudao.module.custom.service.email;

/**
 * 邮箱验证码 Service 接口
 *
 * @author zxhtom
 */
public interface EmailCodeService {

    /**
     * 发送验证码到指定邮箱
     *
     * @param email 收件邮箱地址
     * @return 是否发送成功
     */
    Boolean sendCode(String email);

    /**
     * 验证验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证通过
     */
    Boolean verifyCode(String email, String code);

}
