package cn.iocoder.yudao.module.system.framework.idcard;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.idcard.IdCardCipherUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_ID_CARD_DECRYPT_FAIL;

/**
 * 身份证号码工具：AES 可逆加密用于链路透传；<strong>入库字段存明文</strong>（经 {@link #resolveToPlain} 归一）。
 * <p>
 * {@link #normalizeRequestToCipher} 历史命名保留，语义为「归一成入库用明文」，不再返回密文。
 */
@Service
public class IdCardCipherService {

    @Resource
    private IdCardCipherProperties properties;

    public String getSecret() {
        return secret();
    }

    private String secret() {
        IdCardCipherProperties.Cipher c = properties.getCipher();
        if (c == null || StrUtil.isBlank(c.getSecret())) {
            throw new IllegalStateException("yudao.id-card.cipher.secret 未配置");
        }
        return c.getSecret().trim();
    }

    /**
     * 形态像身份证则返回归一明文；否则尝试解密；再否则返回 trim 原串（尽可能得到可用于入库/比对的明文）。
     */
    public String resolveToPlain(String input) {
        return IdCardCipherUtil.resolveToPlainBestEffort(input, secret());
    }

    /**
     * 严格模式：非身份证形态则必须能解密，否则抛错（如解密接口、强校验场景）。
     */
    public String decryptRequestToPlain(String input) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        String t = input.trim();
        if (IdCardCipherUtil.looksLikePlainIdCard(t)) {
            return IdCardCipherUtil.normalizePlainIdCard(t);
        }
        String p = decryptOrNull(t);
        if (p == null) {
            throw exception(USER_ID_CARD_DECRYPT_FAIL);
        }
        return IdCardCipherUtil.normalizePlainIdCard(p.trim());
    }

    /**
     * 与 {@link #resolveToPlain} 相同。历史名「normalizeRequestToCipher」易误解为返回密文，实际为<strong>入库明文</strong>。
     */
    public String normalizeRequestToCipher(String input) {
        return resolveToPlain(input);
    }

    /**
     * 任意入参（明文或密文）→ 归一明文后再 AES 加密，用于对外返回密文字段等。
     */
    public String requestToStoredCipher(String input) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        String plain = resolveToPlain(input);
        return IdCardCipherUtil.encrypt(plain, secret());
    }

    /**
     * 库中证件字段对外透出值：默认与 {@link #idNoDisplayFromStored} 同属 {@code yudao.id-card.display.plain-for-id-no-display} 策略。<br>
     * false：返回 AES 密文（Base64）；true：返回归一明文（与脱敏展示同为明文时，链路上 idNo 与 idNoDisplay 内容可能重复，慎用）。
     */
    public String storedToCipherForResponse(String stored) {
        if (StrUtil.isBlank(stored)) {
            return null;
        }
        IdCardCipherProperties.Display d = properties.getDisplay();
        if (d != null && d.isPlainForIdNoDisplay()) {
            return resolveToPlain(stored);
        }
        return IdCardCipherUtil.storedToCipherForResponse(stored, secret());
    }

    public String maskFromStored(String stored) {
        return IdCardCipherUtil.maskFromStored(stored, secret());
    }

    /**
     * 用于 {@code idNoDisplay} 等仅展示字段：由配置 {@code yudao.id-card.display.plain-for-id-no-display} 决定脱敏或完整明文。
     */
    public String idNoDisplayFromStored(String stored) {
        if (StrUtil.isBlank(stored)) {
            return "";
        }
        IdCardCipherProperties.Display d = properties.getDisplay();
        if (d != null && d.isPlainForIdNoDisplay()) {
            String p = resolveToPlain(stored);
            return p != null ? p : "";
        }
        return maskFromStored(stored);
    }

    public String decryptOrNull(String cipher) {
        if (StrUtil.isBlank(cipher)) {
            return null;
        }
        try {
            return IdCardCipherUtil.decrypt(cipher.trim(), secret());
        } catch (Exception ex) {
            return null;
        }
    }

    /** 两侧均可为明文或历史密文，归一后比较 */
    public boolean sameIdCard(String plainOrCipherRequest, String storedDb) {
        return IdCardCipherUtil.sameIdCard(plainOrCipherRequest, storedDb, secret());
    }

    /**
     * 登录态返回：按 {@link #storedToCipherForResponse} 改写 {@code user.idNo}（默认密文，配置明文时则为明文）
     */
    public void exposeCipherIdNoOnly(AdminUserDO user) {
        if (user == null) {
            return;
        }
        if (StrUtil.isBlank(user.getIdNo())) {
            return;
        }
        user.setIdNo(storedToCipherForResponse(user.getIdNo()));
    }

    /** 第三方实人等：尽可能得到明文身份证号 */
    public String decryptForExternalApi(String cipherOrPlain) {
        if (StrUtil.isBlank(cipherOrPlain)) {
            return cipherOrPlain;
        }
        return resolveToPlain(cipherOrPlain);
    }
}
