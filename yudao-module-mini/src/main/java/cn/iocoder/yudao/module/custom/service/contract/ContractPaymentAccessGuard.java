package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Objects;

/** Preserves the mini-program payment API while checking the underlying object. */
@Component
public class ContractPaymentAccessGuard {
    @Resource private PayOrderService orderService;
    @Resource private CustomDefineMapper customDefineMapper;
    @Resource private ContractMapper contractMapper;
    @Resource private ContractOwnershipGuard ownershipGuard;
    @Resource private AdminUserService userService;

    public PayOrderDO requireOrder(Long orderId) {
        Long caller = SecurityFrameworkUtils.getLoginUserId();
        PayOrderDO order = orderId == null ? null : orderService.getOrder(orderId);
        if (caller == null || order == null || !Objects.equals(order.getUserType(), UserTypeEnum.ADMIN.getValue()))
            throw new AccessDeniedException("无权访问该支付订单");
        // getUser is tenant-scoped. A global payment id must not cross tenant boundaries.
        cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO owner =
                order.getUserId() == null ? null : userService.getUser(order.getUserId());
        Long tenant = cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId();
        if (owner == null || tenant == null || !Objects.equals(tenant, owner.getTenantId()))
            throw new AccessDeniedException("无权访问该支付订单");
        if (Objects.equals(caller, order.getUserId())) return order;
        Long contractId = customDefineMapper.selectContractByPayOrderId(orderId);
        if (contractId == null || !ownershipGuard.isAuthorized(contractMapper.selectById(contractId), caller))
            throw new AccessDeniedException("无权访问该支付订单");
        return order;
    }
}
