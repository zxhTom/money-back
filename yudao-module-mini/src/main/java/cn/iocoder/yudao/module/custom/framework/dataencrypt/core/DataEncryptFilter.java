package cn.iocoder.yudao.module.custom.framework.dataencrypt.core;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.core.filter.ApiRequestFilter;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.config.DataEncryptProperties;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.core.annotation.DataEncryptIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 响应 data 加密过滤器
 *
 * 加密条件（全部满足）：
 * 1. 请求携带 Bearer Token（第三方入站回调、登录前接口天然跳过）
 * 2. URL 不在 ignore-urls 白名单，且 Handler 无 @DataEncryptIgnore
 * 3. 响应为 2xx 的 application/json，且为 CommonResult 形态、code=0、data 非空
 *
 * 加密失败时写回原文并记录错误日志（fail-open），保证业务可用性
 */
@Slf4j
public class DataEncryptFilter extends ApiRequestFilter {

    public static final String HEADER_NAME = "X-Data-Encrypt";
    public static final String DEBUG_HEADER_NAME = "X-Data-Encrypt-Debug";

    private final DataEncryptProperties properties;
    private final DataKeyService dataKeyService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public DataEncryptFilter(WebProperties webProperties,
                             DataEncryptProperties properties,
                             DataKeyService dataKeyService,
                             RequestMappingHandlerMapping requestMappingHandlerMapping) {
        super(webProperties);
        this.properties = properties;
        this.dataKeyService = dataKeyService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = DataKeyService.obtainBearerToken(request);
        if (token == null || isDebugRequest(request) || isIgnoreUrl(request) || hasIgnoreAnnotation(request)) {
            chain.doFilter(request, response);
            return;
        }

        DataEncryptResponseWrapper wrapper = new DataEncryptResponseWrapper(response);
        chain.doFilter(request, wrapper);

        byte[] body = wrapper.getBody();
        byte[] finalBody = body;
        if (isJsonSuccess(wrapper) && body.length > 0) {
            try {
                JsonNode root = JsonUtils.parseTree(new String(body, StandardCharsets.UTF_8));
                if (isEncryptableResult(root)) {
                    DataKeyService.KeyVersion kv = dataKeyService.current();
                    String keyPart = dataKeyService.keyPart(kv.version, kv.master, token);
                    byte[] sessionKey = DataKeyService.deriveSessionKey(properties.getClientSalt(), keyPart, token);
                    String cipher = DataKeyService.encryptData(sessionKey, kv.version, root.get("data").toString());
                    ((ObjectNode) root).put("data", cipher);
                    finalBody = root.toString().getBytes(StandardCharsets.UTF_8);
                    wrapper.setHeader(HEADER_NAME, "v" + kv.version);
                    wrapper.addHeader("Access-Control-Expose-Headers", HEADER_NAME);
                }
            } catch (Exception ex) {
                log.error("[doFilterInternal][url({}) 响应加密失败，降级返回明文]", request.getRequestURI(), ex);
                finalBody = body;
            }
        }
        wrapper.writeBody(finalBody);
    }

    private static boolean isEncryptableResult(JsonNode root) {
        if (root == null || !root.isObject()) {
            return false;
        }
        JsonNode code = root.get("code");
        JsonNode data = root.get("data");
        return code != null && code.isNumber() && code.asInt() == 0 && data != null && !data.isNull();
    }

    private static boolean isJsonSuccess(HttpServletResponse response) {
        if (response.getStatus() < HttpStatus.OK.value() || response.getStatus() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            return false;
        }
        String contentType = response.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private boolean isDebugRequest(HttpServletRequest request) {
        String secret = properties.getDebugSecret();
        if (StrUtil.isBlank(secret) || !secret.equals(request.getHeader(DEBUG_HEADER_NAME))) {
            return false;
        }
        log.info("[isDebugRequest][url({}) 命中调试密钥，本次响应跳过加密]", request.getRequestURI());
        return true;
    }

    private boolean isIgnoreUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return properties.getIgnoreUrls().stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    private boolean hasIgnoreAnnotation(HttpServletRequest request) {
        try {
            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }
            HandlerExecutionChain mappingHandler = requestMappingHandlerMapping.getHandler(request);
            if (mappingHandler != null && mappingHandler.getHandler() instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) mappingHandler.getHandler();
                return handlerMethod.getMethodAnnotation(DataEncryptIgnore.class) != null
                        || handlerMethod.getBeanType().getAnnotation(DataEncryptIgnore.class) != null;
            }
        } catch (Exception e) {
            log.error("[hasIgnoreAnnotation][url({}) 解析 @DataEncryptIgnore 失败]", request.getRequestURI(), e);
        }
        return false;
    }

}
