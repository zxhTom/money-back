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
    /** 文案套类型：safe=安全模式基准 offcial=正式模式基准，创建后不可变 */
    private String textMode;
    /** 是否生效：同一 textMode 内仅一条应为 true（safe/offcial 各自互斥） */
    private Boolean isActive;
    /** 排序 */
    private Integer sort;
    /** 备注 */
    private String remark;

}
