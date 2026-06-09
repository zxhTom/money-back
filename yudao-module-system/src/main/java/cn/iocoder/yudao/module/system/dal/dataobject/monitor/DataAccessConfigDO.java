package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_data_access_config")
@Data
public class DataAccessConfigDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 模块标识，对应 @DataAccessMonitor.module */
    private String module;
    /** 实体类型，对应 @DataAccessMonitor.entityType */
    private String entityType;
    /** null=全局默认，非null=针对特定用户 */
    private Long userId;
    /** 是否启用监控 */
    private Boolean enabled;
    /** 时间窗口内最大访问条数，null=不限制 */
    private Integer maxRecordsPerWindow;
    /** 时间窗口（秒），默认 86400 */
    private Integer windowSeconds;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
