package cn.iocoder.yudao.module.custom.service.custom;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// 信用查询权限守卫：仅允许持有专门权限或本人已实名查询
@Component
public class CreditQueryAccessChecker {

    @Resource
    private SecurityFrameworkService securityFrameworkService;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private IdCardCipherService idCardCipherService;

    public boolean canQueryCredit(String targetIdNoRaw) {
        if (StrUtil.isBlank(targetIdNoRaw)) {
            return false;
        }
        if (securityFrameworkService.hasPermission("custom:contract:credit-query")) {
            return true;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            return false;
        }
        AdminUserDO caller = adminUserService.getUser(loginUserId);
        if (caller == null || !Integer.valueOf(1).equals(caller.getVerified()) || StrUtil.isBlank(caller.getIdNo())) {
            return false;
        }
        return idCardCipherService.sameIdCard(caller.getIdNo(), targetIdNoRaw);
    }

}
