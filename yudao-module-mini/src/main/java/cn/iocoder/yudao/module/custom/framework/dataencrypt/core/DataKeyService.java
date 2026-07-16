package cn.iocoder.yudao.module.custom.framework.dataencrypt.core;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.config.DataEncryptProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 数据密钥管理：Redis 主密钥（版本化、可轮换）+ 按登录会话派生
 *
 * 派生规则（与 money-ui、小程序保持一致）：
 * keyPart    = HexHMAC-SHA256(key = masterKey, msg = accessToken)
 * sessionKey = HMAC-SHA256(key = clientSalt, msg = keyPart + "|" + token后8位)
 * 密文格式    = v{version}:Base64(IV(16B) || AES-256-CBC密文)
 */
@Service
public class DataKeyService {

    private static final String REDIS_VERSION_KEY = "data_encrypt:version";
    private static final String REDIS_MASTER_KEY_PREFIX = "data_encrypt:master:";
    private static final long LOCAL_CACHE_MILLIS = 5000;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DataEncryptProperties properties;

    private volatile long cachedVersion = -1;
    private volatile String cachedMaster;
    private volatile long cachedAt;

    public static class KeyVersion {
        public final long version;
        public final String master;

        public KeyVersion(long version, String master) {
            this.version = version;
            this.master = master;
        }
    }

    /**
     * 当前版本 + 主密钥（带 5 秒本地缓存）
     */
    public KeyVersion current() {
        if (cachedMaster != null && System.currentTimeMillis() - cachedAt < LOCAL_CACHE_MILLIS) {
            return new KeyVersion(cachedVersion, cachedMaster);
        }
        long version = ensureInit();
        String master = stringRedisTemplate.opsForValue().get(REDIS_MASTER_KEY_PREFIX + version);
        if (master == null) {
            throw new IllegalStateException("data-encrypt 主密钥缺失，version=" + version);
        }
        cachedVersion = version;
        cachedMaster = master;
        cachedAt = System.currentTimeMillis();
        return new KeyVersion(version, master);
    }

    /**
     * 指定版本主密钥；已过期/不存在时返回 null（调用方回退到当前版本）
     */
    public String getMaster(long version) {
        KeyVersion current = current();
        if (version == current.version) {
            return current.master;
        }
        return stringRedisTemplate.opsForValue().get(REDIS_MASTER_KEY_PREFIX + version);
    }

    /**
     * 轮换：版本 +1，旧主密钥保留宽限期
     */
    public synchronized long rotate() {
        ensureInit();
        Long newVersion = stringRedisTemplate.opsForValue().increment(REDIS_VERSION_KEY);
        if (newVersion == null) {
            throw new IllegalStateException("data-encrypt 版本号自增失败");
        }
        stringRedisTemplate.opsForValue().set(REDIS_MASTER_KEY_PREFIX + newVersion, randomMaster());
        stringRedisTemplate.expire(REDIS_MASTER_KEY_PREFIX + (newVersion - 1),
                properties.getGraceSeconds(), TimeUnit.SECONDS);
        cachedMaster = null; // 失效本地缓存
        return newVersion;
    }

    /**
     * 下发给客户端的半密钥
     */
    public String keyPart(long version, String master, String token) {
        return hmacSha256Hex(master.getBytes(StandardCharsets.UTF_8), token);
    }

    /**
     * 服务端加密：使用当前版本密钥
     */
    public String encryptForToken(String dataJson, String token) {
        KeyVersion kv = current();
        String keyPart = keyPart(kv.version, kv.master, token);
        byte[] sessionKey = deriveSessionKey(properties.getClientSalt(), keyPart, token);
        return encryptData(sessionKey, kv.version, dataJson);
    }

    private long ensureInit() {
        String version = stringRedisTemplate.opsForValue().get(REDIS_VERSION_KEY);
        if (version == null) {
            stringRedisTemplate.opsForValue().setIfAbsent(REDIS_MASTER_KEY_PREFIX + 1, randomMaster());
            stringRedisTemplate.opsForValue().setIfAbsent(REDIS_VERSION_KEY, "1");
            version = stringRedisTemplate.opsForValue().get(REDIS_VERSION_KEY);
        }
        return Long.parseLong(version);
    }

    private static String randomMaster() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexUtil.encodeHexStr(bytes);
    }

    public static String obtainBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return StrUtil.isBlank(token) ? null : token;
    }

    // ========= 纯算法部分（与客户端实现一一对应，供单测校验跨端一致性） =========

    public static String hmacSha256Hex(byte[] key, String message) {
        return HexUtil.encodeHexStr(hmacSha256(key, message));
    }

    public static byte[] hmacSha256(byte[] key, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 计算失败", e);
        }
    }

    public static byte[] deriveSessionKey(String clientSalt, String keyPart, String token) {
        String tokenTail = token.length() > 8 ? token.substring(token.length() - 8) : token;
        return hmacSha256(clientSalt.getBytes(StandardCharsets.UTF_8), keyPart + "|" + tokenTail);
    }

    public static String encryptData(byte[] sessionKey, long version, String dataJson) {
        try {
            byte[] iv = new byte[16];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sessionKey, "AES"), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(dataJson.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return "v" + version + ":" + Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("响应数据加密失败", e);
        }
    }

    public static String decryptData(byte[] sessionKey, String payload) {
        try {
            String base64 = payload.substring(payload.indexOf(':') + 1);
            byte[] bytes = Base64.getDecoder().decode(base64);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sessionKey, "AES"),
                    new IvParameterSpec(bytes, 0, 16));
            return new String(cipher.doFinal(bytes, 16, bytes.length - 16), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("响应数据解密失败", e);
        }
    }

}
