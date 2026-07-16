package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IpAccessLogPageReqVO extends PageParam {

    private String ip;
    /** BLACKLIST_BLOCK / BRUTE_FORCE / SQL_INJECT / XSS */
    private String reason;
    /** 统计日范围 yyyy-MM-dd */
    private String dayStart;
    private String dayEnd;
}
