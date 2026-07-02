package cn.iocoder.yudao.module.custom.framework.audit.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import cn.iocoder.yudao.module.custom.framework.audit.annotation.AuditLog;
import cn.iocoder.yudao.module.custom.framework.audit.util.IpUtils;
import cn.iocoder.yudao.module.custom.service.audit.AuditLogService;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserUpdatePasswordReqVO;
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
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("\"(?i)(password|passwd|pwd|payPassword|oldPassword|newPassword)\"\\s*:\\s*\"[^\"]*\"");

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

    // 专门拦截 UserController.updateUserPassword，补录密码修改审计日志
    @Around("execution(* cn.iocoder.yudao.module.system.controller.admin.user.UserController.updateUserPassword(..))")
    public Object aroundUserPasswordUpdate(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        AuditLogDO logDO = new AuditLogDO();
        logDO.setModule("用户管理");
        logDO.setOperationType("UPDATE");
        logDO.setOperation("管理员重置用户密码");
        logDO.setEntityType("system_user");

        Object[] args = pjp.getArgs();
        if (args.length > 0 && args[0] instanceof UserUpdatePasswordReqVO) {
            UserUpdatePasswordReqVO reqVO = (UserUpdatePasswordReqVO) args[0];
            logDO.setEntityId(reqVO.getId());
        }

        fillUserInfo(logDO);
        fillRequestInfo(logDO);
        logDO.setRequestParams(captureRequestParams(pjp));

        Object result = null;
        try {
            result = pjp.proceed();
            logDO.setStatus(0);
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

        String operation = resolveOperation(auditLog.operation(), pjp);
        logDO.setOperation(operation);

        if (StrUtil.isNotBlank(auditLog.entityIdExpression())) {
            Long entityId = evalEntityId(auditLog.entityIdExpression(), pjp, null);
            if (entityId != null) logDO.setEntityId(entityId);
        }

        fillUserInfo(logDO);

        // 无登录态时（@PermitAll），用 usernameExpression 提取目标标识作为 username
        if (logDO.getUserId() == null && StrUtil.isNotBlank(auditLog.usernameExpression())) {
            String resolved = evalStringExpression(auditLog.usernameExpression(), pjp);
            if (StrUtil.isNotBlank(resolved)) {
                logDO.setUsername(resolved);
            }
        }

        fillRequestInfo(logDO);
        logDO.setRequestParams(captureRequestParams(pjp));

        return logDO;
    }

    private void fillUserInfo(AuditLogDO logDO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser != null) {
            logDO.setUserId(loginUser.getId());
            logDO.setUserType(loginUser.getUserType());
            String nickname = loginUser.getInfo() != null ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) : null;
            logDO.setUsername(nickname);
        }
    }

    private void fillRequestInfo(AuditLogDO logDO) {
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
    }

    private String captureRequestParams(ProceedingJoinPoint pjp) {
        try {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            Parameter[] params = sig.getMethod().getParameters();
            Object[] args = pjp.getArgs();
            for (int i = 0; i < params.length; i++) {
                Object arg = args[i];
                if (arg == null) continue;
                String clsName = arg.getClass().getName();
                // skip HttpServlet and primitive wrappers
                if (clsName.startsWith("javax.servlet") || clsName.startsWith("org.springframework")) continue;
                String json = toJson(arg);
                // redact password fields
                java.util.regex.Matcher pwMatcher = PASSWORD_PATTERN.matcher(json);
                StringBuffer sb = new StringBuffer();
                while (pwMatcher.find()) {
                    String key = pwMatcher.group(1);
                    pwMatcher.appendReplacement(sb, "\"" + key + "\":\"***\"");
                }
                pwMatcher.appendTail(sb);
                return sb.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String resolveOperation(String template, ProceedingJoinPoint pjp) {
        if (StrUtil.isBlank(template)) return "";
        if (!template.contains("{{")) return template;
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

    private String evalStringExpression(String expression, ProceedingJoinPoint pjp) {
        try {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            Parameter[] params = sig.getMethod().getParameters();
            Object[] args = pjp.getArgs();
            EvaluationContext ctx = new StandardEvaluationContext();
            for (int i = 0; i < params.length; i++) {
                ctx.setVariable(params[i].getName(), args[i]);
            }
            Object val = SPEL_PARSER.parseExpression(expression).getValue(ctx);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
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
