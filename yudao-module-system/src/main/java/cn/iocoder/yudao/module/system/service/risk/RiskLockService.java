package cn.iocoder.yudao.module.system.service.risk;

import cn.iocoder.yudao.module.system.dal.dataobject.risk.RiskRateLimitConfigDO;

import java.util.List;

public interface RiskLockService {

    /**
     * 处理登录密码错误风控
     *
     * @param userId 用户编号
     */
    void processPasswordError(Long userId);

    /**
     * 检查用户锁定状态，若已锁定则抛出异常
     *
     * @param userId 用户编号
     */
    void checkLockStatus(Long userId);

    /**
     * 获取所有启用的 API 频控规则
     *
     * @return 启用的频控规则列表
     */
    List<RiskRateLimitConfigDO> getAllEnableRateLimitRules();

    /**
     * 阶梯锁定用户
     *
     * @param userId 用户编号
     * @param lockType 锁定类型
     * @param rule 触发限流的规则
     */
    void escalateLock(Long userId, String lockType, RiskRateLimitConfigDO rule);

}
