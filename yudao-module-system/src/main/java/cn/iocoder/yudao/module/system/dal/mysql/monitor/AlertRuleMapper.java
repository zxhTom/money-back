package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlertRuleMapper extends BaseMapperX<AlertRuleDO> {

    /**
     * 按 alertType 取一条规则（优先取启用的）。
     * URL_MONITOR 允许多条（不同 match_url），故此处用 LIMIT 1 而非 selectOne，避免多行报错；
     * 主要用于告警通知路由——同类型规则的通知目标一致。
     */
    default AlertRuleDO selectByAlertType(String alertType) {
        return selectOne(new LambdaQueryWrapperX<AlertRuleDO>()
                .eq(AlertRuleDO::getAlertType, alertType)
                .orderByDesc(AlertRuleDO::getEnabled)
                .last("LIMIT 1"));
    }
}
