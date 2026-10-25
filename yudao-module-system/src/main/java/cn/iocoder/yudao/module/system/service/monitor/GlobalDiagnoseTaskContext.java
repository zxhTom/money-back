package cn.iocoder.yudao.module.system.service.monitor;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public final class GlobalDiagnoseTaskContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long tenantId;
    private final Long userId;
    private final String taskType;
    private final Integer days;
    private final String lockToken;

    public GlobalDiagnoseTaskContext(Long tenantId, Long userId, String taskType, Integer days, String lockToken) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.taskType = taskType;
        this.days = days;
        this.lockToken = lockToken;
    }
}
