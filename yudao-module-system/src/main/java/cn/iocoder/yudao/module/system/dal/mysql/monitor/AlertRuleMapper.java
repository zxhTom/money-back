package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlertRuleMapper extends BaseMapperX<AlertRuleDO> {

    default AlertRuleDO selectByAlertType(String alertType) {
        return selectOne(AlertRuleDO::getAlertType, alertType);
    }
}
