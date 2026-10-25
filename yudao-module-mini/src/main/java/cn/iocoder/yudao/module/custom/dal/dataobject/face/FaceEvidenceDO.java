package cn.iocoder.yudao.module.custom.dal.dataobject.face;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// 人脸核身私有存证 DO
@TableName("custom_face_evidence")
@KeySequence("custom_face_evidence_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"content"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceEvidenceDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long userId;
    private String sessionHash;
    private String mediaType;
    private String path;
    private byte[] content;
    private Integer size;
    private String sha256;
}
