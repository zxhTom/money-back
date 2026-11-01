package cn.iocoder.yudao.module.custom.controller.admin.contract.vo;

import lombok.Data;

@Data
public class ContractGuessSelectionReqVO {
    private String direction;
    private String idNo;
    private String targetIdCard;
    private String impressionId;
    private String candidateToken;
}
