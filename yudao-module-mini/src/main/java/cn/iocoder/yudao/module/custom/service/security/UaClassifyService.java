package cn.iocoder.yudao.module.custom.service.security;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.UaWhitelistDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.UaWhitelistMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * User-Agent 分类：区分"明确的自动化工具/攻击"、"合法浏览器"、"其它可疑"。
 *
 * 硬名单（明确是脚本/工具/注入探测，绝对不是真人浏览器）来自登录日志实测：
 *   python-requests / Python-urllib / python-httpx / curl / wget / Apifox，
 *   以及 ${...} 模板注入（Log4Shell）、<script> XSS 探测等 —— 命中即立即处置。
 * 白名单不再是"包含 Mozilla 就放行"（那样 `Mozilla/5.0 (随便写)` 也能过）：
 *   要求 UA 先在【结构】上像真实浏览器/内置 webview（Webkit 或 Firefox 形态），
 *   再包含 custom_ua_whitelist 里维护的一个具体客户端 token（已去掉万能词 Mozilla）。
 *   注意：UA 完全由客户端控制，白名单只是"抬高门槛的软信号"，真正防线靠认证/限速/风控。
 */
@Service
@Slf4j
public class UaClassifyService {

    /** 明确的自动化/攻击 UA 特征（小写匹配）——命中即判定模拟请求 */
    private static final List<String> HARD_BOT_KEYWORDS = Arrays.asList(
            "python-requests", "python-urllib", "python-httpx", "aiohttp",
            "curl/", "wget", "libwww-perl", "go-http-client", "okhttp",
            "java/", "apache-httpclient", "httpclient", "scrapy", "httpie",
            "apifox", "postmanruntime", "insomnia", "axios/", "node-fetch",
            "winhttp", "restsharp", "guzzlehttp",
            // 注入/攻击探测：${...} 一律视为模板注入(Log4Shell 及其混淆变体)，真实浏览器 UA 绝不含 ${
            "${", "<script", "</script", "jndi:"
    );

    /** 缓存 5 分钟 */
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    /** 真实浏览器/内置 webview 的结构形态（小写匹配）：Webkit 系 或 Firefox 系。
     *  只要求 Mozilla/5.0(平台) + AppleWebKit/版本（不强求 KHTML，兼容微信 PC 小程序等变体），
     *  已足以挡掉 `Mozilla/5.0 (随便写)` / `Mozilla/5.0 (X) Chrome/120` 这类无 AppleWebKit 的伪造。 */
    private static final Pattern WEBKIT_UA = Pattern.compile(
            "^mozilla/5\\.0 \\(.*applewebkit/[0-9.]+");
    private static final Pattern GECKO_UA = Pattern.compile(
            "^mozilla/5\\.0 \\(.*gecko/[0-9].*firefox/[0-9.]+");

    @Resource
    private UaWhitelistMapper uaWhitelistMapper;

    private volatile List<String> cachedKeywords = null;
    private volatile long cachedAt = 0L;

    /**
     * 是否明确的自动化工具/攻击 UA（空 UA 也算——真人浏览器不会不带 UA）。
     */
    public boolean isHardBot(String ua) {
        if (StrUtil.isBlank(ua)) {
            return true;
        }
        String low = ua.toLowerCase();
        for (String kw : HARD_BOT_KEYWORDS) {
            if (low.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否命中浏览器白名单：先要求 UA 结构像真实浏览器/内置 webview，再要求包含一个具体客户端 token。
     * 只"包含 Mozilla/Chrome 关键词"不再放行——防止 `Mozilla/5.0 (随便写)` 这类伪造 UA 蒙混。
     */
    public boolean isWhitelistedBrowser(String ua) {
        if (StrUtil.isBlank(ua)) {
            return false;
        }
        String low = ua.toLowerCase();
        // 1. 结构校验：不像真实浏览器/webview 形态的，直接不算白名单
        if (!WEBKIT_UA.matcher(low).find() && !GECKO_UA.matcher(low).find()) {
            return false;
        }
        // 2. 结构 OK 之外，仍需带一个已知客户端 token（表里维护，已去掉万能词 Mozilla）
        for (String kw : getKeywords()) {
            if (StrUtil.isNotBlank(kw) && low.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private List<String> getKeywords() {
        long now = System.currentTimeMillis();
        List<String> local = cachedKeywords;
        if (local != null && now - cachedAt < CACHE_TTL_MS) {
            return local;
        }
        try {
            local = uaWhitelistMapper.selectEnabled().stream()
                    .map(UaWhitelistDO::getKeyword).filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[UaClassify] 读取 UA 白名单失败，暂用兜底关键词：{}", e.getMessage());
            // 兜底 token 列表：不含万能词 Mozilla；配合结构校验使用
            local = Arrays.asList("MicroMessenger", "Chrome", "CriOS", "Safari", "Edg", "EdgA",
                    "Firefox", "FxiOS", "Opera", "OPR", "MQQBrowser", "QQBrowser", "UCBrowser",
                    "Quark", "HuaweiBrowser", "HarmonyOS", "VivoBrowser", "MiuiBrowser",
                    "HeyTapBrowser", "OppoBrowser", "baiduboxapp", "SogouMobileBrowser",
                    "MetaSr", "SamsungBrowser");
        }
        cachedKeywords = local;
        cachedAt = now;
        return local;
    }

}
