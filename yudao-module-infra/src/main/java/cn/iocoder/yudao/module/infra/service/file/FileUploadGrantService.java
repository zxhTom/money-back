package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

/** Binds a direct-upload registration to the exact server-issued path, URL and login identity. */
@Component
public class FileUploadGrantService {
    @Resource private StringRedisTemplate redis;
    private String key(Long configId, String path) {
        Long actor = SecurityFrameworkUtils.getLoginUserId();
        Long tenant = TenantContextHolder.getTenantId();
        if (actor == null || tenant == null || tenant <= 0 || configId == null || path == null) throw exception0(403, "缺少有效上传身份");
        try {
            String digest = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest((configId + "\n" + path).getBytes(StandardCharsets.UTF_8)));
            return "security:file-upload-grant:" + tenant + ":" + actor + ":" + digest;
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    public void issue(Long configId, String path, String url) {
        redis.opsForValue().set(key(configId, path), HttpUtils.removeUrlQuery(url), Duration.ofMinutes(30));
    }
    public String verify(Long configId, String path, String url) {
        String expected = redis.opsForValue().get(key(configId, path));
        if (expected == null || !Objects.equals(expected, HttpUtils.removeUrlQuery(url))) throw exception0(403, "上传凭据无效或已过期，请重新上传");
        return expected;
    }
    public void complete(Long configId, String path) { redis.delete(key(configId, path)); }
}
