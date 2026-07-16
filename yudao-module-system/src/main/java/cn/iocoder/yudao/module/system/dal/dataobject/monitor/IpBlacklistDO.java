package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

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
    private Integer banCount;
    /** 0=封禁中 1=已解封 */
    private Integer status;
    private Boolean autoAdded;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    /** 最近封禁时间：新封/再次封禁都会刷新 */
    private LocalDateTime updateTime;
}
