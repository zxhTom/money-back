package cn.iocoder.yudao.module.custom.framework.security.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.framework.security.interceptor.ContractAttachmentAccessInterceptor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/** Signs attachments only after the business controller has authorized the contract response. */
@RestControllerAdvice(basePackages="cn.iocoder.yudao.module.custom.controller")
public class ContractAttachmentResponseAdvice implements ResponseBodyAdvice<Object> {
    @Resource private ContractAttachmentAccessInterceptor attachments;
    @Override public boolean supports(MethodParameter method, Class<? extends HttpMessageConverter<?>> converter) { return true; }
    @Override public Object beforeBodyWrite(Object body, MethodParameter method, MediaType type, Class<? extends HttpMessageConverter<?>> converter,
                                           ServerHttpRequest request, ServerHttpResponse response) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servlet = ((ServletServerHttpRequest) request).getServletRequest();
            if (servlet.getRequestURI().endsWith("/files/contract/upload") && body instanceof CommonResult) {
                CommonResult result = (CommonResult) body;
                if (result.getCode() == 0 && result.getData() instanceof String) result.setData(attachments.signForAuthorizedResponse((String) result.getData(), servlet));
            } else sign(body, servlet, 0);
        }
        return body;
    }
    private void sign(Object value, HttpServletRequest request, int depth) {
        if (value == null || depth > 8) return;
        if (value instanceof CommonResult) { sign(((CommonResult<?>) value).getData(), request, depth + 1); return; }
        if (value instanceof PageResult) { sign(((PageResult<?>) value).getList(), request, depth + 1); return; }
        if (value instanceof Iterable) { for (Object row : (Iterable<?>) value) sign(row, request, depth + 1); return; }
        if (!value.getClass().getName().startsWith("cn.iocoder.yudao.module.custom.")) return;
        try {
            java.lang.reflect.Method getter = value.getClass().getMethod("getFile");
            Object file = getter.invoke(value);
            if (file instanceof String) value.getClass().getMethod("setFile", String.class).invoke(value, attachments.signForAuthorizedResponse((String) file, request));
        } catch (NoSuchMethodException ignored) { }
        catch (Exception ex) { throw new IllegalStateException("Cannot authorize contract attachment"); }
    }
}
