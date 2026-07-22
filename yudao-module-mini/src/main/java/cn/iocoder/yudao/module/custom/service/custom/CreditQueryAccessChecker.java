package cn.iocoder.yudao.module.custom.service.custom;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 信用查询访问权限守卫：持有 custom:contract:credit-query 权限可查任意人，
 * 否则仅当当前登录用户与被查询人存在合同往来（custom_contract 表里互为甲乙方）时才放行。
 *
 * 原来 checkUserInfo(提交查询)与creditSearch(明细统计)分别挂了两个不一致的 @PreAuthorize 权限，
 * 导致同一个查询流程里一个接口拒绝一个接口放行；改为两处都调用本类做统一判断。
 */
@Component
public class CreditQueryAccessChecker {

    @Resource
    private SecurityFrameworkService securityFrameworkService;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private IdCardCipherService idCardCipherService;

    @Resource
    private ContractMapper contractMapper;

    public boolean canQueryCredit(String targetIdNoRaw) {
        if (securityFrameworkService.hasPermission("custom:contract:credit-query")) {
            return true;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            return false;
        }
        AdminUserDO caller = adminUserService.getUser(loginUserId);
        if (caller == null || StrUtil.isBlank(caller.getIdNo())) {
            return false;
        }
        String callerIdNo = idCardCipherService.resolveToPlain(caller.getIdNo());
        String targetIdNo = idCardCipherService.resolveToPlain(targetIdNoRaw);
        if (StrUtil.isBlank(callerIdNo) || StrUtil.isBlank(targetIdNo)) {
            return false;
        }
        return contractMapper.existsContractRelation(callerIdNo, targetIdNo);
    }

}
