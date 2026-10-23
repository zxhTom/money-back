package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClientConfig;
import java.util.*;

/** Response-only masks; persistence and storage clients retain the actual credentials. */
public final class FileConfigSecretPolicy {
    public static final String MASK = "********";
    private static final Set<String> FIELDS = new HashSet<>(Arrays.asList(
            "accesskey", "accesssecret", "secretkey", "password", "privatekey", "passphrase", "apikey", "token"));
    private FileConfigSecretPolicy() {}
    private static boolean secret(String key) { return FIELDS.contains(key.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT)); }
    public static Map<String, Object> asMap(FileClientConfig config) {
        return config == null ? new LinkedHashMap<>() : JsonUtils.parseObject(JsonUtils.toJsonString(config), Map.class);
    }
    public static Map<String, Object> masked(FileClientConfig config) {
        Map<String, Object> copy = asMap(config);
        copy.replaceAll((key, value) -> secret(key) && value != null && !value.toString().isEmpty() ? MASK : value);
        return copy;
    }
    public static Map<String, Object> mergeUpdate(FileClientConfig current, Map<String, Object> input) {
        Map<String, Object> copy = input == null ? new LinkedHashMap<>() : new LinkedHashMap<>(input);
        asMap(current).forEach((key, oldValue) -> {
            if (!secret(key)) return;
            Object supplied = copy.get(key);
            if (supplied == null || supplied.toString().trim().isEmpty() || MASK.equals(supplied)) copy.put(key, oldValue);
        });
        return copy;
    }
}
