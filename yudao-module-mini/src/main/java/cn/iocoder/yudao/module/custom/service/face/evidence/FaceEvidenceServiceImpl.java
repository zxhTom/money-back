package cn.iocoder.yudao.module.custom.service.face.evidence;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO;
import cn.iocoder.yudao.module.custom.dal.mysql.face.FaceEvidenceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.Objects;

@Service
@Slf4j
public class FaceEvidenceServiceImpl implements FaceEvidenceService {

    public static final String MEDIA_TYPE_VIDEO = "video";
    public static final String MEDIA_TYPE_BEST_IMAGE = "best_image";

    // 视频最大 8 MiB (8 * 1024 * 1024 = 8388608 bytes), Base64 编码上限约为 8388608 * 4 / 3 ≈ 11184811
    public static final int MAX_VIDEO_BYTES = 8 * 1024 * 1024;
    // 图片最大 2 MiB (2 * 1024 * 1024 = 2097152 bytes), Base64 编码上限约为 2097152 * 4 / 3 ≈ 2796203
    public static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;

    @Resource
    private FaceEvidenceMapper faceEvidenceMapper;

    @Override
    public FaceEvidenceDO saveEvidence(Long tenantId, Long userId, String verifyToken, String mediaType, String base64Data) {
        if (tenantId == null || userId == null || StrUtil.isBlank(verifyToken) || StrUtil.isBlank(mediaType) || StrUtil.isBlank(base64Data)) {
            return null;
        }

        int maxRawBytes;
        String ext;
        if (MEDIA_TYPE_VIDEO.equalsIgnoreCase(mediaType)) {
            maxRawBytes = MAX_VIDEO_BYTES;
            ext = ".mp4";
        } else if (MEDIA_TYPE_BEST_IMAGE.equalsIgnoreCase(mediaType)) {
            maxRawBytes = MAX_IMAGE_BYTES;
            ext = ".jpg";
        } else {
            log.warn("[FaceEvidence] 未知媒体类型，拒绝保存: {}", mediaType);
            return null;
        }

        // 1. 解码前估算 Base64 长度上限（base64 长度大致为 raw * 4 / 3，留 10% 冗余给换行或空白）
        long maxEncodedLen = ((long) maxRawBytes * 4 / 3) + 4096;
        if (base64Data.length() > maxEncodedLen) {
            log.warn("[FaceEvidence] 存证材料编码长度超过上限: mediaType={}, len={}", mediaType, base64Data.length());
            return null;
        }

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(base64Data.trim());
        } catch (IllegalArgumentException e) {
            log.warn("[FaceEvidence] 存证材料 Base64 解析失败: mediaType={}", mediaType);
            return null;
        }

        // 2. 解码后严格检查字节大小
        if (decodedBytes == null || decodedBytes.length == 0 || decodedBytes.length > maxRawBytes) {
            log.warn("[FaceEvidence] 存证材料解码大小非法或超限: mediaType={}, size={}", mediaType, decodedBytes == null ? 0 : decodedBytes.length);
            return null;
        }

        String sessionHash = DigestUtil.sha256Hex(verifyToken);
        String sha256 = DigestUtil.sha256Hex(decodedBytes);

        return TenantUtils.execute(tenantId, () -> {
            // 3. 幂等查已有存证使用元数据投影，避免读取大 BLOB
            FaceEvidenceDO existing = faceEvidenceMapper.selectMetadataByTenantSessionAndMedia(tenantId, sessionHash, mediaType);
            if (existing != null) {
                return existing;
            }

            String path = "private/face-evidence/" + IdUtil.fastSimpleUUID() + ext;
            FaceEvidenceDO evidence = FaceEvidenceDO.builder()
                    .userId(userId)
                    .sessionHash(sessionHash)
                    .mediaType(mediaType)
                    .path(path)
                    .content(decodedBytes)
                    .size(decodedBytes.length)
                    .sha256(sha256)
                    .build();
            evidence.setTenantId(tenantId);

            try {
                faceEvidenceMapper.insert(evidence);
                return evidence;
            } catch (DuplicateKeyException dup) {
                // 并发重复插入时，优雅返回已存在的元数据记录
                return faceEvidenceMapper.selectMetadataByTenantSessionAndMedia(tenantId, sessionHash, mediaType);
            } catch (Exception ex) {
                log.error("[FaceEvidence] 保存私有存证异常: mediaType={}", mediaType);
                return null;
            }
        });
    }

    @Override
    public FaceEvidenceDO getAuthorizedEvidence(Long evidenceId) {
        if (evidenceId == null) {
            throw new AccessDeniedException("存证不存在");
        }

        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            throw new AccessDeniedException("请先登录后访问");
        }

        Long currentTenantId = TenantContextHolder.getTenantId();
        if (currentTenantId == null) {
            throw new AccessDeniedException("缺少有效租户身份");
        }

        // 核心安全原则：拒绝时不读取大 BLOB，先查元数据
        FaceEvidenceDO meta = faceEvidenceMapper.selectMetadataById(evidenceId);
        if (meta == null) {
            throw new AccessDeniedException("存证不存在");
        }

        // 强授权检查：同租户 + 本人。不凭 controller 单层校验，不给 super_admin 隐式旁路
        if (!Objects.equals(meta.getTenantId(), currentTenantId) || !Objects.equals(meta.getUserId(), loginUserId)) {
            log.warn("[FaceEvidence] 跨人或跨租户访问被拦截: actorUser={}, actorTenant={}, ownerUser={}, ownerTenant={}",
                    loginUserId, currentTenantId, meta.getUserId(), meta.getTenantId());
            throw new AccessDeniedException("无权查看该存证");
        }

        // 授权通过，读取全量 DO（含 content BLOB）
        FaceEvidenceDO full = faceEvidenceMapper.selectById(evidenceId);
        if (full == null || full.getContent() == null) {
            throw new AccessDeniedException("存证不存在");
        }
        return full;
    }
}
