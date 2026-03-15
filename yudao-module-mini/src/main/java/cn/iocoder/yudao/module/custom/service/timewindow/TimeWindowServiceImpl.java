package cn.iocoder.yudao.module.custom.service.timewindow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.timewindow.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.timewindow.TimeWindowDO;
import cn.iocoder.yudao.module.custom.dal.mysql.timewindow.TimeWindowMapper;
import cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 时间窗口 Service 实现类
 */
@Service
@Validated
@Slf4j
public class TimeWindowServiceImpl implements TimeWindowService {

    private static final int DEFAULT_STATUS_ACTIVE = 1;

    private static final String TARGET_TYPE_ROLE = "role";

    @Resource
    private TimeWindowMapper timeWindowMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private PermissionCommonApi permissionApi;

    /**
     * 允许的最大重合时长（小时），可配置 key: time-window.overlap-threshold-hours
     */
    @Value("${time-window.overlap-threshold-hours:1}")
    private double overlapThresholdHours;

    @Override
    public Long createTimeWindow(TimeWindowCreateReqVO createReqVO) {
        // 重合校验：不允许超过阈值
        validateOverlapOnSave(createReqVO.getStartTime(), createReqVO.getEndTime(), null);
        TimeWindowDO entity = BeanUtils.toBean(createReqVO, TimeWindowDO.class);
        if (entity.getStatus() == null) {
            entity.setStatus(DEFAULT_STATUS_ACTIVE);
        }
        if (entity.getSelectedUserIds() == null) {
            entity.setSelectedUserIds(java.util.Collections.emptyList());
        }
        if (entity.getExcludedUserIds() == null) {
            entity.setExcludedUserIds(java.util.Collections.emptyList());
        }
        if (entity.getTargetType() == null || entity.getTargetType().isEmpty()) {
            entity.setTargetType("user");
        }
        if (entity.getSelectedRoleIds() == null) {
            entity.setSelectedRoleIds(java.util.Collections.emptyList());
        }
        if (entity.getExcludedRoleIds() == null) {
            entity.setExcludedRoleIds(java.util.Collections.emptyList());
        }
        timeWindowMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateTimeWindow(TimeWindowUpdateReqVO updateReqVO) {
        validateTimeWindowExists(updateReqVO.getId());
        validateOverlapOnSave(updateReqVO.getStartTime(), updateReqVO.getEndTime(), updateReqVO.getId());
        TimeWindowDO updateObj = BeanUtils.toBean(updateReqVO, TimeWindowDO.class);
        timeWindowMapper.updateById(updateObj);
    }

    @Override
    public void deleteTimeWindow(Long id) {
        validateTimeWindowExists(id);
        timeWindowMapper.deleteById(id);
    }

    @Override
    public void deleteTimeWindowList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        timeWindowMapper.deleteByIds(ids);
    }

    @Override
    public TimeWindowDO getTimeWindow(Long id) {
        return timeWindowMapper.selectById(id);
    }

    @Override
    public PageResult<TimeWindowDO> getTimeWindowPage(TimeWindowPageReqVO pageReqVO) {
        return timeWindowMapper.selectPage(pageReqVO);
    }

    @Override
    public TimeWindowCheckOverlapRespVO checkOverlap(TimeWindowCheckOverlapReqVO reqVO) {
        double maxOverlapHours = computeMaxOverlapHours(reqVO.getStartTime(), reqVO.getEndTime(), reqVO.getExcludeId());
        boolean allowed = maxOverlapHours <= overlapThresholdHours;
        String message;
        if (maxOverlapHours <= 0) {
            message = null;
        } else if (maxOverlapHours > overlapThresholdHours) {
            message = String.format("时间窗口与已有记录重合约 %.2f 小时，超过允许的最大重合时长 %.2f 小时，不允许保存", maxOverlapHours, overlapThresholdHours);
        } else {
            message = String.format("与已存在的激活时间窗口重合约 %.2f 小时，是否仍要保存？", maxOverlapHours);
        }
        return TimeWindowCheckOverlapRespVO.builder()
                .allowed(allowed)
                .overlapHours(maxOverlapHours)
                .message(message)
                .overlapThresholdHours(overlapThresholdHours)
                .build();
    }

    @Override
    public TimeWindowConfigRespVO getConfig() {
        return TimeWindowConfigRespVO.builder()
                .overlapThresholdHours(overlapThresholdHours)
                .build();
    }

    @Override
    public double getOverlapThresholdHours() {
        return overlapThresholdHours;
    }

    @Override
    public TimeWindowDO getActiveWindowSelectingUserNow(Long userId) {
        Long targetUserId = userId != null ? userId : SecurityFrameworkUtils.getLoginUserId();
        if (targetUserId == null) {
            return null;
        }
        // 超级管理员不受时间窗口控制，直接返回空（放行）
        if (permissionApi.hasAnyRoles(targetUserId, RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return null;
        }
        Set<Long> userRoleIds = CollectionUtils.convertSet(userRoleMapper.selectListByUserId(targetUserId), UserRoleDO::getRoleId);
        // 用户没有任何角色则放行
        if (userRoleIds.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        List<TimeWindowDO> activeAtNow = timeWindowMapper.selectListActiveAtTime(now);
        for (TimeWindowDO w : activeAtNow) {
            if (TARGET_TYPE_ROLE.equalsIgnoreCase(w.getTargetType()) && windowCoversAllUserRoles(w, userRoleIds)) {
                return w;
            }
        }
        return null;
    }

    /**
     * 判断该时间窗口（按角色）是否覆盖当前用户的全部角色：每个用户角色都在选定角色中且都不在排除角色中。
     * 只有全部角色都被该窗口覆盖才认定为命中。
     */
    private boolean windowCoversAllUserRoles(TimeWindowDO w, Set<Long> userRoleIds) {
        List<Long> selectedRoles = w.getSelectedRoleIds() != null ? w.getSelectedRoleIds() : Collections.emptyList();
        List<Long> excludedRoles = w.getExcludedRoleIds() != null ? w.getExcludedRoleIds() : Collections.emptyList();
        for (Long roleId : userRoleIds) {
            if (!selectedRoles.contains(roleId) || excludedRoles.contains(roleId)) {
                return false;
            }
        }
        return true;
    }

    private void validateTimeWindowExists(Long id) {
        if (timeWindowMapper.selectById(id) == null) {
            throw exception(CustomErrorCodeConstants.TIME_WINDOW_NOT_EXISTS);
        }
    }

    /**
     * 保存前重合校验：重合时长 > 阈值则抛异常
     */
    private void validateOverlapOnSave(LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        double maxOverlap = computeMaxOverlapHours(startTime, endTime, excludeId);
        if (maxOverlap > overlapThresholdHours) {
            throw exception(CustomErrorCodeConstants.TIME_WINDOW_OVERLAP_EXCEEDED, String.format("%.2f", maxOverlap), String.format("%.2f", overlapThresholdHours));
        }
    }

    /**
     * 计算与所有已激活时间窗口的最大重合时长（小时）
     */
    private double computeMaxOverlapHours(LocalDateTime start, LocalDateTime end, Long excludeId) {
        List<TimeWindowDO> list = timeWindowMapper.selectListActive().stream()
                .filter(t -> excludeId == null || !t.getId().equals(excludeId))
                .collect(Collectors.toList());
        double maxHours = 0;
        for (TimeWindowDO tw : list) {
            double hours = overlapHours(start, end, tw.getStartTime(), tw.getEndTime());
            if (hours > maxHours) {
                maxHours = hours;
            }
        }
        return maxHours;
    }

    /**
     * 两区间 [s1,e1] 与 [s2,e2] 的重合时长（小时），无重合返回 0
     */
    private static double overlapHours(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        LocalDateTime overlapStart = s1.isAfter(s2) ? s1 : s2;
        LocalDateTime overlapEnd = e1.isBefore(e2) ? e1 : e2;
        if (!overlapStart.isBefore(overlapEnd)) {
            return 0;
        }
        return Duration.between(overlapStart, overlapEnd).toNanos() / (1_000_000_000.0 * 3600.0);
    }
}
