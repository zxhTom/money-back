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

    /** 某身份证号作为欠款人或被欠款人（债权人）的合同总数，不限状态 */
    default Integer countByPartyIdCard(String idCard) {
        Long count = selectCount(new LambdaQueryWrapperX<ContractDO>()
                .and(w -> w.eq(ContractDO::getIndebtedId, idCard).or().eq(ContractDO::getCreditorId, idCard)));
        return count == null ? 0 : count.intValue();
    }

    /** 把该身份证号作为欠款人的合同，欠款人姓名改成 newName */
    default void updateIndebtedNameByIdCard(String idCard, String newName) {
        update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ContractDO>()
                .eq(ContractDO::getIndebtedId, idCard)
                .set(ContractDO::getIndebtedName, newName));
    }

    /** 把该身份证号作为被欠款人(债权人)的合同，被欠款人姓名改成 newName */
    default void updateCreditorNameByIdCard(String idCard, String newName) {
        update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ContractDO>()
                .eq(ContractDO::getCreditorId, idCard)
                .set(ContractDO::getCreditorName, newName));
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
    
    
    
    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("<script>" +
            "SELECT * FROM (SELECT * FROM custom_contract) t " +
            "<where> AND t.tenant_id = #{tenantId} " +
            "<if test=\"reqVO.keyword != null and reqVO.keyword != ''\"> " +
            "AND (t.id LIKE CONCAT('%', #{reqVO.keyword}, '%') " +
            "OR t.indebted_name LIKE CONCAT('%', #{reqVO.keyword}, '%') " +
            "OR t.creditor_name LIKE CONCAT('%', #{reqVO.keyword}, '%') " +
            "OR t.indebted_id LIKE CONCAT('%', #{reqVO.keyword}, '%') " +
            "OR t.creditor_id LIKE CONCAT('%', #{reqVO.keyword}, '%')) " +
            "</if> " +
            "<if test=\"reqVO.contractId != null\"> AND t.id = #{reqVO.contractId} </if> " +
            "<if test=\"reqVO.indebtedName != null and reqVO.indebtedName != ''\"> AND t.indebted_name LIKE CONCAT('%', #{reqVO.indebtedName}, '%') </if> " +
            "<if test=\"reqVO.creditorName != null and reqVO.creditorName != ''\"> AND t.creditor_name LIKE CONCAT('%', #{reqVO.creditorName}, '%') </if> " +
            "<if test=\"reqVO.indebtedId != null and reqVO.indebtedId != ''\"> AND t.indebted_id = #{reqVO.indebtedId} </if> " +
            "<if test=\"reqVO.creditorId != null and reqVO.creditorId != ''\"> AND t.creditor_id = #{reqVO.creditorId} </if> " +
            "<if test=\"reqVO.status != null\"> AND t.status = #{reqVO.status} </if> " +
            "</where> " +
            "ORDER BY t.id DESC " +
            "</script>")
    com.baomidou.mybatisplus.core.metadata.IPage<ContractDO> selectAdminSearchPage(
        com.baomidou.mybatisplus.core.metadata.IPage<ContractDO> page, 
        @org.apache.ibatis.annotations.Param("reqVO") ContractPageReqVO reqVO,
        @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT * FROM custom_contract WHERE id = #{id} AND tenant_id = #{tenantId}")
    ContractDO selectByIdIgnoreDeleted(@org.apache.ibatis.annotations.Param("id") Long id, @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    default boolean existsContractRelation(String idNoA, String idNoB) {
        Long count = selectCount(new LambdaQueryWrapperX<ContractDO>()
                .and(w -> w.eq(ContractDO::getIndebtedId, idNoA).eq(ContractDO::getCreditorId, idNoB))
                .or(w -> w.eq(ContractDO::getIndebtedId, idNoB).eq(ContractDO::getCreditorId, idNoA)));
        return count != null && count > 0;
    }


    @org.apache.ibatis.annotations.Select("<script>" +
            "SELECT COALESCE(SUM(salary), 0.0) FROM custom_contract " +
            "${ew.customSqlSegment} " +
            "</script>")
    Double selectSumSalary(@org.apache.ibatis.annotations.Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<ContractDO> wrapper);

    @org.apache.ibatis.annotations.Select("SELECT indebted_name as targetName, indebted_id as targetIdCard " +
            "FROM custom_contract " +
            "WHERE creditor_id IN (#{myIdCard}, #{plainIdCard}) AND deleted = 0 " +
            "AND indebted_id NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "GROUP BY indebted_name, indebted_id " +
            "ORDER BY count(*) DESC, max(create_time) DESC, sum(salary) DESC " +
            "LIMIT 1")
    cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractGuessRespVO guessDebtor(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard, @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard);

    @org.apache.ibatis.annotations.Select("SELECT creditor_name as targetName, creditor_id as targetIdCard " +
            "FROM custom_contract " +
            "WHERE indebted_id IN (#{myIdCard}, #{plainIdCard}) AND deleted = 0 " +
            "AND creditor_id NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "GROUP BY creditor_name, creditor_id " +
            "ORDER BY count(*) DESC, max(create_time) DESC, sum(salary) DESC " +
            "LIMIT 1")
    cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractGuessRespVO guessCreditor(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard, @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard);

    // A valid, unrevoked token establishes login status; login_date records the actual login.
    String LOGGED_IN_USERS = "SELECT user_id, MAX(create_time) AS login_time FROM system_oauth2_access_token " +
            "WHERE tenant_id = #{tenantId} AND user_type = 2 AND deleted = 0 AND expires_time > NOW() " +
            "GROUP BY user_id";

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT c.indebted_name AS targetName, c.indebted_id AS targetIdCard " +
            "FROM custom_contract c " +
            "JOIN system_users u ON u.id_no = c.indebted_id AND u.tenant_id = #{tenantId} " +
            "AND u.deleted = 0 AND u.status = 0 AND u.id != #{callerId} " +
            "JOIN (" + LOGGED_IN_USERS + ") t ON t.user_id = u.id " +
            "WHERE c.creditor_id IN (#{myIdCard}, #{plainIdCard}) AND c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "AND c.indebted_id NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "GROUP BY c.indebted_name, c.indebted_id, u.id, u.login_date, t.login_time " +
            "ORDER BY COUNT(*) DESC, COALESCE(u.login_date, t.login_time) DESC, MAX(c.create_time) DESC LIMIT 1")
    ContractGuessRespVO guessActiveDebtor(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard,
                                          @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard,
                                          @org.apache.ibatis.annotations.Param("callerId") Long callerId,
                                          @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT c.creditor_name AS targetName, c.creditor_id AS targetIdCard " +
            "FROM custom_contract c " +
            "JOIN system_users u ON u.id_no = c.creditor_id AND u.tenant_id = #{tenantId} " +
            "AND u.deleted = 0 AND u.status = 0 AND u.id != #{callerId} " +
            "JOIN (" + LOGGED_IN_USERS + ") t ON t.user_id = u.id " +
            "WHERE c.indebted_id IN (#{myIdCard}, #{plainIdCard}) AND c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "AND c.creditor_id NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "GROUP BY c.creditor_name, c.creditor_id, u.id, u.login_date, t.login_time " +
            "ORDER BY COUNT(*) DESC, COALESCE(u.login_date, t.login_time) DESC, MAX(c.create_time) DESC LIMIT 1")
    ContractGuessRespVO guessActiveCreditor(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard,
                                            @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard,
                                            @org.apache.ibatis.annotations.Param("callerId") Long callerId,
                                            @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT u.realname AS targetName, u.id_no AS targetIdCard " +
            "FROM system_users u JOIN (" + LOGGED_IN_USERS + ") t ON t.user_id = u.id " +
            "WHERE u.tenant_id = #{tenantId} AND u.id != #{callerId} AND u.deleted = 0 " +
            "AND u.id_no NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "AND u.status = 0 AND u.verified = 1 AND u.id_no IS NOT NULL AND u.id_no != '' " +
            "AND u.realname IS NOT NULL AND u.realname != '' " +
            "AND u.create_time >= DATE_SUB(NOW(), INTERVAL 14 DAY) " +
            "AND NOT EXISTS (SELECT 1 FROM custom_contract c WHERE c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "AND ((c.creditor_id IN (#{myIdCard}, #{plainIdCard}) AND c.indebted_id = u.id_no) " +
            "OR (c.indebted_id IN (#{myIdCard}, #{plainIdCard}) AND c.creditor_id = u.id_no))) " +
            "ORDER BY COALESCE(u.login_date, t.login_time) DESC, u.create_time DESC LIMIT 1")
    ContractGuessRespVO guessNewLoggedInUser(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard,
                                              @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard,
                                              @org.apache.ibatis.annotations.Param("callerId") Long callerId,
                                              @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT MAX(COALESCE(NULLIF(u.realname, ''), c.indebted_name)) AS targetName, " +
            "c.indebted_id AS targetIdCard, u.id AS targetUserId, COUNT(*) AS relationCount, " +
            "MAX(c.create_time) AS lastContractTime, " +
            "MAX(GREATEST(COALESCE(u.login_date, t.login_time), t.login_time)) AS lastLoginTime " +
            "FROM custom_contract c JOIN system_users u ON u.id_no = c.indebted_id " +
            "AND u.tenant_id = #{tenantId} AND u.deleted = 0 AND u.status = 0 AND u.id != #{callerId} " +
            "JOIN (" + LOGGED_IN_USERS + ") t ON t.user_id = u.id " +
            "WHERE c.creditor_id IN (#{myIdCard}, #{plainIdCard}) AND c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "AND c.indebted_id NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "GROUP BY c.indebted_id, u.id, u.login_date, t.login_time " +
            "ORDER BY COUNT(*) DESC, MAX(GREATEST(COALESCE(u.login_date, t.login_time), t.login_time)) DESC, " +
            "MAX(c.create_time) DESC LIMIT 30")
    List<ContractGuessRespVO> guessActiveDebtors(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard,
                                                  @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard,
                                                  @org.apache.ibatis.annotations.Param("callerId") Long callerId,
                                                  @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT MAX(COALESCE(NULLIF(u.realname, ''), c.creditor_name)) AS targetName, " +
            "c.creditor_id AS targetIdCard, u.id AS targetUserId, COUNT(*) AS relationCount, " +
            "MAX(c.create_time) AS lastContractTime, " +
            "MAX(GREATEST(COALESCE(u.login_date, t.login_time), t.login_time)) AS lastLoginTime " +
            "FROM custom_contract c JOIN system_users u ON u.id_no = c.creditor_id " +
            "AND u.tenant_id = #{tenantId} AND u.deleted = 0 AND u.status = 0 AND u.id != #{callerId} " +
            "JOIN (" + LOGGED_IN_USERS + ") t ON t.user_id = u.id " +
            "WHERE c.indebted_id IN (#{myIdCard}, #{plainIdCard}) AND c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "AND c.creditor_id NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "GROUP BY c.creditor_id, u.id, u.login_date, t.login_time " +
            "ORDER BY COUNT(*) DESC, MAX(GREATEST(COALESCE(u.login_date, t.login_time), t.login_time)) DESC, " +
            "MAX(c.create_time) DESC LIMIT 30")
    List<ContractGuessRespVO> guessActiveCreditors(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard,
                                                    @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard,
                                                    @org.apache.ibatis.annotations.Param("callerId") Long callerId,
                                                    @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT u.realname AS targetName, u.id_no AS targetIdCard, " +
            "u.id AS targetUserId, GREATEST(COALESCE(u.login_date, t.login_time), t.login_time) AS lastLoginTime " +
            "FROM system_users u JOIN (" + LOGGED_IN_USERS + ") t ON t.user_id = u.id " +
            "WHERE u.tenant_id = #{tenantId} AND u.id != #{callerId} AND u.deleted = 0 AND u.status = 0 " +
            "AND u.id_no NOT IN (#{myIdCard}, #{plainIdCard}) AND u.verified = 1 " +
            "AND u.id_no IS NOT NULL AND u.id_no != '' AND u.realname IS NOT NULL AND u.realname != '' " +
            "AND u.create_time >= DATE_SUB(NOW(), INTERVAL 14 DAY) " +
            "AND NOT EXISTS (SELECT 1 FROM custom_contract c WHERE c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "AND ((c.creditor_id IN (#{myIdCard}, #{plainIdCard}) AND c.indebted_id = u.id_no) " +
            "OR (c.indebted_id IN (#{myIdCard}, #{plainIdCard}) AND c.creditor_id = u.id_no))) " +
            "ORDER BY GREATEST(COALESCE(u.login_date, t.login_time), t.login_time) DESC, u.create_time DESC LIMIT 30")
    List<ContractGuessRespVO> guessNewLoggedInUsers(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard,
                                                     @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard,
                                                     @org.apache.ibatis.annotations.Param("callerId") Long callerId,
                                                     @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    // Active verified issuers broaden discovery without requiring a prior contract with the caller.
    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT u.realname AS targetName, u.id_no AS targetIdCard, " +
            "u.id AS targetUserId, COUNT(c.id) AS issuedCount, MAX(c.create_time) AS lastContractTime, " +
            "GREATEST(COALESCE(u.login_date, t.login_time), t.login_time) AS lastLoginTime " +
            "FROM system_users u JOIN (" + LOGGED_IN_USERS + ") t ON t.user_id = u.id " +
            "JOIN custom_contract c ON c.creditor_id = u.id_no AND c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "WHERE u.tenant_id = #{tenantId} AND u.id != #{callerId} AND u.deleted = 0 AND u.status = 0 " +
            "AND u.verified = 1 AND u.id_no IS NOT NULL AND u.id_no != '' " +
            "AND u.id_no NOT IN (#{myIdCard}, #{plainIdCard}) " +
            "AND u.realname IS NOT NULL AND u.realname != '' " +
            "GROUP BY u.id, u.realname, u.id_no, u.login_date, t.login_time " +
            "ORDER BY COUNT(c.id) DESC, GREATEST(COALESCE(u.login_date, t.login_time), t.login_time) DESC LIMIT 30")
    List<ContractGuessRespVO> guessActiveIssuers(@org.apache.ibatis.annotations.Param("myIdCard") String myIdCard,
                                                  @org.apache.ibatis.annotations.Param("plainIdCard") String plainIdCard,
                                                  @org.apache.ibatis.annotations.Param("callerId") Long callerId,
                                                  @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

    @com.baomidou.mybatisplus.annotation.InterceptorIgnore(tenantLine = "true", illegalSql = "false")
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) > 0 FROM system_oauth2_access_token " +
            "WHERE tenant_id = #{tenantId} AND user_id = #{targetId} AND user_type = 2 " +
            "AND deleted = 0 AND expires_time > NOW()")
    boolean isGuessCandidateLoggedIn(@org.apache.ibatis.annotations.Param("targetId") Long targetId,
                                     @org.apache.ibatis.annotations.Param("tenantId") Long tenantId);

}
