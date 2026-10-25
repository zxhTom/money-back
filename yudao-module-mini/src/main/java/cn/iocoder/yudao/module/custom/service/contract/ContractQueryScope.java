package cn.iocoder.yudao.module.custom.service.contract;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.service.custom.CreditQueryAccessChecker;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

// 合同查询与统计作用域统一收敛守卫
@Component
public class ContractQueryScope {

    @Resource
    private CreditQueryAccessChecker creditQueryAccessChecker;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleService roleService;

    public static class ScopeResult {
        private final String creator;
        private final boolean isSuperOrManager;

        public ScopeResult(String creator, boolean isSuperOrManager) {
            this.creator = creator;
            this.isSuperOrManager = isSuperOrManager;
        }

        public String getCreator() {
            return creator;
        }

        public boolean isSuperOrManager() {
            return isSuperOrManager;
        }
    }

    public ScopeResult resolveScope(ContractPageReqVO reqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            throw new AccessDeniedException("当前未登录，无法查询合同数据");
        }

        boolean hasIndebted = reqVO != null && StrUtil.isNotBlank(reqVO.getIndebtedId());
        boolean hasCreditor = reqVO != null && StrUtil.isNotBlank(reqVO.getCreditorId());

        if (hasIndebted && !creditQueryAccessChecker.canQueryCredit(reqVO.getIndebtedId())) {
            throw new AccessDeniedException("无权限查询该欠款人合同数据");
        }
        if (hasCreditor && !creditQueryAccessChecker.canQueryCredit(reqVO.getCreditorId())) {
            throw new AccessDeniedException("无权限查询该债权人合同数据");
        }

        boolean hasPartyFilter = hasIndebted || hasCreditor;

        List<UserRoleDO> userRoleDOS = userRoleMapper.selectListByUserId(loginUserId);
        List<Long> roleIdList = userRoleDOS.stream().map(UserRoleDO::getRoleId).collect(Collectors.toList());
        List<RoleDO> roleList = roleService.getRoleList(roleIdList);
        List<RoleDO> enabledRoles = roleList.stream()
                .filter(role -> Integer.valueOf(0).equals(role.getStatus()))
                .collect(Collectors.toList());
        List<Long> enabledRoleIds = enabledRoles.stream().map(RoleDO::getId).collect(Collectors.toList());
        List<String> enabledRoleCodes = enabledRoles.stream().map(RoleDO::getCode).collect(Collectors.toList());

        boolean isSuperAdmin = roleService.hasAnySuperAdmin(enabledRoleIds);
        boolean isManager = isSuperAdmin || enabledRoleCodes.contains("contract-manager");

        if (hasPartyFilter) {
            return new ScopeResult(null, isManager);
        }

        String creator = isManager ? null : String.valueOf(loginUserId);
        return new ScopeResult(creator, isManager);
    }

    public LambdaQueryWrapperX<ContractDO> buildScopedWrapper(ContractPageReqVO reqVO) {
        return buildScopedWrapper(reqVO, true);
    }

    public LambdaQueryWrapperX<ContractDO> buildScopedWrapper(ContractPageReqVO reqVO, boolean withOrderBy) {
        ScopeResult scope = resolveScope(reqVO);
        LambdaQueryWrapperX<ContractDO> wrapper = new LambdaQueryWrapperX<ContractDO>()
                .eq(ContractDO::getDeleted, 0)
                .eqIfPresent(ContractDO::getCreator, scope.getCreator())
                .likeIfPresent(ContractDO::getIndebtedName, reqVO != null ? reqVO.getIndebtedName() : null)
                .eqIfPresent(ContractDO::getIndebtedId, reqVO != null ? reqVO.getIndebtedId() : null)
                .likeIfPresent(ContractDO::getCreditorName, reqVO != null ? reqVO.getCreditorName() : null)
                .eqIfPresent(ContractDO::getCreditorId, reqVO != null ? reqVO.getCreditorId() : null)
                .eqIfPresent(ContractDO::getDescription, reqVO != null ? reqVO.getDescription() : null)
                .eqIfPresent(ContractDO::getStatus, reqVO != null ? reqVO.getStatus() : null)
                .betweenIfPresent(ContractDO::getCreateTime, reqVO != null ? reqVO.getCreateTime() : null)
                .betweenIfPresent(ContractDO::getStartDate, reqVO != null ? reqVO.getStartDate() : null)
                .betweenIfPresent(ContractDO::getEndDate, reqVO != null ? reqVO.getEndDate() : null)
                .eqIfPresent(ContractDO::getReturnType, reqVO != null ? reqVO.getReturnType() : null)
                .eqIfPresent(ContractDO::getReasonType, reqVO != null ? reqVO.getReasonType() : null)
                .eqIfPresent(ContractDO::getDetailReason, reqVO != null ? reqVO.getDetailReason() : null)
                .eqIfPresent(ContractDO::getSalary, reqVO != null ? reqVO.getSalary() : null)
                .eqIfPresent(ContractDO::getTariff, reqVO != null ? reqVO.getTariff() : null)
                .eqIfPresent(ContractDO::getFile, reqVO != null ? reqVO.getFile() : null)
                .eqIfPresent(ContractDO::getInterest, reqVO != null ? reqVO.getInterest() : null)
                .eqIfPresent(ContractDO::getRefund, reqVO != null ? reqVO.getRefund() : null);
        if (withOrderBy) {
            wrapper.orderByDesc(ContractDO::getId);
        }
        if (reqVO != null) {
            ContractMapper.applyOverdue(wrapper, reqVO.getOverdue());
        }
        return wrapper;
    }
}
