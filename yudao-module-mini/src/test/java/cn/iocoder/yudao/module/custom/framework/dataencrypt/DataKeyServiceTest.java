package cn.iocoder.yudao.module.custom.framework.dataencrypt;

import cn.hutool.core.util.HexUtil;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.core.DataKeyService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataKeyServiceTest {

    // 以下期望值由 crypto-js@4.2 与 node 原生 crypto 双重计算得出，
    // 用于锁定 Java 端与 money-ui / 小程序端派生、加解密算法的跨端一致性
    private static final String SALT = "testSalt";
    private static final String KEY_PART = "aabbcc";
    private static final String TOKEN = "1234567890abcdefgh";
    private static final String EXPECTED_SESSION_KEY_HEX =
            "60712b50aa0e98235a233e1bf6fc8d36e6fe7f8c38ab018dfe562555e1db83cf";
    private static final String CRYPTO_JS_PAYLOAD =
            "v7:AAECAwQFBgcICQoLDA0OD7knJjNmAKUTxRu/kuqm7JgJCHBhtbiyJHijbaMgWLEV";
    private static final String PLAINTEXT = "{\"name\":\"张三\"}";

    @Test
    public void testDeriveSessionKey_matchesCryptoJs() {
        byte[] key = DataKeyService.deriveSessionKey(SALT, KEY_PART, TOKEN);
        assertEquals(EXPECTED_SESSION_KEY_HEX, HexUtil.encodeHexStr(key));
    }

    @Test
    public void testDecrypt_cryptoJsPayload() {
        byte[] key = DataKeyService.deriveSessionKey(SALT, KEY_PART, TOKEN);
        assertEquals(PLAINTEXT, DataKeyService.decryptData(key, CRYPTO_JS_PAYLOAD));
    }

    @Test
    public void testEncryptDecrypt_roundTrip() {
        byte[] key = DataKeyService.deriveSessionKey(SALT, KEY_PART, TOKEN);
        String payload = DataKeyService.encryptData(key, 3, PLAINTEXT);
        assertTrue(payload.startsWith("v3:"));
        assertEquals(PLAINTEXT, DataKeyService.decryptData(key, payload));
        // 随机 IV：两次加密结果不同
        assertNotEquals(payload, DataKeyService.encryptData(key, 3, PLAINTEXT));
    }

    @Test
    public void testDeriveSessionKey_shortToken() {
        // token 不足 8 位时使用整个 token
        byte[] key = DataKeyService.deriveSessionKey(SALT, KEY_PART, "abc");
        assertEquals(32, key.length);
    }

}
