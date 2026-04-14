package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreditPageReqVO extends PageParam {
    @Schema(description = "身份证筛选：明文或密文，服务端自动识别")
    private String idNo;
}
