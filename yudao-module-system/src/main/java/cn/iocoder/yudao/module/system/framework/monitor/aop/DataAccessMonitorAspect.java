package cn.iocoder.yudao.module.system.framework.monitor.aop;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.framework.monitor.annotation.DataAccessMonitor;
import cn.iocoder.yudao.module.system.service.monitor.DataAccessMonitorService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问监控 AOP
 * 拦截带 @DataAccessMonitor 的 Controller 方法，在返回后异步记录日志和检查限额。
 * 使用 @AfterReturning，不影响业务响应。
 */
@Aspect
@Component
@Slf4j
public class DataAccessMonitorAspect {

    @Resource
    private DataAccessMonitorService dataAccessMonitorService;

    @AfterReturning(
            pointcut = "@annotation(monitor)",
            returning = "returnValue"
    )
    public void afterReturning(JoinPoint joinPoint, DataAccessMonitor monitor, Object returnValue) {
        try {
            doMonitor(joinPoint, monitor, returnValue);
        } catch (Exception e) {
            // 监控异常绝不影响业务
            log.error("[DataAccessMonitor] AOP 处理异常，方法: {}", joinPoint.getSignature().toShortString(), e);
        }
    }

    private void doMonitor(JoinPoint joinPoint, DataAccessMonitor monitor, Object returnValue) {
        // 1. 提取 PageResult
        PageResult<?> pageResult = extractPageResult(returnValue);
        if (pageResult == null || pageResult.getList() == null || pageResult.getList().isEmpty()) return;

        // 2. 提取返回记录的 ID 列表
        List<Long> ids = extractIds(pageResult.getList());
        int count = pageResult.getList().size();

        // 3. 获取当前用户
        LoginUser loginUser = tryGetLoginUser();
        Long userId = loginUser != null ? loginUser.getId() : null;
        String username = loginUser != null ? loginUser.getUsername() : "anonymous";
        if (userId == null) return; // 未登录不记录

        // 4. 提取查询参数（方法第一个参数序列化为 JSON）
        String queryParams = extractQueryParams(joinPoint);

        // 5. 获取请求信息
        String requestUrl = "";
        String clientIp = "";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                requestUrl = request.getRequestURI();
                clientIp = getClientIp(request);
            }
        } catch (Exception ignored) {}

        // 6. 异步交给 Service 处理（写日志 + 计数 + 告警）
        dataAccessMonitorService.recordAccess(userId, username, monitor.module(), monitor.entityType(),
                queryParams, ids, count, requestUrl, clientIp);
    }

    @SuppressWarnings("unchecked")
    private PageResult<?> extractPageResult(Object returnValue) {
        if (returnValue instanceof PageResult) return (PageResult<?>) returnValue;
        if (returnValue instanceof CommonResult) {
            Object data = ((CommonResult<?>) returnValue).getData();
            if (data instanceof PageResult) return (PageResult<?>) data;
        }
        return null;
    }

    private List<Long> extractIds(List<?> list) {
        List<Long> ids = new ArrayList<>(list.size());
        for (Object item : list) {
            try {
                Field idField = findField(item.getClass(), "id");
                if (idField != null) {
                    idField.setAccessible(true);
                    Object val = idField.get(item);
                    if (val instanceof Long) ids.add((Long) val);
                    else if (val instanceof Number) ids.add(((Number) val).longValue());
                }
            } catch (Exception ignored) {}
        }
        return ids;
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try { return clazz.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private String extractQueryParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return "{}";
        try {
            return JSON.toJSONString(args[0]);
        } catch (Exception e) {
            return "{}";
        }
    }

    private LoginUser tryGetLoginUser() {
        try {
            return SecurityFrameworkUtils.getLoginUser();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
