package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ContractPayOrderCreateReqVO {
    @NotNull(message = "合同ID不能为空")
    private Long contractId;
    @NotBlank(message = "支付密码不能为空")
    private String password;
}
