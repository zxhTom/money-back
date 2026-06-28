package cn.iocoder.yudao.module.custom.dal.dataobject.audit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_audit_log")
@Data
public class AuditLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceId;
    private Long userId;
    private Integer userType;
    private String username;
    private String module;
    private String operationType;
    private String entityType;
    private Long entityId;
    private String operation;
    private String beforeData;
    private String afterData;
    private String requestParams;
    private String requestUrl;
    private String requestMethod;
    private String externalIp;
    private String directIp;
    private String allIpHeaders;
    private String userAgent;
    private Integer status;
    private String errorMessage;
    private Integer costTime;
    private LocalDateTime createTime;
}
