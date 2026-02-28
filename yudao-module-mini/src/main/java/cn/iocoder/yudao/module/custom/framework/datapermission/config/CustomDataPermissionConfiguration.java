package cn.iocoder.yudao.module.custom.framework.datapermission.config;

import cn.iocoder.yudao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * custom 模块的数据权限 Configuration
 *
 * @author zxhtom
 */
//@Configuration(proxyBeanMethods = false)
public class CustomDataPermissionConfiguration {

//    @Bean
    public DeptDataPermissionRuleCustomizer customDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // 合同表的数据权限配置
            // dept: 部门字段，用于部门数据权限过滤
            rule.addDeptColumn(ContractDO.class);
            // user: 用户字段（creator），用于仅本人数据权限过滤
            rule.addUserColumn(ContractDO.class, "creator");
        };
    }

}
