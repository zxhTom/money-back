package cn.iocoder.yudao.module.custom.dal.mysql.milestone;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.milestone.vo.MilestonePageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.milestone.MilestoneDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MilestoneMapper extends BaseMapperX<MilestoneDO> {

    default PageResult<MilestoneDO> selectPage(MilestonePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MilestoneDO>()
                .likeIfPresent(MilestoneDO::getTitle, reqVO.getTitle())
                .eqIfPresent(MilestoneDO::getCategory, reqVO.getCategory())
                .eqIfPresent(MilestoneDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MilestoneDO::getPriority, reqVO.getPriority())
                .betweenIfPresent(MilestoneDO::getTargetDate, reqVO.getTargetDate())
                .orderByAsc(MilestoneDO::getTargetDate));
    }
}
