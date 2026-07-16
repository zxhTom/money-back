package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * "戏耍模式"守卫：判断当前登录用户是否被标记为 teaseEnabled。
 *
 * 只影响合同相关查询接口，绝不影响 {@link AdminUserService#getUser} 等用户 profile 接口——
 * 被戏耍用户查自己的个人信息必须始终是真实数据。
 */
@Component
public class ContractTeaseGuard {

    @Resource
    private AdminUserService adminUserService;

    public boolean isTeasing(Long loginUserId) {
        if (loginUserId == null) {
            return false;
        }
        AdminUserDO user = adminUserService.getUser(loginUserId);
        return user != null && Boolean.TRUE.equals(user.getTeaseEnabled());
    }

}
