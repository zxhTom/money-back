package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleNotifyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AlertRuleNotifyMapper extends BaseMapperX<AlertRuleNotifyDO> {

    default List<AlertRuleNotifyDO> selectByRuleId(Long ruleId) {
        return selectList(AlertRuleNotifyDO::getRuleId, ruleId);
    }

    default void deleteByRuleId(Long ruleId) {
        delete(AlertRuleNotifyDO::getRuleId, ruleId);
    }
}
