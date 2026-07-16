package cn.iocoder.yudao.module.custom.framework.audit.util;

import cn.hutool.core.util.StrUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

public class IpUtils {

    private static final String UNKNOWN = "unknown";

    /**
     * 获取客户端真实 IP（可信取法）。
     *
     * 统一委托给 {@link cn.iocoder.yudao.framework.common.util.servlet.ServletUtils#getClientRealIp}：
     * 优先 X-Real-IP、其次 X-Forwarded-For 最右一跳，避免最左 XFF 被伪造成内网IP。
     * 原先“取 XFF 最左值”的写法可被客户端伪造，已废弃。
     */
    public static String getExternalIp(HttpServletRequest request) {
        return cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientRealIp(request);
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
