package cn.iocoder.yudao.module.infra.service.file.quarantine;

import java.util.Collection;

public interface FileQuarantineService {

    String canonicalizePath(String rawPath);

    boolean isPrivateFaceEvidence(String rawPathOrUrl);

    boolean isQuarantined(Long configId, String rawPathOrUrl);

    Collection<String> findQuarantinedPaths(Long configId, Collection<String> rawPaths);

    String extractStorageKey(Long configId, String urlOrPath);
}
