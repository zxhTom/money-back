package cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("custom_miniprogram_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class MiniProgramConfigDO extends BaseDO {

    private Long id;
    private String appName;
    private String slogan;
    private String appDescription;
    private String companyName;
    private String contactEmail;
    private Long boundUserId;

}
