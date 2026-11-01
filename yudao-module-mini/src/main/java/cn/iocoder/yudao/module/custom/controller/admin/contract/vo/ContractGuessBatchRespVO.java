package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "合同联系人推荐批次")
@Data
public class ContractGuessBatchRespVO {
    private List<ContractGuessRespVO> items;
    private boolean hasMore;
    /** Optional proof of a served batch; older clients ignore it. */
    private String impressionId;
    private Integer privacyVersion;
}
