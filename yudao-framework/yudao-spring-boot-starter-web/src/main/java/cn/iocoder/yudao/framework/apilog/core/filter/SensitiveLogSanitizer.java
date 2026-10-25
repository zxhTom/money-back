package cn.iocoder.yudao.framework.apilog.core.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.*;

/** Fail-closed redaction for new logs and historical JSON-in-JSON archives. */
public final class SensitiveLogSanitizer {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> PRIVATE_KEYS = new HashSet<>(Arrays.asList(
        "password", "passwd", "pwd", "paypassword", "oldpassword", "newpassword",
        "oldpaypassword", "newpaypassword", "passwordhash", "token", "accesstoken",
        "refreshtoken", "sessionkey", "accesskey", "accesssecret", "secretkey", "privatekey", "passphrase", "code", "verifycode", "emailcode", "smscode", "signature", "authorization", "secret", "clientsecret", "apikey", "apisecret", "appsecret", "mchkey", "apiv3key", "privatekeycontent", "keycontent", "encryptkey", "signingkey",
        "indebtedname", "indebtedid", "creditorname", "creditorid", "salary",
        "targetname", "targetidcard", "contactinfo", "realname", "username", "idno", "idcard", "idnodisplay", "mobile", "email", "address"));
    private SensitiveLogSanitizer() {}
    private static String normalize(String key) { return key.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT); }
    public static String sanitize(String json, String... extraKeys) {
        if (json == null || json.isEmpty()) return null;
        try {
            Set<String> extra = new HashSet<>();
            if (extraKeys != null) for (String key : extraKeys) if (key != null) extra.add(normalize(key));
            return MAPPER.writeValueAsString(clean(MAPPER.readTree(json), extra, 0));
        } catch (Exception ignored) { return "\"[REDACTED: invalid JSON]\""; }
    }
    private static JsonNode clean(JsonNode node, Set<String> extra, int depth) throws Exception {
        if (node == null || node.isNull()) return node;
        if (depth > 32) return TextNode.valueOf("[REDACTED: nesting limit]");
        if (node.isObject()) {
            JsonNode configKey = node.has("configKey") ? node.get("configKey") : node.get("key");
            if (configKey != null && configKey.isTextual()) {
                String name = normalize(configKey.textValue());
                if (name.contains("password") || name.contains("secret") || name.contains("credential")
                        || name.contains("token") || name.contains("privatekey") || name.contains("encryptkey")
                        || name.contains("apikey") || name.contains("mchkey")) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node).remove("value");
                }
            }
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> field = it.next();
                String key = normalize(field.getKey());
                if (PRIVATE_KEYS.contains(key) || key.endsWith("password") || key.endsWith("token") || key.endsWith("secret") || key.endsWith("privatekey") || extra.contains(key)) it.remove();
                else field.setValue(clean(field.getValue(), extra, depth + 1));
            }
        } else if (node.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode array = (com.fasterxml.jackson.databind.node.ArrayNode) node;
            for (int i = 0; i < array.size(); i++) array.set(i, clean(array.get(i), extra, depth + 1));
        } else if (node.isTextual()) {
            String text = node.textValue().trim();
            if (text.startsWith("{") || text.startsWith("[")) {
                try { return TextNode.valueOf(MAPPER.writeValueAsString(clean(MAPPER.readTree(text), extra, depth + 1))); }
                catch (Exception ignored) { return TextNode.valueOf("[REDACTED: invalid nested JSON]"); }
            }
        }
        return node;
    }
}
