package cn.iocoder.yudao.module.custom.framework.security.aop;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 数据泄露监控 AOP
 * 检测：单次大量数据查询、用户在短时间内累计查询敏感数据过多
 */
@Aspect
@Component
@Slf4j
public class DataLeakageMonitorAspect {

    // 单次查询返回敏感数据超过此行数触发 WARNING
    private static final int SINGLE_QUERY_WARNING_THRESHOLD = 500;
    // 5 分钟内累计查询超过此条数触发 CRITICAL
    private static final int CUMULATIVE_CRITICAL_THRESHOLD = 1000;
    // 滑动窗口 5 分钟
    private static final long WINDOW_SECONDS = 300;

    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 拦截所有返回 PageResult 的 Service 层方法
     */
    @Around("execution(cn.iocoder.yudao.framework.common.pojo.PageResult cn.iocoder.yudao.module.custom.service..*(..))")
    public Object monitorPageQuery(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (!(result instanceof PageResult)) return result;

        PageResult<?> page = (PageResult<?>) result;
        int size = page.getList() != null ? page.getList().size() : 0;
        if (size == 0) return result;

        Long userId = tryGetUserId();
        String methodName = pjp.getSignature().toShortString();

        // 单次大量查询告警
        if (size >= SINGLE_QUERY_WARNING_THRESHOLD) {
            String msg = String.format("单次查询返回 %d 条记录，方法：%s", size, methodName);
            log.warn("[DataLeakage] {}", msg);
            securityAlertService.saveAsync("DATA_LEAKAGE", 2, null, userId,
                    methodName, "QUERY", null, msg);
        }

        // 累计查询计数（按用户）
        if (userId != null) {
            String key = "security:data:query:" + userId;
            Long total = stringRedisTemplate.opsForValue().increment(key, size);
            if (total != null && total == size) {
                stringRedisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (total != null && total >= CUMULATIVE_CRITICAL_THRESHOLD) {
                String msg = String.format("用户 %d 在5分钟内累计查询 %d 条记录，方法：%s", userId, total, methodName);
                log.warn("[DataLeakage] {}", msg);
                securityAlertService.saveAsync("DATA_LEAKAGE", 3, null, userId,
                        methodName, "QUERY", null, msg);
                // 重置计数器，避免重复告警
                stringRedisTemplate.delete(key);
            }
        }

        return result;
    }

    private Long tryGetUserId() {
        try {
            return SecurityFrameworkUtils.getLoginUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
