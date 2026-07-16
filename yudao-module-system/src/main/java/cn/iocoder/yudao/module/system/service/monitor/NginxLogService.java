package cn.iocoder.yudao.module.system.service.monitor;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpBlacklistMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 读取服务端 nginx 日志：查看 access/error 原文（尾部），并基于 access 日志做 IP/接口/成功失败统计。
 *
 * 只读【配置指定】的两个日志文件，无路径可注入；nginx 日志可能很大，仅读取文件尾部 scan-bytes 字节，
 * 保证与文件大小无关的稳定开销。统计结果做 30 秒内存缓存，避免前端轮询反复扫描。
 */
@Service
@Slf4j
public class NginxLogService {

    // combined 格式：ip - - [time] "method path proto" status size "ref" "ua"
    private static final Pattern ACCESS = Pattern.compile(
            "^(\\S+) \\S+ \\S+ \\[([^\\]]+)\\] \"(\\S+) ([^ ?\"]+)[^\"]*\" (\\d{3}) ");

    @Value("${yudao.nginx-log.enabled:true}")
    private boolean enabled;
    @Value("${yudao.nginx-log.access-path:/var/log/nginx/access.log}")
    private String accessPath;
    @Value("${yudao.nginx-log.error-path:/var/log/nginx/error.log}")
    private String errorPath;
    @Value("${yudao.nginx-log.scan-bytes:10485760}") // 10MB
    private long scanBytes;
    @Value("${yudao.nginx-log.tail-lines:500}")
    private int defaultTailLines;
    @Value("${yudao.nginx-log.top-n:100}")
    private int topN;

    @Resource
    private IpBlacklistMapper ipBlacklistMapper;

    private final Map<String, long[]> cacheTs = new HashMap<>();
    private final Map<String, Map<String, Object>> cache = new HashMap<>();
    private static final long CACHE_TTL_MS = 30_000L;

    public Map<String, Object> config() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", enabled);
        m.put("accessPath", accessPath);
        m.put("errorPath", errorPath);
        m.put("accessExists", Files.exists(Paths.get(accessPath)));
        m.put("errorExists", Files.exists(Paths.get(errorPath)));
        m.put("scanBytes", scanBytes);
        return m;
    }

    /** 查看日志尾部原文；type=access|error，可按 ip 过滤 */
    public List<String> tail(String type, int lines, String ip) {
        checkEnabled();
        Path path = Paths.get("error".equalsIgnoreCase(type) ? errorPath : accessPath);
        List<String> all = readTail(path, scanBytes);
        if (StrUtil.isNotBlank(ip)) {
            all.removeIf(l -> !l.contains(ip));
        }
        int n = lines > 0 ? lines : defaultTailLines;
        return all.size() > n ? all.subList(all.size() - n, all.size()) : all;
    }

    /** 基于 access 日志统计：IP、接口、成功/失败、状态码；可按 ip 过滤（对所有统计生效） */
    public Map<String, Object> stats(String ip) {
        checkEnabled();
        String key = StrUtil.blankToDefault(ip, "");
        long now = System.currentTimeMillis();
        long[] ts = cacheTs.get(key);
        if (ts != null && now - ts[0] < CACHE_TTL_MS && cache.containsKey(key)) {
            return cache.get(key);
        }
        Map<String, Object> result = doStats(ip);
        cache.put(key, result);
        cacheTs.put(key, new long[]{now});
        return result;
    }

    private Map<String, Object> doStats(String ipFilter) {
        List<String> lines = readTail(Paths.get(accessPath), scanBytes);
        Map<String, long[]> ipCount = new HashMap<>();   // ip -> [total, fail]
        Map<String, Long> pathCount = new HashMap<>();
        Map<String, Long> codeCount = new HashMap<>();
        long total = 0, success = 0, fail = 0, unparsed = 0;
        boolean filter = StrUtil.isNotBlank(ipFilter);

        for (String line : lines) {
            Matcher m = ACCESS.matcher(line);
            if (!m.find()) {
                unparsed++;
                continue;
            }
            String ip = m.group(1);
            if (filter && !ip.equals(ipFilter)) {
                continue;
            }
            String path = m.group(4);
            int status = Integer.parseInt(m.group(5));
            boolean ok = status < 400;
            total++;
            if (ok) success++; else fail++;
            long[] ic = ipCount.computeIfAbsent(ip, k -> new long[2]);
            ic[0]++;
            if (!ok) ic[1]++;
            pathCount.merge(path, 1L, Long::sum);
            codeCount.merge(String.valueOf(status), 1L, Long::sum);
        }

        // 接入黑名单：曾封禁/现封禁的 IP 标红
        Set<String> everBanned = new HashSet<>(ipBlacklistMapper.selectEverBannedIps());
        Set<String> activeBanned = new HashSet<>(ipBlacklistMapper.selectAllActiveIps(LocalDateTime.now()));

        List<Map<String, Object>> ipStats = new ArrayList<>();
        ipCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(topN)
                .forEach(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("ip", e.getKey());
                    row.put("count", e.getValue()[0]);
                    row.put("fail", e.getValue()[1]);
                    row.put("banned", activeBanned.contains(e.getKey()));
                    row.put("everBanned", everBanned.contains(e.getKey()));
                    ipStats.add(row);
                });

        List<Map<String, Object>> pathStats = topEntries(pathCount, "path");
        List<Map<String, Object>> codeStats = topEntries(codeCount, "code");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("fail", fail);
        result.put("unparsed", unparsed);
        result.put("scannedLines", lines.size());
        result.put("ipStats", ipStats);
        result.put("pathStats", pathStats);
        result.put("codeStats", codeStats);
        return result;
    }

    private List<Map<String, Object>> topEntries(Map<String, Long> map, String keyName) {
        List<Map<String, Object>> out = new ArrayList<>();
        map.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .forEach(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put(keyName, e.getKey());
                    row.put("count", e.getValue());
                    out.add(row);
                });
        return out;
    }

    /** 只读文件尾部 maxBytes 字节，避免读整文件；RandomAccessFile 按字节读后按 UTF-8 重解码 */
    private List<String> readTail(Path path, long maxBytes) {
        if (!Files.exists(path)) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(400,
                    "nginx 日志文件不存在：" + path + "。请确认 nginx 日志已挂载进应用容器，并配置 yudao.nginx-log.access-path/error-path");
        }
        List<String> lines = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            long len = raf.length();
            long start = Math.max(0, len - maxBytes);
            raf.seek(start);
            if (start > 0) {
                raf.readLine(); // 丢弃被截断的首行
            }
            String line;
            while ((line = raf.readLine()) != null) {
                lines.add(new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 nginx 日志失败: " + e.getMessage(), e);
        }
        return lines;
    }

    private void checkEnabled() {
        if (!enabled) {
            throw new IllegalStateException("nginx 日志功能未启用（yudao.nginx-log.enabled=false）");
        }
    }
}
