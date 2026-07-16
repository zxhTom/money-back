package cn.iocoder.yudao.module.system.dal.dataobject.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IP 访问尝试记录（按 IP+原因+分钟 聚合，hit_count 累加）。
 * 主要记录"被拦截/被判可疑"的访问，用于观察被封 IP 是否仍在试探、以及每日频率。
 */
@TableName("custom_ip_access_log")
@Data
public class IpAccessLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ip;
    /** BLACKLIST_BLOCK / BRUTE_FORCE / SQL_INJECT / XSS */
    private String reason;
    private String uri;
    private String method;
    private String userAgent;
    private Long userId;
    /** 该(ip,reason,分钟)内的命中次数 */
    private Integer hitCount;
    /** 统计日（应用侧计算，避免 DB 时区偏差） */
    private LocalDate statDay;
    /** 分钟桶（去掉秒） */
    private LocalDateTime minuteBucket;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
