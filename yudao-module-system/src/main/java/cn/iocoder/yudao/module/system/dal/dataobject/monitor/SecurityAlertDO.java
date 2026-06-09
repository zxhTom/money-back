package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_security_alert")
@Data
public class SecurityAlertDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String alertType;
    private Integer severity;
    private String sourceIp;
    private Long userId;
    private String requestUrl;
    private String requestMethod;
    private String suspiciousContent;
    private String alertMessage;
    private String detail;
    private Integer handled;
    private Long handleBy;
    private LocalDateTime handleTime;
    private String handleRemark;
    private LocalDateTime createTime;
}
