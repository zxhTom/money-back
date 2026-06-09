package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_data_access_log")
@Data
public class DataAccessLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String entityType;
    /** 查询参数 JSON */
    private String queryParams;
    /** 返回记录 ID 列表 JSON，如 [1,2,3] */
    private String resultIds;
    private Integer resultCount;
    private String requestUrl;
    private String clientIp;
    private LocalDateTime accessTime;
    private Integer deleted;
}
