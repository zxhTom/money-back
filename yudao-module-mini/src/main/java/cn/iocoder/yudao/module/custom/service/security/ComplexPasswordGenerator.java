package cn.iocoder.yudao.module.custom.service.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 复杂随机密码生成器（用于风控自动改密）。
 * 生成的密码足够长且强随机，包含大写、小写、数字、符号各至少一个，攻击者无法预测/爆破。
 */
public final class ComplexPasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SYMBOL = "!@#$%^&*()-_=+[]{}<>?";
    private static final String ALL = UPPER + LOWER + DIGIT + SYMBOL;

    private static final int DEFAULT_LENGTH = 24;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ComplexPasswordGenerator() {
    }

    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public static String generate(int length) {
        int len = Math.max(length, 12);
        List<Character> chars = new ArrayList<>(len);
        // 先保证每类至少一个
        chars.add(pick(UPPER));
        chars.add(pick(LOWER));
        chars.add(pick(DIGIT));
        chars.add(pick(SYMBOL));
        for (int i = chars.size(); i < len; i++) {
            chars.add(pick(ALL));
        }
        Collections.shuffle(chars, RANDOM);
        StringBuilder sb = new StringBuilder(len);
        for (Character c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static char pick(String pool) {
        return pool.charAt(RANDOM.nextInt(pool.length()));
    }

}
