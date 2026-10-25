package cn.iocoder.yudao.module.system.service.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalDiagnoseCacheEnvelope implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private Long userId;
    private String taskType;
    private Integer days;
    private Long createTime;
    private String dataJson;
}
