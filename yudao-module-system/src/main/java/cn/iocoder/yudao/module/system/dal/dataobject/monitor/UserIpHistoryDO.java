package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_user_ip_history")
@Data
public class UserIpHistoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String ip;

    private LocalDateTime firstSeen;

    private LocalDateTime lastSeen;

    private Integer count;
}
