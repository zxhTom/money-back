package cn.iocoder.yudao.module.system.framework.monitor.annotation;

import java.lang.annotation.*;

/**
 * 数据访问监控注解
 * 加在 Controller 分页查询方法上，框架自动记录访问日志并检查用户条数限额。
 *
 * 示例:
 * <pre>
 * {@literal @}GetMapping("/page")
 * {@literal @}DataAccessMonitor(module = "用户管理", entityType = "system_user")
 * public CommonResult<PageResult<UserRespVO>> getUserPage(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataAccessMonitor {

    /** 展示用模块名，如"用户管理"、"合同管理" */
    String module();

    /** 实体类型标识，如"system_user"、"contract"，用于匹配配置 */
    String entityType();
}
