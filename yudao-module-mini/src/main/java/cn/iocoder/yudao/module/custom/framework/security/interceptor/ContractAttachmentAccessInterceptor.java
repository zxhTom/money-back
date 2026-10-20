package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Contract uploads are private. Only short-lived server-issued links can download them. */
@Component
public class ContractAttachmentAccessInterceptor implements HandlerInterceptor {
    private static final long LIFETIME_SECONDS = 600;
    private static final String KEY = "security:contract-attachment:signing-key";
    @Resource private StringRedisTemplate stringRedisTemplate;
    static boolean isPrivatePath(String rawPath) {
        try {
            String decoded = URLDecoder.decode(rawPath, "UTF-8").replace('\\', '/');
            // Reject ambiguous paths before the storage client can normalize them into a private directory.
            if (decoded.contains("/../") || decoded.contains("/./") || decoded.contains("//")) return true;
            return decoded.contains("/get/private/contracts/");
        }
        catch (Exception ignored) { return true; }
    }
    private byte[] key() {
        String value = stringRedisTemplate.opsForValue().get(KEY);
        if (value == null) {
            byte[] random = new byte[32]; new SecureRandom().nextBytes(random);
            stringRedisTemplate.opsForValue().setIfAbsent(KEY, Base64.getEncoder().encodeToString(random));
            value = stringRedisTemplate.opsForValue().get(KEY);
        }
        if (value == null) throw new IllegalStateException("Attachment signing unavailable");
        return Base64.getDecoder().decode(value);
    }
    private String signature(String path, long expiry) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key(), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal((URLDecoder.decode(path.replace("+", "%2B"), "UTF-8") + "\n" + expiry).getBytes(StandardCharsets.UTF_8)));
    }
    public String signForAuthorizedResponse(String url, HttpServletRequest request) {
        if (url == null || url.isEmpty()) return url;
        try {
            URI uri = new URI(url);
            String path = uri.getRawPath();
            if (!isPrivatePath(path)) return url;
            if (!path.startsWith("/admin-api/infra/file/")) throw new IllegalArgumentException("Invalid private attachment path");
            long expiry = System.currentTimeMillis() / 1000 + LIFETIME_SECONDS;
            // Relative URL keeps the current environment and avoids trusting a client supplied Host header.
            String host = request.getServerName();
            if (!"staging.toms.chat".equals(host) && !"org.toms.chat".equals(host)) host = "org.toms.chat";
            return "https://" + host + path + "?expires=" + expiry + "&signature=" + signature(path, expiry);
        } catch (Exception ex) { throw new IllegalStateException("Cannot authorize attachment download"); }
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (!isPrivatePath(path)) return true;
        try {
            long expiry = Long.parseLong(request.getParameter("expires"));
            long now = System.currentTimeMillis() / 1000;
            String supplied = request.getParameter("signature");
            if (expiry > now && expiry <= now + LIFETIME_SECONDS && supplied != null
                && MessageDigest.isEqual(signature(path, expiry).getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                response.setHeader("Cache-Control", "private, no-store");
                response.setHeader("Referrer-Policy", "no-referrer");
                return true;
            }
        } catch (Exception ignored) { }
        response.setStatus(403);
        ServletUtils.writeJSON(response, CommonResult.error(403, "附件链接无效或已过期，请重新打开合同"));
        return false;
    }
}
