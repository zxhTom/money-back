package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.enums.oauth2.OAuth2ClientConstants;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

/** Short-lived, actor-bound account switching. The target receives only its own permissions. */
@Service
@Slf4j
public class AccountImpersonationService {
    public static final String SCOPE = cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi.ACCOUNT_IMPERSONATION_SCOPE;
    private static final String KEY_PREFIX = "money:account-impersonation:v1:";
    private static final long MINUTES = 15;

    @Resource @Lazy private OAuth2TokenService tokenService;
    @Resource private AdminUserService userService;
    @Resource private AdminUserMapper userMapper;
    @Resource private PermissionService permissionService;
    @Resource private StringRedisTemplate redis;

    @Data
    public static class Session {
        private Long actorId;
        private Long targetId;
        private String targetUsername;
        private String targetName;
        private String accessToken;
        private LocalDateTime expiresAt;
    }

    @Data
    public static class UserChoice {
        private Long id;
        private String username;
        private String name;
    }

    public List<UserChoice> search(String actorToken, String keyword) {
        OAuth2AccessTokenDO actor = requireAdmin(actorToken);
        String query = StrUtil.trimToEmpty(keyword);
        if (query.matches("[0-9]{1,18}")) {
            try {
                AdminUserDO found = userService.getUserSimple(Long.valueOf(query));
                if (found == null || !actor.getTenantId().equals(found.getTenantId())
                        || !CommonStatusEnum.ENABLE.getStatus().equals(found.getStatus())
                        || !canTarget(actor.getUserId(), found.getId())) return Collections.emptyList();
                return Collections.singletonList(choice(found));
            } catch (NumberFormatException ex) { return Collections.emptyList(); }
        }
        if (query.length() < 2 || query.length() > 40) return Collections.emptyList();
        LambdaQueryWrapper<AdminUserDO> wrapper = new LambdaQueryWrapper<AdminUserDO>()
                .select(AdminUserDO::getId, AdminUserDO::getTenantId, AdminUserDO::getUsername,
                        AdminUserDO::getNickname, AdminUserDO::getRealname, AdminUserDO::getStatus)
                .eq(AdminUserDO::getTenantId, actor.getTenantId())
                .and(w -> w.like(AdminUserDO::getUsername, query)
                        .or().like(AdminUserDO::getRealname, query)
                        .or().like(AdminUserDO::getNickname, query))
                .orderByDesc(AdminUserDO::getId).last("LIMIT 20");
        return userMapper.selectList(wrapper).stream()
                .filter(user -> CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus()))
                .filter(user -> canTarget(actor.getUserId(), user.getId()))
                .map(this::choice).collect(Collectors.toList());
    }

    public boolean canStart(String actorToken) {
        try { requireAdmin(actorToken); return true; }
        catch (RuntimeException ex) { return false; }
    }

    private UserChoice choice(AdminUserDO user) {
        UserChoice choice = new UserChoice();
        choice.id = user.getId();
        choice.username = user.getUsername();
        choice.name = StrUtil.blankToDefault(user.getRealname(), user.getNickname());
        return choice;
    }

    public Session start(String actorToken, Long targetId, String password) {
        OAuth2AccessTokenDO actor = requireAdmin(actorToken);
        AdminUserDO actorUser = userService.getUserSimple(actor.getUserId());
        if (actorUser == null || StrUtil.isBlank(password)
                || !userService.isPasswordMatch(password, actorUser.getPassword()))
            throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "当前管理员登录密码不正确");
        AdminUserDO target = targetId == null ? null : userService.getUserSimple(targetId);
        if (target == null || !actor.getTenantId().equals(target.getTenantId())
                || !CommonStatusEnum.ENABLE.getStatus().equals(target.getStatus())
                || !canTarget(actor.getUserId(), targetId))
            throw new AccessDeniedException("目标账号不可切换");
        OAuth2AccessTokenDO issued = tokenService.createAccessToken(targetId, UserTypeEnum.ADMIN.getValue(),
                OAuth2ClientConstants.CLIENT_ID_DEFAULT, Collections.singletonList(SCOPE));
        try {
            redis.opsForValue().set(KEY_PREFIX + issued.getAccessToken(),
                    actor.getUserId() + "|" + actor.getTenantId() + "|" + targetId + "|" + actorToken,
                    MINUTES, TimeUnit.MINUTES);
        } catch (RuntimeException ex) {
            tokenService.removeAccessToken(issued.getAccessToken());
            throw ex;
        }
        Session session = new Session();
        session.actorId = actor.getUserId();
        session.targetId = targetId;
        session.targetUsername = target.getUsername();
        session.targetName = StrUtil.blankToDefault(target.getRealname(), target.getNickname());
        session.accessToken = issued.getAccessToken();
        session.expiresAt = LocalDateTime.now().plusMinutes(MINUTES);
        log.info("[accountSwitch][start] actor={} target={} tenant={}", actor.getUserId(), targetId, actor.getTenantId());
        return session;
    }

    public Session status(String targetToken) {
        OAuth2AccessTokenDO token = tokenService.checkAccessToken(targetToken);
        if (!isImpersonation(token)) throw new AccessDeniedException("当前不是切换会话");
        String[] parts = sessionParts(targetToken);
        AdminUserDO target = userService.getUserSimple(token.getUserId());
        Session session = new Session();
        session.actorId = Long.valueOf(parts[0]);
        session.targetId = token.getUserId();
        session.targetUsername = target.getUsername();
        session.targetName = StrUtil.blankToDefault(target.getRealname(), target.getNickname());
        return session;
    }

    public void stop(String targetToken) {
        Session session = status(targetToken);
        redis.delete(KEY_PREFIX + targetToken);
        tokenService.removeAccessToken(targetToken);
        log.info("[accountSwitch][stop] actor={} target={}", session.actorId, session.targetId);
    }

    /** Called on every target-token request; missing Redis state fails closed. */
    public void validate(OAuth2AccessTokenDO target, String targetToken) {
        String[] parts = sessionParts(targetToken);
        try {
            Long actorId = Long.valueOf(parts[0]);
            Long tenantId = Long.valueOf(parts[1]);
            Long targetId = Long.valueOf(parts[2]);
            if (!tenantId.equals(target.getTenantId()) || !targetId.equals(target.getUserId())) throw denied();
            OAuth2AccessTokenDO actor = tokenService.getAccessToken(parts[3]);
            if (actor == null || isImpersonation(actor)
                    || StrUtil.equals(actor.getAccessToken(), actor.getRefreshToken())
                    || !actorId.equals(actor.getUserId())
                    || !tenantId.equals(actor.getTenantId()) || actor.getExpiresTime() == null
                    || !actor.getExpiresTime().isAfter(LocalDateTime.now())) throw denied();
            AdminUserDO actorUser = TenantUtils.execute(tenantId, () -> userService.getUserSimple(actorId));
            AdminUserDO targetUser = TenantUtils.execute(tenantId, () -> userService.getUserSimple(targetId));
            if (actorUser == null || !tenantId.equals(actorUser.getTenantId())
                    || !CommonStatusEnum.ENABLE.getStatus().equals(actorUser.getStatus())
                    || !TenantUtils.execute(tenantId, () -> permissionService.hasAnyRoles(actorId, "super_admin", "tenant_admin"))
                    || targetUser == null || !tenantId.equals(targetUser.getTenantId())
                    || !CommonStatusEnum.ENABLE.getStatus().equals(targetUser.getStatus())
                    || !TenantUtils.execute(tenantId, () -> canTarget(actorId, targetId))) throw denied();
            java.util.Map<String, String> info = target.getUserInfo() == null
                    ? new HashMap<>() : new HashMap<>(target.getUserInfo());
            info.put("_impersonatorId", actorId.toString());
            target.setUserInfo(info);
        } catch (NumberFormatException ex) { throw denied(); }
    }

    public static boolean isImpersonation(OAuth2AccessTokenDO token) {
        return token != null && token.getScopes() != null && token.getScopes().contains(SCOPE);
    }

    private String[] sessionParts(String token) {
        String value = redis.opsForValue().get(KEY_PREFIX + token);
        if (value == null) throw denied();
        String[] parts = value.split("\\|", 4);
        if (parts.length != 4) throw denied();
        return parts;
    }

    private OAuth2AccessTokenDO requireAdmin(String actorToken) {
        if (StrUtil.isBlank(actorToken)) throw denied();
        OAuth2AccessTokenDO actor = tokenService.checkAccessToken(actorToken);
        if (isImpersonation(actor) || StrUtil.equals(actor.getAccessToken(), actor.getRefreshToken())
                || !UserTypeEnum.ADMIN.getValue().equals(actor.getUserType())
                || actor.getTenantId() == null || actor.getUserId() == null
                || !actor.getTenantId().equals(TenantContextHolder.getTenantId())
                || !permissionService.hasAnyRoles(actor.getUserId(), "super_admin", "tenant_admin")) throw denied();
        return actor;
    }

    private boolean canTarget(Long actorId, Long targetId) {
        if (actorId.equals(targetId)) return false;
        if (permissionService.hasAnyRoles(actorId, "super_admin")) return true;
        return !permissionService.hasAnyRoles(targetId,
                "super_admin", "tenant_admin", "contract-manager", "crm_admin")
                && !permissionService.hasAnyPermissions(targetId,
                "system:user:query", "system:role:query", "system:tenant:query", "mini:admin:dashboard");
    }

    private AccessDeniedException denied() { return new AccessDeniedException("账号切换会话无效或无权限"); }
}
