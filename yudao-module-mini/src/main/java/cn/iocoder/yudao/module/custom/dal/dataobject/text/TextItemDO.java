package cn.iocoder.yudao.module.custom.dal.dataobject.text;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小程序文案条目 DO
 */
@TableName("custom_text_item")
@Data
@EqualsAndHashCode(callSuper = true)
public class TextItemDO extends BaseDO {

    @TableId
    private Long id;
    /** 所属文案套 id */
    private Long profileId;
    /** 页面标识，如 contract.contractDetail */
    private String pageKey;
    /** 模块标识，用于同一页面内分组 */
    private String moduleKey;
    /** 完整文案 key，如 contract.contractDetail.title */
    private String itemKey;
    /** 文案内容 */
    private String itemValue;
    /** 排序 */
    private Integer sort;
    /** 备注 */
    private String remark;

}
