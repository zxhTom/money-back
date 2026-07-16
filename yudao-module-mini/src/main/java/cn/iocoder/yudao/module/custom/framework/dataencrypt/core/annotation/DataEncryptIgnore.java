package cn.iocoder.yudao.module.custom.framework.dataencrypt.core.annotation;

import java.lang.annotation.*;

/**
 * 标注在 Controller 类或方法上，豁免响应 data 加密
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataEncryptIgnore {
}
