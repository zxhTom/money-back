package cn.iocoder.yudao.module.custom.dal.dataobject.security;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 浏览器 User-Agent 白名单关键词。
 * UA 里包含任一启用的 keyword（不区分大小写）即视为合法浏览器。
 */
@TableName("custom_ua_whitelist")
@Data
@EqualsAndHashCode(callSuper = true)
public class UaWhitelistDO extends BaseDO {

    private Long id;
    private String keyword;
    private String remark;
    private Integer enabled;

}
