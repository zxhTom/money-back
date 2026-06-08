package cn.iocoder.yudao.module.custom.dal.dataobject.security;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("custom_ip_blacklist")
@Data
public class IpBlacklistDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ip;
    private String reason;
    private Boolean autoAdded;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
