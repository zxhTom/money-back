package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IP 白名单：命中的 IP 躲过所有安全封禁与多登录风控。
 */
@TableName("custom_ip_whitelist")
@Data
public class IpWhitelistDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ip;
    private String remark;
    /** 来源：MANUAL=手动添加(同步不触碰) / WECHAT=微信官方自动同步 */
    private String source;
    private Integer enabled;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Integer deleted;
}
