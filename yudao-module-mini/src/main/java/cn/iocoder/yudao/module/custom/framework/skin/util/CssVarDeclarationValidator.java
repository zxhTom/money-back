package cn.iocoder.yudao.module.custom.framework.skin.util;

import java.util.regex.Pattern;

/**
 * 皮肤自定义 CSS 声明文本校验工具：只允许 --token: value; 形式的自定义属性声明，
 * 禁止选择器、伪类、媒体查询等注入内容。
 */
public class CssVarDeclarationValidator {

    private static final Pattern LINE_PATTERN = Pattern.compile("^--[a-zA-Z0-9-]+\\s*:\\s*[^;{}<>@]+;$");

    public static boolean isValid(String cssText) {
        if (cssText == null || cssText.isBlank()) {
            return true; // 允许为空
        }
        String[] lines = cssText.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (!LINE_PATTERN.matcher(trimmed).matches()) {
                return false;
            }
        }
        return true;
    }

}
