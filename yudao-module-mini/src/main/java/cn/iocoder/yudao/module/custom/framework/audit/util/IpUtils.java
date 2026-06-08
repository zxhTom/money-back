package cn.iocoder.yudao.module.custom.framework.audit.util;

import cn.hutool.core.util.StrUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

public class IpUtils {

    private static final String UNKNOWN = "unknown";

    /**
     * 获取外网IP：从代理头中取第一个非 unknown 的 IP
     */
    public static String getExternalIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Original-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个 IP，取第一个
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取直连IP（请求的实际来源，可能是反向代理地址）
     */
    public static String getDirectIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    /**
     * 获取所有 IP 来源信息，返回 JSON 字符串
     */
    public static String getAllIpHeadersJson(HttpServletRequest request) {
        Map<String, String> ips = new LinkedHashMap<>();
        String[] headers = {
                "X-Forwarded-For",
                "X-Original-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };
        for (String header : headers) {
            String val = request.getHeader(header);
            if (StrUtil.isNotBlank(val) && !UNKNOWN.equalsIgnoreCase(val)) {
                ips.put(header, val);
            }
        }
        ips.put("RemoteAddr", request.getRemoteAddr());
        // 简单 JSON 序列化，避免引入多余依赖
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : ips.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
