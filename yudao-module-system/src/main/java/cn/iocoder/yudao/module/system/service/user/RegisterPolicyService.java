package cn.iocoder.yudao.module.system.service.user;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 注册严格等级策略（配置驱动，后端权威校验；小程序据此动态控制 UI）。
 *   low    = 登录密码仅6位数字，支付密码=登录密码
 *   middle = 登录密码不能纯数字，支付密码=身份证后6位
 *   high   = 登录密码不能纯数字，支付密码用户自设6位数字且不能连续/过简
 * 配置项 system.user.register-strictness（infra_config），默认 low。
 */
@Service
@Slf4j
public class RegisterPolicyService {

    public static final String CONFIG_KEY = "system.user.register-strictness";
    public static final String LOW = "low", MIDDLE = "middle", HIGH = "high";

    @Resource
    private ConfigApi configApi;

    public String getStrictness() {
        String v;
        try {
            v = configApi.getConfigValueByKey(CONFIG_KEY);
        } catch (Exception e) {
            v = null;
        }
        v = v == null ? "" : v.trim().toLowerCase();
        return (MIDDLE.equals(v) || HIGH.equals(v)) ? v : LOW;
    }

    /** 给小程序的策略描述，进注册页时拉取 */
    public Map<String, Object> policyForClient() {
        String level = getStrictness();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("level", level);
        m.put("loginPasswordNumericOnly", LOW.equals(level));          // low: 键盘/校验仅数字
        m.put("loginPasswordForbidPureNumber", !LOW.equals(level));    // middle/high: 禁止纯数字
        m.put("loginPasswordMinLen", 6);
        m.put("loginPasswordMaxLen", LOW.equals(level) ? 6 : 20);
        // same=同登录密码; idcard6=身份证后6位; user6=用户自设6位数字
        m.put("payPasswordMode", HIGH.equals(level) ? "user6" : (MIDDLE.equals(level) ? "idcard6" : "same"));
        m.put("payPasswordInput", HIGH.equals(level));                 // 仅 high 需要用户输入
        return m;
    }

    /** 校验登录密码（按等级）；不合法直接抛异常。 */
    public void validateLoginPassword(String level, String pwd) {
        if (StrUtil.isBlank(pwd) || pwd.length() < 6) {
            throw exception0(400, "登录密码至少6位");
        }
        if (LOW.equals(level)) {
            if (!pwd.matches("\\d{6}")) {
                throw exception0(400, "登录密码需为6位数字");
            }
        } else {
            if (pwd.matches("\\d+")) {
                throw exception0(400, "登录密码不能全为数字，请包含字母");
            }
            if (pwd.length() > 32) {
                throw exception0(400, "登录密码过长");
            }
        }
    }

    /** 按等级解析要存储的支付密码（明文，后续统一 bcrypt 加密）；不合法抛异常。 */
    public String resolvePayPassword(String level, String loginPwd, String payInput, String plainIdNo) {
        if (HIGH.equals(level)) {
            if (StrUtil.isBlank(payInput) || !payInput.matches("\\d{6}")) {
                throw exception0(400, "请设置6位数字支付密码");
            }
            if (isWeakSixDigit(payInput)) {
                throw exception0(400, "支付密码过于简单（连续或重复，如123456、223344），请重新设置");
            }
            return payInput;
        }
        if (MIDDLE.equals(level)) {
            if (StrUtil.isBlank(plainIdNo) || plainIdNo.length() < 6) {
                throw exception0(400, "身份证号无效，无法生成默认支付密码");
            }
            return plainIdNo.substring(plainIdNo.length() - 6);
        }
        return loginPwd; // low：与登录密码相同
    }

    /** 6位数字是否"过于简单"：全同 / 顺子(升降) / 两两成对(223344) / 交替(121212) / 三三重复(123123) */
    public boolean isWeakSixDigit(String s) {
        if (s == null || !s.matches("\\d{6}")) {
            return false;
        }
        if (s.chars().distinct().count() == 1) {
            return true;
        }
        boolean asc = true, desc = true;
        for (int i = 1; i < 6; i++) {
            int d = s.charAt(i) - s.charAt(i - 1);
            if (d != 1) asc = false;
            if (d != -1) desc = false;
        }
        if (asc || desc) {
            return true;
        }
        if (s.charAt(0) == s.charAt(1) && s.charAt(2) == s.charAt(3) && s.charAt(4) == s.charAt(5)) {
            return true;
        }
        if (s.charAt(0) == s.charAt(2) && s.charAt(2) == s.charAt(4)
                && s.charAt(1) == s.charAt(3) && s.charAt(3) == s.charAt(5)) {
            return true;
        }
        return s.substring(0, 3).equals(s.substring(3, 6));
    }
}
