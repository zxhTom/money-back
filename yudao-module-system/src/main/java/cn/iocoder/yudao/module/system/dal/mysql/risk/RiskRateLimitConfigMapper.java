package cn.iocoder.yudao.module.system.dal.mysql.risk;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskRateLimitConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * API 访问频率规则配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface RiskRateLimitConfigMapper extends BaseMapperX<RiskRateLimitConfigDO> {

    default List<RiskRateLimitConfigDO> selectListByStatus(Integer status) {
        return selectList(RiskRateLimitConfigDO::getStatus, status);
    }

}
