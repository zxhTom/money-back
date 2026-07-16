package cn.iocoder.yudao.framework.common.util.servlet;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 客户端工具类
 *
 * @author 芋道源码
 */
public class ServletUtils {

    /**
     * 返回 JSON 字符串
     *
     * @param response 响应
     * @param object   对象，会序列化成 JSON 字符串
     */
    @SuppressWarnings("deprecation") // 必须使用 APPLICATION_JSON_UTF8_VALUE，否则会乱码
    public static void writeJSON(HttpServletResponse response, Object object) {
        String content = JsonUtils.toJsonString(object);
        ServletUtil.write(response, content, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     * 返回附件
     *
     * @param response 响应
     * @param filename 文件名
     * @param content  附件内容
     */
    public static void writeAttachment(HttpServletResponse response, String filename, byte[] content) throws IOException {
        // 设置 header 和 contentType
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, content);
    }

    /**
     * @param request 请求
     * @return ua
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "";
    }

    /**
     * 获得请求
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return getUserAgent(request);
    }

    public static String getClientIP() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return ServletUtil.getClientIP(request);
    }

    public static boolean isJsonRequest(ServletRequest request) {
        return StrUtil.startWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE);
    }

    public static String getBody(HttpServletRequest request) {
        // 只有在 json 请求在读取，因为只有 CacheRequestBodyFilter 才会进行缓存，支持重复读取
        if (isJsonRequest(request)) {
            return ServletUtil.getBody(request);
        }
        return null;
    }

    public static byte[] getBodyBytes(HttpServletRequest request) {
        // 只有在 json 请求在读取，因为只有 CacheRequestBodyFilter 才会进行缓存，支持重复读取
        if (isJsonRequest(request)) {
            return ServletUtil.getBodyBytes(request);
        }
        return null;
    }

    public static String getClientIP(HttpServletRequest request) {
        return ServletUtil.getClientIP(request);
    }

    /**
     * 获取可信的客户端真实 IP（用于审计/风控/黑名单等安全场景）。
     *
     * 与 {@link #getClientIP(HttpServletRequest)}（hutool 取 X-Forwarded-For 最左值）不同：
     * X-Forwarded-For 是明文请求头，最左值可被客户端任意伪造。这里按“单层可信反向代理(nginx)”模型取值：
     * <ol>
     *   <li>优先 {@code X-Real-IP}：nginx 用 {@code $remote_addr} 覆盖，客户端伪造不了；</li>
     *   <li>其次 {@code X-Forwarded-For} 取<b>最右一跳</b>：nginx 用 {@code $proxy_add_x_forwarded_for}
     *       把真实直连地址追加在末尾，最右值才是 nginx 亲眼所见的对端；最左值反而是可伪造的；</li>
     *   <li>都没有 → 直连对端 {@code getRemoteAddr()}（部署在 nginx 后通常是 nginx/内网地址）。</li>
     * </ol>
     * 注意：本方法假设只有一层对外可信代理(nginx)。若前面还有云 LB 等多层代理，需要按信任跳数调整。
     */
    public static String getClientRealIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (isValidIp(realIp)) {
            return realIp.trim();
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(xff)) {
            String[] parts = xff.split(",");
            for (int i = parts.length - 1; i >= 0; i--) {
                String ip = parts[i].trim();
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        return StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip.trim());
    }

    /**
     * 是否内网/私有地址（10/172.16-31/192.168/127 及 IPv6 回环）。
     */
    public static boolean isInternalIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return false;
        }
        String s = ip.trim();
        if (s.startsWith("127.") || s.startsWith("10.") || s.startsWith("192.168.")
                || s.equals("0:0:0:0:0:0:0:1") || s.equals("::1") || s.equalsIgnoreCase("localhost")) {
            return true;
        }
        if (s.startsWith("172.")) {
            // 172.16.0.0 ~ 172.31.255.255
            int dot = s.indexOf('.', 4);
            if (dot > 4) {
                try {
                    int second = Integer.parseInt(s.substring(4, dot));
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }

    public static Map<String, String> getParamMap(HttpServletRequest request) {
        return ServletUtil.getParamMap(request);
    }

    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        return ServletUtil.getHeaderMap(request);
    }

}
