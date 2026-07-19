package cn.iocoder.yudao.module.custom.dal.dataobject.text;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小程序文案配置 DO
 */
@TableName("custom_text_profile")
@Data
@EqualsAndHashCode(callSuper = true)
public class TextProfileDO extends BaseDO {

    @TableId
    private Long id;
    /** 文案套名称 */
    private String name;
    /** 内部唯一标识 */
    private String code;
    /** 种子来源：初始固定为 safe，克隆时记录来源 profile 的 code */
    private String seedFrom;
    /** 是否生效：全表仅一条应为 true */
    private Boolean isActive;
    /** 排序 */
    private Integer sort;
    /** 备注 */
    private String remark;

}
