package cn.iocoder.yudao.module.custom.dal.dataobject.timewindow;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间窗口 DO
 */
@TableName(value = "time_window", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeWindowDO extends BaseDO {

    @TableId
    private Long id;

    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 快捷选择：1 / 3 / 7，可选 */
    private Integer quickSelect;
    /** 按用户或按角色：user=按用户 role=按角色 */
    private String targetType;
    /** 选定用户 ID 列表，targetType=user 时使用 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> selectedUserIds;
    /** 排除用户 ID 列表，与选定重合时以排除为准 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> excludedUserIds;
    /** 选定角色 ID 列表，targetType=role 时使用 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> selectedRoleIds;
    /** 排除角色 ID 列表，与选定重合时以排除为准 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> excludedRoleIds;
    /** 激活状态：0 未激活 1 激活 */
    private Integer status;
}
