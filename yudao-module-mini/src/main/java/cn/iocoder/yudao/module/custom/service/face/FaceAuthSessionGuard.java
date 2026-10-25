package cn.iocoder.yudao.module.custom.service.face;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.framework.faceauth.FaceAuthCallbackTokenStore;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class FaceAuthSessionGuard {
    @Resource private AdminUserService users;
    @Resource private IdCardCipherService cipher;
    @Resource private StringRedisTemplate redis;
    @Resource private FaceAuthCallbackTokenStore legacy;
    private AdminUserDO caller() {
        Long id = SecurityFrameworkUtils.getLoginUserId();
        AdminUserDO user = id == null ? null : users.getUser(id);
        if (user == null || StrUtil.isBlank(user.getIdNo()) || TenantContextHolder.getTenantId() == null
                || !java.util.Objects.equals(user.getTenantId(), TenantContextHolder.getTenantId()))
            throw denied();
        return user;
    }
    public AdminUserDO requireIdentity(String idCard) {
        AdminUserDO user = caller();
        if (StrUtil.isBlank(idCard) || !cipher.sameIdCard(user.getIdNo(), idCard)) throw denied();
        return user;
    }
    private String owner(AdminUserDO user) {
        return TenantContextHolder.getTenantId() + ":" + user.getId() + ":" + DigestUtil.sha256Hex(cipher.normalizeRequestToCipher(user.getIdNo()));
    }
    private String key(String token) { return "face:verify:owner:" + DigestUtil.sha256Hex(token); }
    public void bind(String token, String idCard) {
        AdminUserDO user = requireIdentity(idCard);
        redis.opsForValue().set(key(token), owner(user), 30, TimeUnit.MINUTES);
    }
    public void requireToken(String token) {
        AdminUserDO user = caller();
        if (StrUtil.isBlank(token)) throw denied();
        String saved = redis.opsForValue().get(key(token));
        if (owner(user).equals(saved)) return;
        if (saved != null) throw denied();
        // Sessions started by the released client immediately before an update remain usable.
        // Adopt only a token already mapped to this caller's identity, never an arbitrary token.
        for (String id : new String[]{user.getIdNo(), cipher.normalizeRequestToCipher(user.getIdNo()), cipher.resolveToPlain(user.getIdNo())}) {
            if (token.equals(legacy.get(id))) { bind(token, user.getIdNo()); return; }
        }
        throw denied();
    }
    public static class SessionOwner {
        private final Long tenantId;
        private final Long userId;
        private final String idHash;

        public SessionOwner(Long tenantId, Long userId, String idHash) {
            this.tenantId = tenantId;
            this.userId = userId;
            this.idHash = idHash;
        }

        public Long getTenantId() { return tenantId; }
        public Long getUserId() { return userId; }
        public String getIdHash() { return idHash; }
    }

    public SessionOwner getTrustedOwner(String token) {
        if (StrUtil.isBlank(token)) return null;
        String saved = redis.opsForValue().get(key(token));
        if (StrUtil.isBlank(saved)) return null;
        String[] parts = saved.split(":");
        if (parts.length != 3) return null;
        try {
            Long tenantId = Long.parseLong(parts[0]);
            Long userId = Long.parseLong(parts[1]);
            String idHash = parts[2];
            // 拒绝非正 tenant/user 及非 64 位 hex 身份摘要
            if (tenantId <= 0 || userId <= 0) return null;
            if (idHash == null || idHash.length() != 64 || !idHash.matches("^[0-9a-fA-F]{64}$")) return null;
            return new SessionOwner(tenantId, userId, idHash);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean matchesIdentity(SessionOwner owner, String idCard) {
        if (owner == null || StrUtil.isBlank(owner.getIdHash()) || StrUtil.isBlank(idCard)) {
            return false;
        }
        String normalizedCipher = cipher.normalizeRequestToCipher(idCard);
        String candidateHash = DigestUtil.sha256Hex(normalizedCipher);
        return owner.getIdHash().equalsIgnoreCase(candidateHash);
    }

    private AccessDeniedException denied() { return new AccessDeniedException("无权访问该人脸认证会话，请重新发起本人认证"); }
}
