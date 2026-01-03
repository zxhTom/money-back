package cn.iocoder.yudao.module.custom.dal.mysql.contract;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractModelDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合同模型 Mapper
 * 
 * @author zxhtom
 */
@Mapper
public interface ContractModelMapper extends BaseMapperX<ContractModelDO> {

    /**
     * 根据 app_version 查询
     * 
     * @param appVersion 应用版本号
     * @return ContractModelDO
     */
    default ContractModelDO selectByAppVersion(String appVersion) {
        return selectOne(ContractModelDO::getAppVersion, appVersion);
    }
}

