package cn.iocoder.yudao.module.custom.framework.iconset.util;

import java.util.regex.Pattern;

/**
 * 图标集图标值校验工具。
 * 合法值分为三类：
 * 1. SVG 源码：必须为完整的 {@code <svg ...>...</svg>} 标签，禁止 script/事件属性
 * 2. 图片 URL：以 http:// 或 https:// 开头的远端图片地址
 * 3. 本地路径：以 / 开头的小程序 bundle 内相对路径（.png/.jpg/.gif/.svg/.webp 结尾）
 *
 * 空值视为合法（由调用方回退到预设图案）。
 */
public class SvgIconValidator {

    private static final Pattern SVG_PATTERN =
            Pattern.compile("^<svg\\b[^>]*>.*</svg>$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FORBIDDEN_PATTERN =
            Pattern.compile("<script\\b|on[a-zA-Z]+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://.+", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCAL_PATH_PATTERN =
            Pattern.compile("^/.*\\.(png|jpg|jpeg|gif|svg|webp)$", Pattern.CASE_INSENSITIVE);

    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        String t = value.trim();
        if (URL_PATTERN.matcher(t).matches()) return true;
        if (LOCAL_PATH_PATTERN.matcher(t).matches()) return true;
        if (!SVG_PATTERN.matcher(t).matches()) return false;
        return !FORBIDDEN_PATTERN.matcher(t).find();
    }

    public static boolean isImageUrl(String value) {
        return value != null && value.trim().startsWith("http");
    }

    public static boolean isLocalPath(String value) {
        return value != null && LOCAL_PATH_PATTERN.matcher(value.trim()).matches();
    }

}
