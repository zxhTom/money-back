package cn.iocoder.yudao.module.custom.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 设备模块开放控制 DO
 */
@TableName("custom_device_module_access")
@KeySequence("custom_device_module_access_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceModuleAccessDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 设备 ID
     */
    private String deviceId;

    /**
     * 是否开放
     */
    private Boolean enabled;

}

