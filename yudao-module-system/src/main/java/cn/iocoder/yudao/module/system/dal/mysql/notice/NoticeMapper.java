package cn.iocoder.yudao.module.system.dal.mysql.notice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.time.LocalDateTime;

@Mapper
public interface NoticeMapper extends BaseMapperX<NoticeDO> {

    default List<NoticeDO> selectRecentPushed(Long userId) {
        return selectList(new LambdaQueryWrapperX<NoticeDO>()
                .eq(NoticeDO::getStatus, 0)
                .isNotNull(NoticeDO::getPushTime)
                .and(wrapper -> wrapper.isNull(NoticeDO::getTargetUserId)
                        .or().eq(NoticeDO::getTargetUserId, userId))
                .orderByDesc(NoticeDO::getPushTime)
                .last("LIMIT 30"));
    }

    default List<NoticeDO> selectActiveUpgrades(Long userId) {
        return selectActiveUpgrades(userId, false);
    }

    default List<NoticeDO> selectActiveUpgrades(Long userId, boolean includeScheduled) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapperX<NoticeDO> query = new LambdaQueryWrapperX<>();
        query.eq(NoticeDO::getStatus, 0)
                .eq(NoticeDO::getType, 3)
                .isNotNull(NoticeDO::getPushTime)
                .and(wrapper -> wrapper.gt(NoticeDO::getDisplayEndTime, now)
                        .or().isNull(NoticeDO::getDisplayEndTime)
                            .gt(NoticeDO::getPushTime, now.minusHours(24)))
                .and(wrapper -> wrapper.isNull(NoticeDO::getTargetUserId)
                        .or().eq(NoticeDO::getTargetUserId, userId));
        if (!includeScheduled) {
            query.and(wrapper -> wrapper.isNull(NoticeDO::getDisplayStartTime)
                    .or().le(NoticeDO::getDisplayStartTime, now));
        }
        return selectList(query.orderByDesc(NoticeDO::getPushTime).last("LIMIT 30"));
    }

    default PageResult<NoticeDO> selectPage(NoticePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NoticeDO>()
                .likeIfPresent(NoticeDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NoticeDO::getStatus, reqVO.getStatus())
                .orderByDesc(NoticeDO::getId));
    }

}
