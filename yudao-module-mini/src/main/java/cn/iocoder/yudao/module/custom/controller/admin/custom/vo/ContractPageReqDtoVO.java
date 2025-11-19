package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import lombok.Data;

@Data
public class ContractPageReqDtoVO extends ContractPageReqVO {
    private String loanType;
}
