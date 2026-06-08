package cn.iocoder.yudao.module.custom.framework.audit.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import cn.iocoder.yudao.module.custom.framework.audit.annotation.AuditLog;
import cn.iocoder.yudao.module.custom.framework.audit.util.IpUtils;
import cn.iocoder.yudao.module.custom.service.audit.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

    @Resource
    private AuditLogService auditLogService;
    @Resource
    private ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        AuditLogDO logDO = buildBaseLog(pjp, auditLog);

        Object result = null;
        try {
            result = pjp.proceed();
            logDO.setStatus(0);
            if (auditLog.captureAfterData() && result != null) {
                logDO.setAfterData(toJson(result));
            }
            // 解析实体 ID（从返回值中）
            if (StrUtil.isNotBlank(auditLog.entityIdExpression()) && result != null) {
                Long entityId = evalEntityId(auditLog.entityIdExpression(), pjp, result);
                if (entityId != null) logDO.setEntityId(entityId);
            }
        } catch (Throwable e) {
            logDO.setStatus(1);
            logDO.setErrorMessage(truncate(e.getMessage(), 500));
            throw e;
        } finally {
            logDO.setCostTime((int) (System.currentTimeMillis() - startTime));
            logDO.setCreateTime(LocalDateTime.now());
            auditLogService.saveAsync(logDO);
        }
        return result;
    }

    private AuditLogDO buildBaseLog(ProceedingJoinPoint pjp, AuditLog auditLog) {
        AuditLogDO logDO = new AuditLogDO();
        logDO.setModule(auditLog.module());
        logDO.setOperationType(auditLog.type().name());
        logDO.setEntityType(auditLog.entityType());

        // 解析操作描述（支持简单 {{#param}} 模板替换）
        String operation = resolveOperation(auditLog.operation(), pjp);
        logDO.setOperation(operation);

        // 解析实体 ID（从方法参数中）
        if (StrUtil.isNotBlank(auditLog.entityIdExpression())) {
            Long entityId = evalEntityId(auditLog.entityIdExpression(), pjp, null);
            if (entityId != null) logDO.setEntityId(entityId);
        }

        // 填充当前用户信息
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser != null) {
            logDO.setUserId(loginUser.getId());
            logDO.setUserType(loginUser.getUserType());
            String nickname = loginUser.getInfo() != null ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) : null;
            logDO.setUsername(nickname);
        }

        // 填充请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            logDO.setRequestUrl(request.getRequestURI());
            logDO.setRequestMethod(request.getMethod());
            logDO.setExternalIp(IpUtils.getExternalIp(request));
            logDO.setDirectIp(IpUtils.getDirectIp(request));
            logDO.setAllIpHeaders(IpUtils.getAllIpHeadersJson(request));
            logDO.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
        }

        return logDO;
    }

    private String resolveOperation(String template, ProceedingJoinPoint pjp) {
        if (StrUtil.isBlank(template)) return "";
        if (!template.contains("{{")) return template;
        // 将 {{#expr}} 替换为实际值
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Parameter[] params = sig.getMethod().getParameters();
        Object[] args = pjp.getArgs();
        EvaluationContext ctx = new StandardEvaluationContext();
        for (int i = 0; i < params.length; i++) {
            ctx.setVariable(params[i].getName(), args[i]);
        }
        try {
            Matcher matcher = TEMPLATE_PATTERN.matcher(template);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String expr = matcher.group(1);
                String replacement = "";
                try {
                    Object val = SPEL_PARSER.parseExpression(expr).getValue(ctx);
                    if (val != null) replacement = Matcher.quoteReplacement(val.toString());
                } catch (Exception ignored) {
                }
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return template;
        }
    }

    private Long evalEntityId(String expression, ProceedingJoinPoint pjp, Object result) {
        try {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            Parameter[] params = sig.getMethod().getParameters();
            Object[] args = pjp.getArgs();
            EvaluationContext ctx = new StandardEvaluationContext();
            for (int i = 0; i < params.length; i++) {
                ctx.setVariable(params[i].getName(), args[i]);
            }
            if (result != null) {
                ctx.setVariable("result", result);
            }
            Expression expr = SPEL_PARSER.parseExpression(expression);
            Object val = expr.getValue(ctx);
            if (val instanceof Long) return (Long) val;
            if (val instanceof Number) return ((Number) val).longValue();
            if (val instanceof String) return Long.parseLong((String) val);
        } catch (Exception e) {
            // SpEL 解析失败，忽略
        }
        return null;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max);
    }
}
