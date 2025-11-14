package cn.iocoder.yudao.module.custom.dal.mysql.contract;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.*;

/**
 * 合同 Mapper
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
                .betweenIfPresent(ContractDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(ContractDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(ContractDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(ContractDO::getReasonType, reqVO.getReasonType())
                .eqIfPresent(ContractDO::getDetailReason, reqVO.getDetailReason())
                .eqIfPresent(ContractDO::getSalary, reqVO.getSalary())
                .eqIfPresent(ContractDO::getTariff, reqVO.getTariff())
                .eqIfPresent(ContractDO::getFile, reqVO.getFile())
                .orderByDesc(ContractDO::getId));
    }

}