package cn.iocoder.yudao.module.infra.dal.mysql.file.quarantine;

import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.quarantine.FileQuarantineDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper
public interface FileQuarantineMapper extends BaseMapper<FileQuarantineDO> {

    default boolean existsByConfigAndCanonicalPath(Long configId, String canonicalPath) {
        if (configId == null || canonicalPath == null) {
            return false;
        }
        byte[] rawBytes = canonicalPath.getBytes(StandardCharsets.UTF_8);
        String pathHash = DigestUtil.sha256Hex(rawBytes);
        List<FileQuarantineDO> list = selectList(new LambdaQueryWrapper<FileQuarantineDO>()
                .eq(FileQuarantineDO::getConfigId, configId)
                .eq(FileQuarantineDO::getPathHash, pathHash));
        for (FileQuarantineDO item : list) {
            if (Arrays.equals(item.getCanonicalPath(), rawBytes)) {
                return true;
            }
        }
        return false;
    }

    default List<FileQuarantineDO> selectListByConfigAndPaths(Long configId, Collection<String> canonicalPaths) {
        if (configId == null || canonicalPaths == null || canonicalPaths.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> hashes = canonicalPaths.stream()
                .filter(Objects::nonNull)
                .map(p -> DigestUtil.sha256Hex(p.getBytes(StandardCharsets.UTF_8)))
                .collect(Collectors.toList());
        if (hashes.isEmpty()) {
            return Collections.emptyList();
        }
        List<FileQuarantineDO> candidates = selectList(new LambdaQueryWrapper<FileQuarantineDO>()
                .eq(FileQuarantineDO::getConfigId, configId)
                .in(FileQuarantineDO::getPathHash, hashes));
        return candidates.stream().filter(item -> {
            for (String p : canonicalPaths) {
                if (Arrays.equals(item.getCanonicalPath(), p.getBytes(StandardCharsets.UTF_8))) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }
}
