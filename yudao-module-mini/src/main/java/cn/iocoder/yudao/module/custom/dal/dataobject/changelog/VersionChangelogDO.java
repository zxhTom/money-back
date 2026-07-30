package cn.iocoder.yudao.module.custom.dal.dataobject.changelog;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 版本升级说明 DO
 *
 * 每个 version 只应有一条（唯一索引 uk_version）。
 */
@TableName("custom_version_changelog")
@Data
@EqualsAndHashCode(callSuper = true)
public class VersionChangelogDO extends BaseDO {

    private Long id;
    private String version;
    private String title;
    private String content;
    private Integer enabled;

}
