package cn.iocoder.yudao.module.custom.dal.dataobject.skin;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 小程序皮肤配置 DO
 */
@TableName(value = "custom_skin_profile", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class SkinProfileDO extends BaseDO {

    /** 皮肤类型：预设，不可删除、核心字段不可改 */
    public static final int TYPE_PRESET = 0;
    /** 皮肤类型：自定义 */
    public static final int TYPE_CUSTOM = 1;

    /** 配置模式：基础模式，tokens 生效 */
    public static final int CONFIG_MODE_BASIC = 0;
    /** 配置模式：高级模式，customCssText 追加覆盖 tokens 中同名 token */
    public static final int CONFIG_MODE_ADVANCED = 1;

    @TableId
    private Long id;
    /** 皮肤名称 */
    private String name;
    /** 内部唯一标识 */
    private String code;
    /** 类型：0=预设(不可删除) 1=自定义 */
    private Integer type;
    /** 若基于某预设克隆创建，记录来源皮肤 id */
    private Long sourcePresetId;
    /** 配置模式：0=基础模式(tokens生效) 1=高级模式(customCssText追加覆盖tokens中同名token) */
    private Integer configMode;
    /** token 键值对，如 {"--color-primary": "#6C4FF2", "--radius-lg": "32rpx"} */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> tokens;
    /** 声明式 CSS 变量文本，形如 --color-primary: #FF0000;\n--radius-lg: 40rpx; */
    private String customCssText;
    /** 缩略图地址 */
    private String thumbnailUrl;
    /** 是否生效：全表仅一条应为 true */
    private Boolean isActive;
    /** 排序 */
    private Integer sort;
    /** 备注 */
    private String remark;

}
