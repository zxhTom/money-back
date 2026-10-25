package cn.iocoder.yudao.module.custom.service.face.evidence;

import cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO;

public interface FaceEvidenceService {

    // 保存单项存证材料
    FaceEvidenceDO saveEvidence(Long tenantId, Long userId, String verifyToken, String mediaType, String base64Data);

    // 鉴权下载存证二进制
    FaceEvidenceDO getAuthorizedEvidence(Long evidenceId);
}
