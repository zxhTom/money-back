package cn.iocoder.yudao.module.infra.service.file.quarantine;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.quarantine.FileQuarantineDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.quarantine.FileQuarantineMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import cn.iocoder.yudao.module.infra.service.file.FileConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileQuarantineServiceImpl implements FileQuarantineService {

    @Resource
    private FileQuarantineMapper fileQuarantineMapper;

    @Resource
    @Lazy
    private FileConfigService fileConfigService;

    @Override
    public String canonicalizePath(String rawPath) {
        if (rawPath == null) {
            return null;
        }
        if (rawPath.isEmpty()) {
            return "";
        }
        String path = rawPath;

        // 如果是完整 URL，提取路径部分并去除首尾空白
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path = path.trim();
            try {
                URI uri = new URI(path.replace(" ", "%20"));
                path = uri.getPath();
            } catch (Exception e) {
                int slashIdx = path.indexOf('/', 8);
                if (slashIdx >= 0) {
                    path = path.substring(slashIdx);
                }
            }
        }

        // 统一反斜杠
        path = path.replace('\\', '/');

        int queryIdx = path.indexOf('?');
        if (queryIdx >= 0) {
            path = path.substring(0, queryIdx);
        }

        // 严格精确匹配应用代理前缀 /admin-api/infra/file/{configId}/get/
        if (path.startsWith("/admin-api/infra/file/")) {
            int getIdx = path.indexOf("/get/");
            if (getIdx > 0) {
                path = path.substring(getIdx + 5);
            }
        }

        // 仅在明确是 URL 或者是编码绕过（包含 %XX 格式）时尝试受控解码（最多 5 层）
        boolean isHttpUrl = rawPath.startsWith("http://") || rawPath.startsWith("https://");
        boolean hasValidEncoding = path.matches(".*%[0-9a-fA-F]{2}.*");
        if (isHttpUrl || hasValidEncoding) {
            for (int i = 0; i < 5; i++) {
                if (!path.matches(".*%[0-9a-fA-F]{2}.*")) {
                    break;
                }
                try {
                    String decoded = URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8.name());
                    if (decoded.equals(path)) {
                        break;
                    }
                    path = decoded;
                } catch (Exception e) {
                    break;
                }
            }
            // 检查是否有残留的未决编码绕过（如 %2f, %2e, %5c 等）
            if (path.matches("(?i).*%(2f|5c|2e).*")) {
                throw new IllegalArgumentException("路径包含未决编码");
            }
        }

        // 处理连续多斜杠
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }

        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        // 严格拒绝任何包含 . 或 .. 的相对路径片段，不消除后交存储
        String[] parts = path.split("/");
        List<String> segments = new ArrayList<>();
        for (String part : parts) {
            if (".".equals(part) || "..".equals(part)) {
                throw new IllegalArgumentException("禁止包含相对路径段");
            }
            if (!part.isEmpty()) {
                segments.add(part);
            }
        }

        String result = String.join("/", segments);
        // 保留尾部空格（若原 path 末尾以空格结尾且未被截断）
        if (path.endsWith(" ") && !result.endsWith(" ")) {
            result = result + " ";
        }
        return result;
    }

    @Override
    public boolean isPrivateFaceEvidence(String rawPathOrUrl) {
        if (rawPathOrUrl == null) {
            return false;
        }
        try {
            String canonical = canonicalizePath(rawPathOrUrl);
            return canonical != null && canonical.startsWith("private/face-evidence");
        } catch (IllegalArgumentException e) {
            // 包含相对路径段或非法编码等畸形格式，拒绝放行
            return true;
        }
    }

    @Override
    public boolean isQuarantined(Long configId, String rawPathOrUrl) {
        if (configId == null || rawPathOrUrl == null) {
            return false;
        }
        // 若为完整 URL，先转换为实际执行 key；若为 storageKey 则保持原字面量（包含 %25, ?, 尾空格等）
        String storageKey = extractStorageKey(configId, rawPathOrUrl);
        if (storageKey == null || storageKey.isEmpty()) {
            return false;
        }
        try {
            // 检查原始 key / URL
            if (fileQuarantineMapper.existsByConfigAndCanonicalPath(configId, rawPathOrUrl)) {
                return true;
            }
            // 检查提取出的 storageKey
            if (storageKey != null && !storageKey.equals(rawPathOrUrl) && fileQuarantineMapper.existsByConfigAndCanonicalPath(configId, storageKey)) {
                return true;
            }
            // 针对普通路径的连续多斜杠/首斜杠情况，检查规范化后的执行 key
            String norm = rawPathOrUrl.replace('\\', '/');
            while (norm.contains("//")) norm = norm.replace("//", "/");
            while (norm.startsWith("/")) norm = norm.substring(1);
            if (!norm.equals(rawPathOrUrl) && !norm.equals(storageKey) && fileQuarantineMapper.existsByConfigAndCanonicalPath(configId, norm)) {
                return true;
            }
            return false;
        } catch (org.springframework.dao.DataAccessException ex) {
            log.error("[FileQuarantine] 访问隔离清单异常: configId={}, error={}", configId, ex.getClass().getSimpleName());
            throw new AccessDeniedException("存储隔离服务不可用，拒绝访问历史对象");
        }
    }

    @Override
    public Collection<String> findQuarantinedPaths(Long configId, Collection<String> rawPaths) {
        if (configId == null || rawPaths == null || rawPaths.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> queryKeys = new HashSet<>();
        for (String raw : rawPaths) {
            if (raw != null) {
                queryKeys.add(raw);
                String storageKey = extractStorageKey(configId, raw);
                if (storageKey != null) {
                    queryKeys.add(storageKey);
                }
            }
        }
        if (queryKeys.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<FileQuarantineDO> list = fileQuarantineMapper.selectListByConfigAndPaths(configId, queryKeys);
            return list.stream().map(d -> new String(d.getCanonicalPath(), StandardCharsets.UTF_8)).collect(Collectors.toSet());
        } catch (org.springframework.dao.DataAccessException ex) {
            log.error("[FileQuarantine] 批量查询隔离清单异常: configId={}, error={}", configId, ex.getClass().getSimpleName());
            throw new AccessDeniedException("存储隔离服务不可用，拒绝访问历史对象");
        }
    }

    @Override
    public String extractStorageKey(Long configId, String urlOrPath) {
        if (urlOrPath == null) return null;
        String raw = urlOrPath;
        if (configId != null && fileConfigService != null) {
            cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient client = fileConfigService.getFileClient(configId);
            if (client != null) {
                raw = client.extractStorageKey(raw);
            } else {
                FileConfigDO fileConfig = fileConfigService.getFileConfig(configId);
                if (fileConfig != null && fileConfig.getConfig() instanceof S3FileClientConfig) {
                    S3FileClientConfig s3Config = (S3FileClientConfig) fileConfig.getConfig();
                    String domain = s3Config.getDomain();
                    if (StrUtil.isEmpty(domain)) {
                        String endpoint = s3Config.getEndpoint();
                        if (cn.hutool.http.HttpUtil.isHttp(endpoint) || cn.hutool.http.HttpUtil.isHttps(endpoint)) {
                            domain = StrUtil.format("{}/{}", endpoint, s3Config.getBucket());
                        } else if (StrUtil.isNotBlank(endpoint)) {
                            domain = StrUtil.format("https://{}.{}", s3Config.getBucket(), endpoint);
                        }
                    }
                    if (StrUtil.isNotBlank(domain)) {
                        String prefix = domain.endsWith("/") ? domain : domain + "/";
                        if (raw.startsWith(prefix)) {
                            raw = raw.substring(prefix.length());
                            raw = cn.iocoder.yudao.framework.common.util.http.HttpUtils.removeUrlQuery(raw);
                        }
                    }
                }
            }
        }
        return raw;
    }
}
