package cn.iocoder.yudao.module.custom.framework.iconset.util;

import java.util.regex.Pattern;
import java.util.*;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

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

    private static final Set<String> TAGS = new HashSet<>(Arrays.asList(
            "svg", "g", "path", "circle", "ellipse", "rect", "line", "polyline", "polygon",
            "defs", "lineargradient", "radialgradient", "stop", "clippath", "mask", "symbol", "use", "title", "desc", "text", "tspan"));
    private static final Set<String> ATTRS = new HashSet<>(Arrays.asList(
            "xmlns", "xmlns:xlink", "viewbox", "width", "height", "x", "y", "x1", "x2", "y1", "y2",
            "cx", "cy", "r", "rx", "ry", "d", "points", "fill", "stroke", "stroke-width", "stroke-linecap",
            "stroke-linejoin", "stroke-miterlimit", "fill-rule", "clip-rule", "opacity", "fill-opacity", "stroke-opacity",
            "stroke-dasharray", "stroke-dashoffset", "transform", "id", "class", "clip-path", "mask", "href", "xlink:href",
            "offset", "stop-color", "stop-opacity", "gradientunits", "gradienttransform", "spreadmethod",
            "clippathunits", "maskunits", "maskcontentunits", "preserveaspectratio", "version", "role",
            "focusable", "aria-hidden", "aria-label", "font-size", "font-family", "font-weight", "text-anchor", "dx", "dy"));

    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) return true;
        String t = value.trim();
        if (t.length() > 200000) return false;
        if (URL_PATTERN.matcher(t).matches()) return true;
        if (LOCAL_PATH_PATTERN.matcher(t).matches()) return true;
        if (!SVG_PATTERN.matcher(t).matches()) return false;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            Element root = builder.parse(new InputSource(new StringReader(t))).getDocumentElement();
            return "svg".equals(root.getTagName()) && validElement(root, 0);
        } catch (Exception ex) { return false; }
    }

    private static boolean validElement(Element element, int depth) {
        if (depth > 64) return false;
        String namespace = element.getNamespaceURI();
        if (namespace != null && !"http://www.w3.org/2000/svg".equals(namespace)) return false;
        if (!TAGS.contains(element.getTagName().toLowerCase(Locale.ROOT))) return false;
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String name = attr.getNodeName().toLowerCase(Locale.ROOT);
            String val = attr.getNodeValue().trim();
            if (!ATTRS.contains(name)) return false;
            if ("xmlns".equals(name)) { if (!"http://www.w3.org/2000/svg".equals(val)) return false; continue; }
            if ("xmlns:xlink".equals(name)) { if (!"http://www.w3.org/1999/xlink".equals(val)) return false; continue; }
            if (("href".equals(name) || "xlink:href".equals(name)) && !val.matches("#[A-Za-z_][A-Za-z0-9_.:-]*")) return false;
            String lower = val.toLowerCase(Locale.ROOT);
            if (lower.contains("javascript:") || lower.contains("data:") || lower.contains("expression(")) return false;
            if (lower.contains("url(") && !val.matches("url\\(#[A-Za-z_][A-Za-z0-9_.:-]*\\)")) return false;
        }
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && !validElement((Element) child, depth + 1)) return false;
            if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE || child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) return false;
        }
        return true;
    }

    public static boolean isImageUrl(String value) {
        return value != null && value.trim().startsWith("http");
    }

    public static boolean isLocalPath(String value) {
        return value != null && LOCAL_PATH_PATTERN.matcher(value.trim()).matches();
    }

}
