package cn.iocoder.yudao.module.custom.framework.audit.util;

import cn.iocoder.yudao.framework.apilog.core.filter.SensitiveLogSanitizer;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.dal.dataobject.audit.AuditLogDO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/** Audit payloads are diagnostics, not a second copy of personal identity records. */
public final class AuditLogPrivacy {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern IDENTITY = Pattern.compile("(?<![A-Za-z0-9])(?:[1-9]\\d{16}[\\dXx]|[1-9]\\d{14})(?![A-Za-z0-9])");
    private static final Pattern SAFE_VALUE = Pattern.compile("(?:\\d{1,14}|true|false|CREATE|READ|UPDATE|DELETE|EXPORT|LOGIN|\\d{4}-\\d{2}-\\d{2}(?:[ T]\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?(?:Z|[+-]\\d{2}:\\d{2})?)?)");

    private AuditLogPrivacy() {}

    public static AuditLogDO safeCopy(AuditLogDO original) {
        if (original == null) return null;
        AuditLogDO copy = BeanUtils.toBean(original, AuditLogDO.class);
        copy.setRequestParams(payload(original.getRequestParams()));
        copy.setBeforeData(payload(original.getBeforeData()));
        copy.setAfterData(payload(original.getAfterData()));
        // Historical descriptions can embed names without a structured field name.
        copy.setOperation((original.getOperationType() == null ? "操作" : original.getOperationType())
                + (original.getEntityId() == null ? "" : " / ID=" + original.getEntityId()));
        copy.setUsername(original.getUsername() == null ? null : "[已隐藏，见用户ID]");
        copy.setErrorMessage(original.getErrorMessage() == null ? null : "操作失败（个人信息已隐藏）");
        if (original.getRequestUrl() != null) {
            String path = original.getRequestUrl().split("\\?", 2)[0];
            copy.setRequestUrl(IDENTITY.matcher(path).replaceAll("[证件已隐藏]"));
        }
        copy.setUserAgent(null);
        copy.setAllIpHeaders(null);
        return copy;
    }

    public static String payload(String input) {
        if (input == null || input.isEmpty()) return null;
        try {
            String redacted = SensitiveLogSanitizer.sanitize(input, "name", "nickname", "inviteeRealname");
            return MAPPER.writeValueAsString(clean(MAPPER.readTree(redacted), 0));
        } catch (Exception ignored) {
            return "\"[已隐藏：无法解析的审计数据]\"";
        }
    }

    private static JsonNode clean(JsonNode node, int depth) throws Exception {
        if (node == null || node.isNull()) return node;
        if (depth > 32) return TextNode.valueOf("[已隐藏：嵌套过深]");
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                field.setValue(clean(field.getValue(), depth + 1));
            }
        } else if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            for (int i = 0; i < array.size(); i++) array.set(i, clean(array.get(i), depth + 1));
        } else if (node.isTextual()) {
            String value = node.textValue().trim();
            if (value.startsWith("{") || value.startsWith("[")) {
                try {
                    return TextNode.valueOf(MAPPER.writeValueAsString(clean(MAPPER.readTree(value), depth + 1)));
                } catch (Exception ignored) {
                    return TextNode.valueOf("[已隐藏：无法解析的嵌套数据]");
                }
            }
            // Unknown free text can contain a name, address or embedded identity number.
            return SAFE_VALUE.matcher(value).matches() ? node : TextNode.valueOf("[已隐藏]");
        } else if (node.isNumber() && IDENTITY.matcher(node.asText()).find()) {
            return TextNode.valueOf("[证件已隐藏]");
        }
        return node;
    }
}
