package cn.iocoder.yudao.module.custom.service.contract;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.CONTRACT_NOT_EXISTS;

/**
 * 合同归属校验：创建者本人 / 身份证匹配当事人 / 超管 / 合同管理员，四选一放行。
 * 抽取自 {@link ContractServiceImpl#getContract(Long)} 原有的读路径校验逻辑，供写路径复用。
 */
@Component
public class ContractOwnershipGuard {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleService roleService;
    @Resource
    private IdCardCipherService idCardCipherService;

    public boolean isAuthorized(ContractDO contract, Long loginUserId) {
        if (contract == null || loginUserId == null) {
            return false;
        }
        if (String.valueOf(loginUserId).equals(contract.getCreator())) {
            return true;
        }
        AdminUserDO user = adminUserService.getUser(loginUserId);
        if (user != null && StrUtil.isNotBlank(user.getIdNo())) {
            if (idCardCipherService.sameIdCard(user.getIdNo(), contract.getIndebtedId())
                    || idCardCipherService.sameIdCard(user.getIdNo(), contract.getCreditorId())) {
                return true;
            }
        }
        List<Long> roleIdList = userRoleMapper.selectListByUserId(loginUserId)
                .stream().map(UserRoleDO::getRoleId).collect(Collectors.toList());
        if (roleService.hasAnySuperAdmin(roleIdList)) {
            return true;
        }
        return roleService.getRoleList(roleIdList).stream()
                .map(RoleDO::getCode).anyMatch("contract-manager"::equals);
    }

    /**
     * 写路径专用：未通过归属校验时按"合同不存在"处理（不额外暴露记录确实存在），不走读路径的蜜罐机制
     */
    public void checkOrThrow(ContractDO contract, Long loginUserId) {
        if (contract == null || !isAuthorized(contract, loginUserId)) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
    }

}
