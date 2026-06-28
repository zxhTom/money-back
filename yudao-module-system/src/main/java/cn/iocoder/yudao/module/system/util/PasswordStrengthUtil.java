package cn.iocoder.yudao.module.system.util;

/**
 * 密码强度计算工具
 * 返回值：1=弱，2=中，3=强
 */
public class PasswordStrengthUtil {

    public static int calc(String password) {
        if (password == null || password.isEmpty()) return 1;

        int score = 0;
        if (password.length() >= 8)  score++;
        if (password.length() >= 12) score++;
        if (password.chars().anyMatch(Character::isLowerCase)) score++;
        if (password.chars().anyMatch(Character::isUpperCase)) score++;
        if (password.chars().anyMatch(Character::isDigit))     score++;
        if (password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) score++;

        if (score <= 2) return 1;
        if (score <= 4) return 2;
        return 3;
    }
}
