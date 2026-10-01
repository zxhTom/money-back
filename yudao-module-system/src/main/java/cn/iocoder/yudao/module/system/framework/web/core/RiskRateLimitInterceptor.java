package cn.iocoder.yudao.module.system.framework.web.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskRateLimitConfigDO;
import cn.iocoder.yudao.module.system.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.risk.RiskLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 全局 API 访问频率限制拦截器
 *
 * 当已登录用户请求匹配到启用的频控规则且频次超限时，自动执行阶梯锁定并拦截请求。
 */
@Component
@Slf4j
public class RiskRateLimitInterceptor implements HandlerInterceptor {

    private static final String REDIS_KEY_API_LIMIT = "risk:api_limit:";

    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Resource
    private RiskLockService riskLockService;

    @Resource
    private PermissionService permissionService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 获取当前登录用户 ID。如果没有登录，则直接放行。
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return true;
        }

        // 检查用户当前锁定状态（若已被锁定直接抛出异常拦截）
        riskLockService.checkLockStatus(userId);

        // 2. 获得当前请求的 URI
        String uri = request.getRequestURI();

        // 3. 遍历所有的启用的频控规则
        List<RiskRateLimitConfigDO> rules = riskLockService.getAllEnableRateLimitRules();
        if (CollUtil.isEmpty(rules)) {
            return true;
        }

        for (RiskRateLimitConfigDO rule : rules) {
            if (rule.getId() == null || StrUtil.isBlank(rule.getApiPattern())
                    || rule.getTimeWindow() == null || rule.getMaxCount() == null) {
                continue;
            }

            // 4. 使用 AntPathMatcher 匹配 rule.apiPattern 和当前 URI
            if (!isPathMatch(rule.getApiPattern(), uri)) {
                continue;
            }

            // 检查规则是否适用于该用户（如果 rule.roleId 不为空，则校验用户是否拥有该角色；如果为空，则适用于所有人）
            if (rule.getRoleId() != null) {
                Set<Long> userRoleIds = permissionService.getUserRoleIdListByUserIdFromCache(userId);
                if (CollUtil.isEmpty(userRoleIds) || !userRoleIds.contains(rule.getRoleId())) {
                    continue;
                }
            }

            // 5. 适用该规则时，使用 Redis 计数器校验频率
            String key = REDIS_KEY_API_LIMIT + rule.getId() + ":" + userId;
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && (count == 1 || stringRedisTemplate.getExpire(key) == -1)) {
                stringRedisTemplate.expire(key, rule.getTimeWindow(), TimeUnit.SECONDS);
            }

            // 如果 count > rule.maxCount，说明触发限流！
            if (count != null && count > rule.getMaxCount()) {
                log.warn("[RiskRateLimitInterceptor][用户({}) 触发限流，规则ID: {}, Pattern: {}, 频次: {}/{}]",
                        userId, rule.getId(), rule.getApiPattern(), count, rule.getMaxCount());
                // 6. 阶梯锁定处理
                riskLockService.escalateLock(userId, "RATE_LIMIT", rule);
                // 抛出自定义异常拦截当前请求
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.USER_RATE_LIMIT_LOCKED);
            }
        }

        return true;
    }

    private boolean isPathMatch(String pattern, String uri) {
        if (StrUtil.isBlank(pattern) || StrUtil.isBlank(uri)) {
            return false;
        }
        pattern = pattern.trim();
        uri = uri.trim();
        if (pathMatcher.match(pattern, uri)) {
            return true;
        }
        if (uri.startsWith("/admin-api") && pathMatcher.match(pattern, uri.substring("/admin-api".length()))) {
            return true;
        }
        if (pattern.startsWith("/admin-api") && !uri.startsWith("/admin-api") && pathMatcher.match(pattern, "/admin-api" + uri)) {
            return true;
        }
        return false;
    }

}
