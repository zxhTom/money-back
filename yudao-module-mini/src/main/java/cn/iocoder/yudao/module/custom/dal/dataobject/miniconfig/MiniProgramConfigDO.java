package cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("custom_miniprogram_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class MiniProgramConfigDO extends TenantBaseDO {

    private Long id;
    private String appName;
    private String slogan;
    private String appDescription;
    private String companyName;
    private String contactEmail;
    private Long boundUserId;\n    private String layoutTemplate;

}
