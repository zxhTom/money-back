package cn.iocoder.yudao.module.custom.dal.mysql.timewindow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo.TimeWindowPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.timewindow.TimeWindowDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 时间窗口 Mapper
 */
@Mapper
public interface TimeWindowMapper extends BaseMapperX<TimeWindowDO> {

    default PageResult<TimeWindowDO> selectPage(TimeWindowPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TimeWindowDO>()
                .betweenIfPresent(TimeWindowDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(TimeWindowDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(TimeWindowDO::getStatus, reqVO.getStatus())
                .orderByDesc(TimeWindowDO::getId));
    }

    /**
     * 查询所有激活状态的时间窗口（用于重合校验）
     */
    default java.util.List<TimeWindowDO> selectListActive() {
        return selectList(new LambdaQueryWrapperX<TimeWindowDO>()
                .eq(TimeWindowDO::getStatus, 1));
    }

    /**
     * 查询在指定时间处于有效期内且已激活的时间窗口（startTime <= time <= endTime, status=1）
     */
    default java.util.List<TimeWindowDO> selectListActiveAtTime(java.time.LocalDateTime time) {
        return selectList(new LambdaQueryWrapperX<TimeWindowDO>()
                .eq(TimeWindowDO::getStatus, 1)
                .le(TimeWindowDO::getStartTime, time)
                .ge(TimeWindowDO::getEndTime, time));
    }
}
