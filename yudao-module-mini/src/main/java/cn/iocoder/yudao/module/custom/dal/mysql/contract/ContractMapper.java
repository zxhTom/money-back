package cn.iocoder.yudao.module.custom.dal.mysql.contract;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.*;
import org.springframework.util.StringUtils;

/**
 * 合同 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ContractMapper extends BaseMapperX<ContractDO> {

    /** 与 DashboardMapper 中“逾期合同”的口径保持一致：已完结(3)/已取消(4)状态不算逾期 */
    List<Integer> NOT_OVERDUE_STATUSES = Arrays.asList(3, 4);

    static void applyOverdue(LambdaQueryWrapperX<ContractDO> wrapper, Boolean overdue) {
        if (Boolean.TRUE.equals(overdue)) {
            wrapper.lt(ContractDO::getEndDate, LocalDateTime.now())
                    .notIn(ContractDO::getStatus, NOT_OVERDUE_STATUSES);
        }
    }

    default PageResult<ContractDO> selectPage(ContractPageReqVO reqVO) {
        LambdaQueryWrapperX<ContractDO> wrapper = new LambdaQueryWrapperX<ContractDO>()
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
                .eqIfPresent(ContractDO::getInterest, reqVO.getInterest())
                .eqIfPresent(ContractDO::getRefund, reqVO.getRefund())
                .orderByDesc(ContractDO::getId);
        applyOverdue(wrapper, reqVO.getOverdue());
        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询当前登录用户创建的合同分页
     *
     * @param reqVO 分页查询条件
     * @param creator 创建者ID（当前登录用户ID）
     * @return 合同分页
     */
    default PageResult<ContractDO> selectSelfPage(ContractPageReqVO reqVO, String creator) {
        LambdaQueryWrapperX<ContractDO> wrapper = new LambdaQueryWrapperX<ContractDO>()
                .eqIfPresent(ContractDO::getCreator, creator)  // 只查询当前登录用户创建的数据
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
                .eqIfPresent(ContractDO::getInterest, reqVO.getInterest())
                .eqIfPresent(ContractDO::getRefund, reqVO.getRefund())
                .orderByDesc(ContractDO::getId);
        applyOverdue(wrapper, reqVO.getOverdue());
        return selectPage(reqVO, wrapper);
    }

    default PageResult<ContractDO> selectPageByNameKeyword(String nameKeyword, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ContractDO>()
                .and(StringUtils.hasText(nameKeyword), w -> w
                        .like(ContractDO::getIndebtedName, nameKeyword)
                        .or()
                        .like(ContractDO::getCreditorName, nameKeyword))
                .orderByDesc(ContractDO::getId));
    }

    /**
     * 判断两个（明文归一化）身份证号之间是否存在合同往来（互为甲乙方，不区分方向）。
     * 用于信用互查场景：无独立credit-query权限时，允许查询与自己有合同关系的对方。
     */
    default boolean existsContractRelation(String idNoA, String idNoB) {
        Long count = selectCount(new LambdaQueryWrapperX<ContractDO>()
                .and(w -> w.eq(ContractDO::getIndebtedId, idNoA).eq(ContractDO::getCreditorId, idNoB))
                .or(w -> w.eq(ContractDO::getIndebtedId, idNoB).eq(ContractDO::getCreditorId, idNoA)));
        return count != null && count > 0;
    }

}