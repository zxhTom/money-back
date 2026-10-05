package cn.iocoder.yudao.module.custom.util;

import cn.hutool.core.codec.Base64;
import java.util.Random;

public class AvatarGenerateUtil {
    
    private static final String[] COLORS = {
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
        "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50",
        "#8BC34A", "#CDDC39", "#FF9800", "#FF5722", "#795548", 
        "#9E9E9E", "#607D8B"
    };

    /**
     * 根据名字自动生成内联 SVG 的 Base64 编码图片
     */
    public static String generateAvatarBase64(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "U";
        }
        name = name.trim();
        String text;
        if (name.matches(".*[\\u4e00-\\u9fa5]+.*")) {
            text = name.length() > 2 ? name.substring(name.length() - 2) : name;
        } else {
            text = name.length() > 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
        }

        int hash = Math.abs(name.hashCode());
        String color = COLORS[hash % COLORS.length];

        String svg = "<svg width=\"200\" height=\"200\" xmlns=\"http://www.w3.org/2000/svg\">" +
                "<rect width=\"200\" height=\"200\" fill=\"" + color + "\"/>" +
                "<text x=\"100\" y=\"135\" font-family=\"sans-serif\" font-weight=\"bold\" font-size=\"80\" fill=\"#fff\" text-anchor=\"middle\">" + text + "</text>" +
                "</svg>";

        return "data:image/svg+xml;base64," + Base64.encode(svg);
    }
}
