package cn.iocoder.yudao.module.custom.framework.iconset.util;

import java.util.regex.Pattern;

/**
 * 图标集 SVG 源码校验工具：必须是形如 <svg ...>...</svg> 的整体标签，
 * 禁止 <script> 标签与 on* 事件属性，防止管理端粘贴的内容作为 data URI
 * 渲染到小程序端时携带注入内容。
 */
public class SvgIconValidator {

    private static final Pattern SVG_PATTERN =
            Pattern.compile("^<svg\\b[^>]*>.*</svg>$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FORBIDDEN_PATTERN =
            Pattern.compile("<script\\b|on[a-zA-Z]+\\s*=", Pattern.CASE_INSENSITIVE);

    public static boolean isValid(String svg) {
        if (svg == null || svg.trim().isEmpty()) {
            return true; // 允许为空，缺失的 key 由调用方回退到预设图案
        }
        String trimmed = svg.trim();
        if (!SVG_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        return !FORBIDDEN_PATTERN.matcher(trimmed).find();
    }

}
