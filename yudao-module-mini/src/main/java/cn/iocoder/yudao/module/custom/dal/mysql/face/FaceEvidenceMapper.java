package cn.iocoder.yudao.module.custom.dal.mysql.face;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FaceEvidenceMapper extends BaseMapperX<FaceEvidenceDO> {

    @Select("SELECT id, tenant_id, user_id, session_hash, media_type, path, size, sha256, create_time " +
            "FROM custom_face_evidence WHERE tenant_id = #{tenantId} AND session_hash = #{sessionHash} " +
            "AND media_type = #{mediaType} AND deleted = 0 LIMIT 1")
    FaceEvidenceDO selectMetadataByTenantSessionAndMedia(@Param("tenantId") Long tenantId,
                                                         @Param("sessionHash") String sessionHash,
                                                         @Param("mediaType") String mediaType);

    // 不读取 content BLOB 的元数据投影查询
    @Select("SELECT id, tenant_id, user_id, session_hash, media_type, path, size, sha256, create_time " +
            "FROM custom_face_evidence WHERE id = #{id} AND deleted = 0")
    FaceEvidenceDO selectMetadataById(@Param("id") Long id);
}
