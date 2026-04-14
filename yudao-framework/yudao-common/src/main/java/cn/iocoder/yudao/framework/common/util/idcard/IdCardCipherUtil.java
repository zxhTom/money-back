package cn.iocoder.yudao.framework.common.util.idcard;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
/**
 * 身份证号码：AES-256-CBC 可逆加密（同一明文 + 同一密钥 → 同一密文），密文为 Base64(IV(16) || cipherBytes)。
 * <p>
 * 业务约定：数据库可存<strong>明文</strong>身份证；链路透传密文时用 {@link #encrypt}/{@link #decrypt}。
 * 比较、入库归一请用 {@link #resolveToPlainBestEffort}。
 */
public final class IdCardCipherUtil {

    private IdCardCipherUtil() {
    }

    /**
     * 是否像大陆身份证明文：18 位（末位可为 X）或 15 位全数字（宽松校验，避免误把 Base64 当明文）
     */
    public static boolean looksLikePlainIdCard(String s) {
        if (s == null) {
            return false;
        }
        String t = s.trim();
        if (t.length() == 18) {
            return t.matches("^[1-9]\\d{16}[0-9Xx]$");
        }
        if (t.length() == 15) {
            return t.matches("^[1-9]\\d{14}$");
        }
        return false;
    }

    /**
     * 用户手输明文归一：trim；18 位末位小写 x 转大写 X（与前端「大写归一」一致）。
     */
    public static String normalizePlainIdCard(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.length() == 18 && t.charAt(17) == 'x') {
            return t.substring(0, 17) + 'X';
        }
        return t;
    }

    /**
     * 展示：前 6 位 + 中间全部为 * + 后 4 位（长度与星号数量由后端统一）
     */
    public static String maskPlain(String plain) {
        if (plain == null || plain.isEmpty()) {
            return "";
        }
        String s = plain.trim();
        int n = s.length();
        if (n < 10) {
            return "****";
        }
        int mid = n - 10;
        String stars = repeatStar(mid);
        return s.substring(0, 6) + stars + s.substring(n - 4);
    }

    private static String repeatStar(int n) {
        if (n <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append('*');
        }
        return sb.toString();
    }

    public static String encrypt(String plain, String secret) {
        if (plain == null || plain.isEmpty()) {
            return null;
        }
        byte[] keyBytes = secretKeyBytes(secret);
        byte[] iv = ivFromPlain(secret, plain);
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, keyBytes, iv);
        byte[] enc = aes.encrypt(plain.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + enc.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(enc, 0, combined, iv.length, enc.length);
        return Base64.encode(combined);
    }

    public static String decrypt(String cipherBase64, String secret) {
        if (cipherBase64 == null || cipherBase64.isEmpty()) {
            return null;
        }
        byte[] combined = Base64.decode(cipherBase64);
        if (combined.length < 17) {
            throw new IllegalArgumentException("invalid id card cipher");
        }
        byte[] iv = Arrays.copyOfRange(combined, 0, 16);
        byte[] enc = Arrays.copyOfRange(combined, 16, combined.length);
        byte[] keyBytes = secretKeyBytes(secret);
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, keyBytes, iv);
        byte[] plain = aes.decrypt(enc);
        return new String(plain, StandardCharsets.UTF_8);
    }

    /**
     * 尽可能得到明文：形态像 15/18 位身份证则归一后返回；否则尝试 AES 解密；再否则返回 trim 原串（兼容无法识别的历史数据）。
     */
    public static String resolveToPlainBestEffort(String input, String secret) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        String t = input.trim();
        if (looksLikePlainIdCard(t)) {
            return normalizePlainIdCard(t);
        }
        try {
            String p = decrypt(t, secret);
            if (StrUtil.isBlank(p)) {
                return t;
            }
            return normalizePlainIdCard(p.trim());
        } catch (Exception ex) {
            return t;
        }
    }

    /**
     * @deprecated 请使用 {@link #resolveToPlainBestEffort}（入库明文）或 {@link #encrypt}（需密文时）
     */
    @Deprecated
    public static String normalizeRequestToCipher(String input, String secret) {
        return resolveToPlainBestEffort(input, secret);
    }

    /**
     * 库中值（可能是历史明文或密文）→ 对外统一返回密文
     */
    public static String storedToCipherForResponse(String stored, String secret) {
        if (StrUtil.isBlank(stored)) {
            return null;
        }
        String t = stored.trim();
        if (looksLikePlainIdCard(t)) {
            return encrypt(normalizePlainIdCard(t), secret);
        }
        try {
            decrypt(t, secret);
            return t;
        } catch (Exception ex) {
            return "*****";
//            throw new IllegalStateException("invalid id_no in database");
        }
    }

    /**
     * 库中值 → 展示串（不对外返回明文）
     */
    public static String maskFromStored(String stored, String secret) {
        if (StrUtil.isBlank(stored)) {
            return "";
        }
        String t = stored.trim();
        if (looksLikePlainIdCard(t)) {
            return maskPlain(normalizePlainIdCard(t));
        }
        try {
            return maskPlain(decrypt(t, secret));
        } catch (Exception ex) {
            return "****";
        }
    }

    /**
     * 明文与库中存储是否同一证件（请求可为明文或密文）
     */
    public static boolean sameIdCard(String plainOrCipherRequest, String storedDb, String secret) {
        if (plainOrCipherRequest == null || storedDb == null) {
            return false;
        }
        String a = resolveToPlainBestEffort(plainOrCipherRequest, secret);
        String b = resolveToPlainBestEffort(storedDb, secret);
        return a != null && a.equals(b);
    }

    private static byte[] secretKeyBytes(String secret) {
        if (secret == null || secret.length() < 8) {
            throw new IllegalArgumentException("id-card cipher secret must be at least 8 characters");
        }
        return Arrays.copyOf(DigestUtil.sha256(secret.getBytes(StandardCharsets.UTF_8)), 32);
    }

    private static byte[] ivFromPlain(String secret, String plain) {
        String s = secret + "|" + plain;
        byte[] h = DigestUtil.sha256(s.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOfRange(h, 0, 16);
    }
}
