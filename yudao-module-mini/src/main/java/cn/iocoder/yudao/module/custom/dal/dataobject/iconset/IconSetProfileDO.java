package cn.iocoder.yudao.module.custom.dal.dataobject.iconset;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 小程序图标集配置 DO
 */
@TableName(value = "custom_icon_set_profile", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class IconSetProfileDO extends BaseDO {

    /** 图标集类型：预设，不可删除、核心字段不可改 */
    public static final int TYPE_PRESET = 0;
    /** 图标集类型：自定义 */
    public static final int TYPE_CUSTOM = 1;

    @TableId
    private Long id;
    /** 图标集名称 */
    private String name;
    /** 内部唯一标识 */
    private String code;
    /** 类型：0=预设(不可删除) 1=自定义 */
    private Integer type;
    /** 若基于某预设克隆创建，记录来源图标集 id */
    private Long sourcePresetId;
    /** 图标key -> SVG源码，如 {"plus": "<svg>...</svg>"}，缺失的key由调用方回退到预设图案 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> icons;
    /** 缩略图地址 */
    private String thumbnailUrl;
    /** 是否生效：全表仅一条应为 true */
    private Boolean isActive;
    /** 排序 */
    private Integer sort;
    /** 备注 */
    private String remark;

}
