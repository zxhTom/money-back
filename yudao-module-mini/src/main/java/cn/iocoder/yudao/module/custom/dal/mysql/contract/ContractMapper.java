package cn.iocoder.yudao.module.custom.dal.mysql.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 客户端 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ContractMapper extends BaseMapperX<ContractDO> {

    PageResult<ContractDO> selectPageCustom(@Param("query") ContractPageReqVO reqVO) ;
    default PageResult<ContractDO> selectPage(ContractPageReqVO reqVO) {
        return selectPage(reqVO, reqVO.getSortFields(),new LambdaQueryWrapperX<ContractDO>()
                .likeIfPresent(ContractDO::getIndebtedName, reqVO.getIndebtedName())
                .eqIfPresent(ContractDO::getIndebtedId, reqVO.getIndebtedId())
                .likeIfPresent(ContractDO::getCreditorName, reqVO.getCreditorName())
                .eqIfPresent(ContractDO::getCreditorId, reqVO.getCreditorId())
                .eqIfPresent(ContractDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ContractDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ContractDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ContractDO::getId));
    }

}