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
    private AccessDeniedException denied() { return new AccessDeniedException("无权访问该人脸认证会话，请重新发起本人认证"); }
}
