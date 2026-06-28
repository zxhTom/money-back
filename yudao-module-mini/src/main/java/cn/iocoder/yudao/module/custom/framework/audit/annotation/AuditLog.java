package cn.iocoder.yudao.module.custom.framework.audit.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    String module() default "";
    // 操作描述，支持 SpEL，例如 "创建合同-{{#reqVO.indebtedName}}"
    String operation() default "";
    AuditOperationType type();
    String entityType() default "";
    // SpEL 表达式，从方法返回值或参数中提取实体ID，如 "#result.data" 或 "#id"
    String entityIdExpression() default "";
    // 无登录态时（如 @PermitAll 接口），用 SpEL 从请求参数中提取用户标识作为 username
    String usernameExpression() default "";
    // 是否在执行前抓取操作对象的快照（需要 Service 配合）
    boolean captureBeforeData() default false;
    boolean captureAfterData() default false;
}
