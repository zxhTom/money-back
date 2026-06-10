package cn.iocoder.yudao.module.custom.dal.mysql.contract;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合同归档库 Mapper，所有操作路由到 archive 数据源
 */
@Mapper
@DS("archive")
public interface ArchiveContractMapper extends BaseMapperX<ContractDO> {
}
