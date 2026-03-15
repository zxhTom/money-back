package cn.iocoder.yudao.module.custom.service.timewindow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.timewindow.TimeWindowDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 时间窗口 Service 接口
 */
public interface TimeWindowService {

    Long createTimeWindow(@Valid TimeWindowCreateReqVO createReqVO);

    void updateTimeWindow(@Valid TimeWindowUpdateReqVO updateReqVO);

    void deleteTimeWindow(Long id);

    /**
     * 批量删除时间窗口
     *
     * @param ids 编号列表
     */
    void deleteTimeWindowList(List<Long> ids);

    TimeWindowDO getTimeWindow(Long id);

    PageResult<TimeWindowDO> getTimeWindowPage(TimeWindowPageReqVO pageReqVO);

    /**
     * 重合校验：与所有已激活的时间窗口计算最大重合时长，与阈值比较
     */
    TimeWindowCheckOverlapRespVO checkOverlap(@Valid TimeWindowCheckOverlapReqVO reqVO);

    /**
     * 获取重合阈值配置（小时）
     */
    TimeWindowConfigRespVO getConfig();

    /**
     * 获取当前重合阈值（小时），用于 create/update 时校验
     */
    double getOverlapThresholdHours();

    /**
     * 查询当前时间命中且将指定用户判定为「选中」的已激活时间窗口。
     * 规则：在窗口有效期内（startTime &lt;= now &lt;= endTime）且 status=1；
     * 用户需在 selectedUserIds 中且不在 excludedUserIds 中；
     * 若用户同时在选中与排除列表中，视为排除（未选中）。
     *
     * @param userId 用户 ID，若为 null 则使用当前登录用户
     * @return 命中的时间窗口（取第一条），未命中返回 null
     */
    TimeWindowDO getActiveWindowSelectingUserNow(Long userId);
}
