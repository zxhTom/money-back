package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件操作 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FileMapper extends BaseMapperX<FileDO> {

    default PageResult<FileDO> selectPage(FilePageReqVO reqVO) {
        return selectScopedPage(reqVO, null, null);
    }

    default PageResult<FileDO> selectScopedPage(FilePageReqVO reqVO, Long uploaderId, Long tenantId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileDO>()
                .eqIfPresent(FileDO::getCreator, uploaderId == null ? null : String.valueOf(uploaderId))
                .likeIfPresent(FileDO::getPath, reqVO.getPath())
                .likeIfPresent(FileDO::getType, reqVO.getType())
                .betweenIfPresent(FileDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileDO::getId)
                .apply(tenantId != null, "creator IN (SELECT CAST(id AS CHAR) FROM system_users WHERE tenant_id = {0} AND deleted = 0)", tenantId));
    }

    default FileDO selectByTraceId(String traceId) {
        return selectOne(FileDO::getAuditTraceId, traceId);
    }

    /** 删除早于指定时间的临时文件记录（仅 demo/ 目录，与 MinIO 生命周期策略对齐），单次最多 limit 条 */
    default int deleteByCreateTimeBefore(java.time.LocalDateTime time, int limit) {
        return delete(new LambdaQueryWrapperX<FileDO>()
                .lt(FileDO::getCreateTime, time)
                .likeRight(FileDO::getPath, "demo/")
                .last("LIMIT " + limit));
    }

}
