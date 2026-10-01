package cn.iocoder.yudao.module.system.service.risk;

public interface RiskLockService {
    void processPasswordError(Long userId);
    void checkLockStatus(Long userId);
}
